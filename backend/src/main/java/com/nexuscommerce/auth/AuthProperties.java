package com.nexuscommerce.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Auth configuration: refresh-token lifetime and refresh-cookie attributes ({@code app.auth.*}). */
@Validated
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(Refresh refresh, Cookie cookie) {

    public record Refresh(@Positive long ttlDays) {}

    public record Cookie(
            @NotBlank String name,
            boolean secure,
            @NotBlank String sameSite,
            @NotBlank String path,
            String domain) {}
}
