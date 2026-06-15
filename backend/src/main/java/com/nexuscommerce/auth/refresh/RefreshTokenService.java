package com.nexuscommerce.auth.refresh;

import com.nexuscommerce.auth.AuthProperties;
import com.nexuscommerce.shared.error.InvalidTokenException;
import com.nexuscommerce.shared.web.RequestMetadata;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Issues, rotates and revokes opaque refresh tokens (stored only as SHA-256 hashes). */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32; // 256-bit opaque token

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

    @Transactional
    public RotationResult rotate(String rawToken, RequestMetadata metadata) {
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
        if (!existing.isActive(Instant.now())) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }
        existing.revoke(Instant.now());
        IssuedToken next = issue(existing.getUserId(), metadata);
        return new RotationResult(existing.getUserId(), next);
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
