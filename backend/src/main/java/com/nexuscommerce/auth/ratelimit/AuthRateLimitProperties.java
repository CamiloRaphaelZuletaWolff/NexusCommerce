package com.nexuscommerce.auth.ratelimit;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Brute-force limits for the public auth endpoints ({@code app.auth.rate-limit.*}).
 *
 * @param loginAttemptsPerIp      login attempts (any outcome) one IP may make per window
 * @param loginFailuresPerEmail   failed logins for one account per window before it is locked out
 * @param registrationsPerIp      sign-ups one IP may make per window
 * @param window                  fixed window length
 */
@Validated
@ConfigurationProperties(prefix = "app.auth.rate-limit")
public record AuthRateLimitProperties(
        @Positive int loginAttemptsPerIp,
        @Positive int loginFailuresPerEmail,
        @Positive int registrationsPerIp,
        @NotNull Duration window) {
}
