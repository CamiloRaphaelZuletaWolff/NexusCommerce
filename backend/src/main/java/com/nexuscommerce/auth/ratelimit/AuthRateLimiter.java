package com.nexuscommerce.auth.ratelimit;

import com.nexuscommerce.shared.error.TooManyRequestsException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * In-memory, fixed-window brute-force protection for {@code /api/auth/login} and
 * {@code /api/auth/register}.
 *
 * <p>Two independent limits guard login: a per-IP budget of attempts (slows
 * credential stuffing from one source) and a per-account budget of <em>failures</em>
 * (stops password guessing on one account even when the attacker rotates IPs). A
 * successful login clears that account's failures.
 *
 * <p>Sized for the single-instance demo: state lives in this JVM, so it resets on
 * restart and is not shared between replicas (use Redis/bucket4j if this ever scales out).
 * The IP is {@code request.getRemoteAddr()}, i.e. whatever Spring's forwarded-header
 * handling resolved — never a raw {@code X-Forwarded-For} read.
 */
@Component
public class AuthRateLimiter {

    /** Upper bound on tracked keys so a flood of distinct IPs/emails cannot exhaust the heap. */
    static final int MAX_TRACKED_KEYS = 50_000;

    private final AuthRateLimitProperties properties;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Autowired
    public AuthRateLimiter(AuthRateLimitProperties properties) {
        this(properties, Clock.systemUTC());
    }

    AuthRateLimiter(AuthRateLimitProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    /** Call before authenticating. Consumes one per-IP attempt; rejects locked-out accounts. */
    public void checkLogin(String ip, String email) {
        consume("login-ip:" + ip, properties.loginAttemptsPerIp());
        Window failures = current("login-email:" + normalize(email));
        if (failures != null && failures.count() >= properties.loginFailuresPerEmail()) {
            throw tooMany(failures);
        }
    }

    public void recordLoginFailure(String email) {
        increment("login-email:" + normalize(email));
    }

    public void recordLoginSuccess(String email) {
        windows.remove("login-email:" + normalize(email));
    }

    public void checkRegister(String ip) {
        consume("register-ip:" + ip, properties.registrationsPerIp());
    }

    private void consume(String key, int limit) {
        Window window = increment(key);
        if (window.count() > limit) {
            throw tooMany(window);
        }
    }

    private Window increment(String key) {
        evictIfFull();
        Instant now = clock.instant();
        return windows.compute(key, (k, existing) ->
                existing == null || existing.isExpired(now, properties.window())
                        ? new Window(now, 1)
                        : new Window(existing.start(), existing.count() + 1));
    }

    private Window current(String key) {
        Window window = windows.get(key);
        return window == null || window.isExpired(clock.instant(), properties.window()) ? null : window;
    }

    private void evictIfFull() {
        if (windows.size() < MAX_TRACKED_KEYS) {
            return;
        }
        Instant now = clock.instant();
        windows.values().removeIf(window -> window.isExpired(now, properties.window()));
        if (windows.size() >= MAX_TRACKED_KEYS) {
            windows.clear(); // fail open rather than grow without bound
        }
    }

    private TooManyRequestsException tooMany(Window window) {
        Instant resetsAt = window.start().plus(properties.window());
        long retryAfter = Math.max(1, Duration.between(clock.instant(), resetsAt).toSeconds());
        return new TooManyRequestsException("Too many attempts. Try again later.", retryAfter);
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private record Window(Instant start, int count) {
        boolean isExpired(Instant now, Duration length) {
            return !now.isBefore(start.plus(length));
        }
    }
}
