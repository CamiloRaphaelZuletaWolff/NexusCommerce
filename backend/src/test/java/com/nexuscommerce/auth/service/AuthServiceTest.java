package com.nexuscommerce.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexuscommerce.audit.domain.AuditEventType;
import com.nexuscommerce.audit.service.AuditService;
import com.nexuscommerce.auth.dto.LoginRequest;
import com.nexuscommerce.auth.dto.RegisterRequest;
import com.nexuscommerce.auth.ratelimit.AuthRateLimiter;
import com.nexuscommerce.auth.refresh.RefreshTokenService;
import com.nexuscommerce.auth.refresh.RefreshTokenService.IssuedToken;
import com.nexuscommerce.auth.refresh.RefreshTokenService.RotationResult;
import com.nexuscommerce.shared.error.InvalidTokenException;
import com.nexuscommerce.shared.error.TooManyRequestsException;
import com.nexuscommerce.security.jwt.JwtService;
import com.nexuscommerce.shared.web.RequestMetadata;
import com.nexuscommerce.users.domain.Role;
import com.nexuscommerce.users.domain.User;
import com.nexuscommerce.users.service.UserService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserService userService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuditService auditService;
    @Mock private AuthRateLimiter rateLimiter;

    @InjectMocks private AuthService authService;

    private final RequestMetadata metadata = new RequestMetadata("127.0.0.1", "JUnit");

    private User demoUser() {
        User user = new User("alice@nexus.local", "hash", "Alice");
        user.addRole(new Role("CUSTOMER", "desc"));
        return user;
    }

    @Test
    void registerCreatesUserAndIssuesTokens() {
        User user = demoUser();
        when(userService.register("alice@nexus.local", "Alice", "Password123!", "CUSTOMER")).thenReturn(user);
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtService.accessTtlSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(any(), any()))
                .thenReturn(new IssuedToken("refresh-token", Instant.now().plusSeconds(3600)));

        AuthResult result = authService.register(
                new RegisterRequest("alice@nexus.local", "Password123!", "Alice"), metadata);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.user().email()).isEqualTo("alice@nexus.local");
        verify(auditService).success(eq(AuditEventType.USER_REGISTERED), any(), eq(metadata), any());
    }

    @Test
    void loginSuccessIssuesTokensAndAudits() {
        User user = demoUser();
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userService.findByEmail("alice@nexus.local")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtService.accessTtlSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(any(), any()))
                .thenReturn(new IssuedToken("refresh-token", Instant.now().plusSeconds(3600)));

        AuthResult result = authService.login(new LoginRequest("alice@nexus.local", "Password123!"), metadata);

        assertThat(result.accessToken()).isEqualTo("access-token");
        verify(auditService).success(eq(AuditEventType.LOGIN_SUCCESS), any(), eq(metadata), any());
    }

    @Test
    void loginFailureAuditsAndRethrows() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice@nexus.local", "wrong"), metadata))
                .isInstanceOf(BadCredentialsException.class);

        verify(auditService).failure(eq(AuditEventType.LOGIN_FAILURE), isNull(), eq(metadata), any());
        verify(rateLimiter).recordLoginFailure("alice@nexus.local");
    }

    @Test
    void loginRejectedByRateLimiterNeverChecksThePassword() {
        doThrow(new TooManyRequestsException("Too many attempts. Try again later.", 60))
                .when(rateLimiter).checkLogin("127.0.0.1", "alice@nexus.local");

        assertThatThrownBy(() -> authService.login(new LoginRequest("Alice@Nexus.local ", "x"), metadata))
                .isInstanceOf(TooManyRequestsException.class);

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void refreshForDisabledUserRevokesSessionsAndFails() {
        User user = demoUser();
        user.disable();
        when(refreshTokenService.rotate("refresh", metadata)).thenReturn(new RotationResult(
                user.getId(), new IssuedToken("next", Instant.now().plusSeconds(3600))));
        when(userService.getByIdOrThrow(user.getId())).thenReturn(user);

        assertThatThrownBy(() -> authService.refresh("refresh", metadata))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenService).revokeAllForUser(user.getId());
        verify(jwtService, never()).generateAccessToken(any(), any(), any());
    }
}
