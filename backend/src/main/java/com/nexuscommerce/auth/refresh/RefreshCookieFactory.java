package com.nexuscommerce.auth.refresh;

import com.nexuscommerce.auth.AuthProperties;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Builds the HttpOnly refresh-token cookie (and its cleared variant for logout). */
@Component
@RequiredArgsConstructor
public class RefreshCookieFactory {

    private final AuthProperties authProperties;

    public ResponseCookie build(String rawToken, Instant expiresAt) {
        return baseBuilder(rawToken)
                .maxAge(Duration.between(Instant.now(), expiresAt))
                .build();
    }

    public ResponseCookie clear() {
        return baseBuilder("").maxAge(0).build();
    }

    public String cookieName() {
        return authProperties.cookie().name();
    }

    private ResponseCookie.ResponseCookieBuilder baseBuilder(String value) {
        AuthProperties.Cookie cookie = authProperties.cookie();
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookie.name(), value)
                .httpOnly(true)
                .secure(cookie.secure())
                .sameSite(cookie.sameSite())
                .path(cookie.path());
        if (StringUtils.hasText(cookie.domain())) {
            builder.domain(cookie.domain());
        }
        return builder;
    }
}
