package com.nexuscommerce.auth.api;

import com.nexuscommerce.auth.dto.AuthResponse;
import com.nexuscommerce.auth.dto.LoginRequest;
import com.nexuscommerce.auth.dto.RegisterRequest;
import com.nexuscommerce.auth.refresh.RefreshCookieFactory;
import com.nexuscommerce.auth.service.AuthResult;
import com.nexuscommerce.auth.service.AuthService;
import com.nexuscommerce.security.AppPrincipal;
import com.nexuscommerce.shared.web.RequestMetadata;
import com.nexuscommerce.users.api.dto.UserResponse;
import com.nexuscommerce.users.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final RefreshCookieFactory refreshCookieFactory;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletRequest httpRequest) {
        AuthResult result = authService.register(request, RequestMetadata.from(httpRequest));
        return authResponse(HttpStatus.CREATED, result);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        AuthResult result = authService.login(request, RequestMetadata.from(httpRequest));
        return authResponse(HttpStatus.OK, result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest httpRequest) {
        AuthResult result = authService.refresh(readRefreshCookie(httpRequest), RequestMetadata.from(httpRequest));
        return authResponse(HttpStatus.OK, result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        authService.logout(readRefreshCookie(httpRequest), RequestMetadata.from(httpRequest));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.clear().toString())
                .build();
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AppPrincipal principal) {
        return UserResponse.from(userService.getByIdOrThrow(principal.userId()));
    }

    private ResponseEntity<AuthResponse> authResponse(HttpStatus status, AuthResult result) {
        ResponseCookie cookie = refreshCookieFactory.build(result.refreshToken(), result.refreshExpiresAt());
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(AuthResponse.of(result.accessToken(), result.accessTtlSeconds(), result.user()));
    }

    private String readRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        String name = refreshCookieFactory.cookieName();
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
