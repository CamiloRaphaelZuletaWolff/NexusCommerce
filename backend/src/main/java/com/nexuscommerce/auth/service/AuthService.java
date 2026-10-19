package com.nexuscommerce.auth.service;

import com.nexuscommerce.audit.domain.AuditEventType;
import com.nexuscommerce.audit.service.AuditService;
import com.nexuscommerce.auth.dto.LoginRequest;
import com.nexuscommerce.auth.dto.RegisterRequest;
import com.nexuscommerce.auth.ratelimit.AuthRateLimiter;
import com.nexuscommerce.auth.refresh.RefreshTokenService;
import com.nexuscommerce.auth.refresh.RefreshTokenService.IssuedToken;
import com.nexuscommerce.auth.refresh.RefreshTokenService.RotationResult;
import com.nexuscommerce.security.jwt.JwtService;
import com.nexuscommerce.shared.error.InvalidTokenException;
import com.nexuscommerce.shared.web.RequestMetadata;
import com.nexuscommerce.users.api.dto.UserResponse;
import com.nexuscommerce.users.domain.User;
import com.nexuscommerce.users.service.UserService;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

/** Orchestrates registration, login, refresh-token rotation and logout, with audit logging. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_ROLE = "CUSTOMER";

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;
    private final AuthRateLimiter rateLimiter;

    // Deliberately NOT transactional: the user must be committed (by UserService) before
    // the audit row, written in its own REQUIRES_NEW transaction, references it by FK.
    // Wrapping both in one transaction made every API registration fail at commit.
    public AuthResult register(RegisterRequest request, RequestMetadata metadata) {
        rateLimiter.checkRegister(clientIp(metadata));
        // Role is fixed server-side: RegisterRequest has no role field, so a client
        // cannot self-assign ADMIN/STORE_MANAGER (unknown JSON properties are ignored).
        User user = userService.register(request.email(), request.fullName(), request.password(), DEFAULT_ROLE);
        AuthResult result = issueFor(user, metadata);
        auditService.success(AuditEventType.USER_REGISTERED, user.getId(), metadata,
                Map.of("email", user.getEmail()));
        return result;
    }

    public AuthResult login(LoginRequest request, RequestMetadata metadata) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        rateLimiter.checkLogin(clientIp(metadata), email);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException ex) {
            rateLimiter.recordLoginFailure(email);
            auditService.failure(AuditEventType.LOGIN_FAILURE, null, metadata, Map.of("email", email));
            throw ex;
        }
        rateLimiter.recordLoginSuccess(email);
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Authentication failed"));
        AuthResult result = issueFor(user, metadata);
        auditService.success(AuditEventType.LOGIN_SUCCESS, user.getId(), metadata,
                Map.of("email", user.getEmail()));
        return result;
    }

    public AuthResult refresh(String rawRefreshToken, RequestMetadata metadata) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidTokenException("Missing refresh token");
        }
        RotationResult rotation = refreshTokenService.rotate(rawRefreshToken, metadata);
        User user = userService.getByIdOrThrow(rotation.userId());
        if (!user.isEnabled()) {
            // A disabled account must not keep minting access tokens via its refresh cookie.
            refreshTokenService.revokeAllForUser(user.getId());
            throw new InvalidTokenException("Account disabled");
        }
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.roleNames());
        auditService.success(AuditEventType.TOKEN_REFRESHED, user.getId(), metadata, null);
        return new AuthResult(accessToken, jwtService.accessTtlSeconds(),
                rotation.issued().rawToken(), rotation.issued().expiresAt(), UserResponse.from(user));
    }

    public void logout(String rawRefreshToken, RequestMetadata metadata) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revoke(rawRefreshToken);
        }
        auditService.success(AuditEventType.LOGOUT, null, metadata, null);
    }

    private static String clientIp(RequestMetadata metadata) {
        return metadata != null && metadata.ipAddress() != null ? metadata.ipAddress() : "unknown";
    }

    private AuthResult issueFor(User user, RequestMetadata metadata) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.roleNames());
        IssuedToken refresh = refreshTokenService.issue(user.getId(), metadata);
        return new AuthResult(accessToken, jwtService.accessTtlSeconds(),
                refresh.rawToken(), refresh.expiresAt(), UserResponse.from(user));
    }
}
