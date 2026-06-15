package com.nexuscommerce.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String ISSUER = "nexuscommerce";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder()
                .encodeToString("0123456789abcdef0123456789abcdef".getBytes()); // 32 bytes
        jwtService = new JwtService(new JwtProperties(secret, 15, ISSUER));
    }

    @Test
    void generatesAndParsesToken() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, "alice@nexus.local", Set.of("CUSTOMER", "ADMIN"));
        Claims claims = jwtService.parse(token).getPayload();

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("alice@nexus.local");
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.get("roles", List.class)).containsExactlyInAnyOrder("CUSTOMER", "ADMIN");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void accessTtlSecondsMatchesConfiguredMinutes() {
        assertThat(jwtService.accessTtlSeconds()).isEqualTo(15 * 60);
    }

    @Test
    void rejectsSecretShorterThan256Bits() {
        String shortSecret = Base64.getEncoder().encodeToString("too-short".getBytes());
        assertThatThrownBy(() -> new JwtService(new JwtProperties(shortSecret, 15, ISSUER)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtService.generateAccessToken(UUID.randomUUID(), "x@y.z", Set.of("CUSTOMER"));
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertThatThrownBy(() -> jwtService.parse(tampered)).isInstanceOf(RuntimeException.class);
    }
}
