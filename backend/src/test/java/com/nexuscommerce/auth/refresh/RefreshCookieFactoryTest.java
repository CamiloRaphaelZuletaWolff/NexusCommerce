package com.nexuscommerce.auth.refresh;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexuscommerce.auth.AuthProperties;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

class RefreshCookieFactoryTest {

    @Test
    void buildsHardenedCookieFromProperties() {
        AuthProperties properties = new AuthProperties(
                new AuthProperties.Refresh(7),
                new AuthProperties.Cookie("nexus_refresh", true, "None", "/api/auth", "example.com"));
        RefreshCookieFactory factory = new RefreshCookieFactory(properties);

        ResponseCookie cookie = factory.build("raw-token", Instant.now().plus(7, ChronoUnit.DAYS));

        assertThat(cookie.getName()).isEqualTo("nexus_refresh");
        assertThat(cookie.getValue()).isEqualTo("raw-token");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("None");
        assertThat(cookie.getPath()).isEqualTo("/api/auth");
        assertThat(cookie.getDomain()).isEqualTo("example.com");
    }

    @Test
    void clearProducesExpiredEmptyCookie() {
        AuthProperties properties = new AuthProperties(
                new AuthProperties.Refresh(7),
                new AuthProperties.Cookie("nexus_refresh", false, "Lax", "/api/auth", ""));
        RefreshCookieFactory factory = new RefreshCookieFactory(properties);

        ResponseCookie cleared = factory.clear();

        assertThat(cleared.getValue()).isEmpty();
        assertThat(cleared.getMaxAge().isZero()).isTrue();
        assertThat(cleared.isHttpOnly()).isTrue();
    }
}
