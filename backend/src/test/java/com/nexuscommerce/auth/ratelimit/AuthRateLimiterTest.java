package com.nexuscommerce.auth.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexuscommerce.shared.error.TooManyRequestsException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthRateLimiterTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private AuthRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new AuthRateLimiter(
                new AuthRateLimitProperties(3, 2, 2, Duration.ofMinutes(15)), clock);
    }

    @Test
    void perIpLoginAttemptsAreCappedPerWindow() {
        for (int i = 0; i < 3; i++) {
            limiter.checkLogin("10.0.0.1", "user" + i + "@x.io");
        }
        assertThatThrownBy(() -> limiter.checkLogin("10.0.0.1", "other@x.io"))
                .isInstanceOf(TooManyRequestsException.class)
                .satisfies(ex -> assertThat(((TooManyRequestsException) ex).retryAfterSeconds()).isPositive());

        // A different IP is unaffected.
        assertThatCode(() -> limiter.checkLogin("10.0.0.2", "other@x.io")).doesNotThrowAnyException();
    }

    @Test
    void accountIsLockedAfterRepeatedFailuresEvenFromDifferentIps() {
        limiter.recordLoginFailure("victim@x.io");
        limiter.recordLoginFailure("VICTIM@x.io "); // normalized: same account

        assertThatThrownBy(() -> limiter.checkLogin("203.0.113.9", "victim@x.io"))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void successfulLoginClearsFailures() {
        limiter.recordLoginFailure("alice@x.io");
        limiter.recordLoginSuccess("alice@x.io");
        limiter.recordLoginFailure("alice@x.io");

        assertThatCode(() -> limiter.checkLogin("10.0.0.1", "alice@x.io")).doesNotThrowAnyException();
    }

    @Test
    void limitsResetWhenTheWindowElapses() {
        limiter.recordLoginFailure("bob@x.io");
        limiter.recordLoginFailure("bob@x.io");
        assertThatThrownBy(() -> limiter.checkLogin("10.0.0.1", "bob@x.io"))
                .isInstanceOf(TooManyRequestsException.class);

        clock.advance(Duration.ofMinutes(15));

        assertThatCode(() -> limiter.checkLogin("10.0.0.1", "bob@x.io")).doesNotThrowAnyException();
    }

    @Test
    void registrationsArePerIp() {
        limiter.checkRegister("10.0.0.1");
        limiter.checkRegister("10.0.0.1");

        assertThatThrownBy(() -> limiter.checkRegister("10.0.0.1"))
                .isInstanceOf(TooManyRequestsException.class);
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
