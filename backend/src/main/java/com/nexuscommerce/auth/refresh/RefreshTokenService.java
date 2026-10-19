package com.nexuscommerce.auth.refresh;

import com.nexuscommerce.auth.AuthProperties;
import com.nexuscommerce.shared.error.InvalidTokenException;
import com.nexuscommerce.shared.web.RequestMetadata;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues, rotates and revokes opaque refresh tokens (stored only as SHA-256 hashes).
 *
 * <p>Rotation is single-use with <b>reuse detection</b>: presenting a token that was
 * already rotated means it was copied (or the legitimate client lost a race), so every
 * active token of that user is revoked and both parties must log in again. A short
 * grace period tolerates the benign case of two browser tabs refreshing at once.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final int TOKEN_BYTES = 32; // 256-bit opaque token
    /** A token re-presented within this window after rotation is treated as a concurrent-tab race. */
    static final Duration REUSE_GRACE = Duration.ofSeconds(10);

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthProperties authProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public record IssuedToken(String rawToken, Instant expiresAt) {}

    public record RotationResult(UUID userId, IssuedToken issued) {}

    @Transactional
    public IssuedToken issue(UUID userId, RequestMetadata metadata) {
        String rawToken = generateRawToken();
        Instant expiresAt = Instant.now().plus(authProperties.refresh().ttlDays(), ChronoUnit.DAYS);
        refreshTokenRepository.save(new RefreshToken(
                userId,
                hash(rawToken),
                expiresAt,
                metadata != null ? metadata.userAgent() : null,
                metadata != null ? metadata.ipAddress() : null));
        return new IssuedToken(rawToken, expiresAt);
    }

    // noRollbackFor: the reuse-detection revocation must commit even though we then reject the request.
    @Transactional(noRollbackFor = InvalidTokenException.class)
    public RotationResult rotate(String rawToken, RequestMetadata metadata) {
        Instant now = Instant.now();
        RefreshToken existing = refreshTokenRepository.findByTokenHashForUpdate(hash(rawToken))
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
        if (existing.getRevokedAt() != null) {
            if (existing.getRevokedAt().plus(REUSE_GRACE).isBefore(now)) {
                int revoked = refreshTokenRepository.revokeAllActiveForUser(existing.getUserId(), now);
                log.warn("Refresh token reuse detected for user {}; revoked {} active token(s)",
                        existing.getUserId(), revoked);
            }
            throw new InvalidTokenException("Refresh token expired or revoked");
        }
        if (!existing.isActive(now)) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }
        existing.revoke(now);
        IssuedToken next = issue(existing.getUserId(), metadata);
        return new RotationResult(existing.getUserId(), next);
    }

    /** Logs a user out everywhere (reuse detection, disabled account, password reset). */
    @Transactional
    public int revokeAllForUser(UUID userId) {
        return refreshTokenRepository.revokeAllActiveForUser(userId, Instant.now());
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(token -> token.revoke(Instant.now()));
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
