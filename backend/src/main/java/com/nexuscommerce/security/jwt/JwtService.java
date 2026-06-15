package com.nexuscommerce.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/** Issues and verifies short-lived HS256 access tokens. */
@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTtlMinutes;
    private final String issuer;

    public JwtService(JwtProperties properties) {
        byte[] secretBytes = Decoders.BASE64.decode(properties.secret());
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret must be a Base64-encoded value of at least 256 bits (32 bytes)");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.accessTtlMinutes = properties.accessTtlMinutes();
        this.issuer = properties.issuer();
    }

    public String generateAccessToken(UUID userId, String email, Set<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessTtlMinutes, ChronoUnit.MINUTES);
        return Jwts.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .claim("email", email)
                .claim("roles", new ArrayList<>(roles))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public long accessTtlSeconds() {
        return accessTtlMinutes * 60;
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);
    }
}
