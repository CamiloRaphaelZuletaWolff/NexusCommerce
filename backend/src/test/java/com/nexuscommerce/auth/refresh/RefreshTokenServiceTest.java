package com.nexuscommerce.auth.refresh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexuscommerce.auth.AuthProperties;
import com.nexuscommerce.auth.refresh.RefreshTokenService.IssuedToken;
import com.nexuscommerce.auth.refresh.RefreshTokenService.RotationResult;
import com.nexuscommerce.shared.error.InvalidTokenException;
import com.nexuscommerce.shared.web.RequestMetadata;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService service;

    private final RequestMetadata metadata = new RequestMetadata("127.0.0.1", "JUnit");

    @BeforeEach
    void setUp() {
        AuthProperties properties = new AuthProperties(
                new AuthProperties.Refresh(7),
                new AuthProperties.Cookie("nexus_refresh", false, "Lax", "/api/auth", ""));
        service = new RefreshTokenService(refreshTokenRepository, properties);
    }

    @Test
    void issueStoresHashNotRawToken() {
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        IssuedToken issued = service.issue(UUID.randomUUID(), metadata);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken stored = captor.getValue();

        assertThat(issued.rawToken()).isNotBlank();
        assertThat(stored.getTokenHash()).hasSize(64);              // SHA-256 hex
        assertThat(stored.getTokenHash()).isNotEqualTo(issued.rawToken());
        assertThat(stored.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void rotateRevokesOldTokenAndIssuesNew() {
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UUID userId = UUID.randomUUID();

        IssuedToken first = service.issue(userId, metadata);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken stored = captor.getValue();
        when(refreshTokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(stored));

        RotationResult result = service.rotate(first.rawToken(), metadata);

        assertThat(stored.getRevokedAt()).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.issued().rawToken()).isNotBlank();
    }

    @Test
    void rotateRejectsUnknownToken() {
        when(refreshTokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rotate("bogus", metadata))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rotateRejectsExpiredToken() {
        RefreshToken expired = new RefreshToken(
                UUID.randomUUID(), "hash", Instant.now().minusSeconds(60), null, null);
        when(refreshTokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.rotate("any", metadata))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void reusingARotatedTokenRevokesEveryActiveTokenOfTheUser() {
        UUID userId = UUID.randomUUID();
        RefreshToken rotatedLongAgo = new RefreshToken(
                userId, "hash", Instant.now().plusSeconds(3600), null, null);
        rotatedLongAgo.revoke(Instant.now().minus(RefreshTokenService.REUSE_GRACE).minusSeconds(1));
        when(refreshTokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(rotatedLongAgo));

        assertThatThrownBy(() -> service.rotate("stolen", metadata))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenRepository).revokeAllActiveForUser(eq(userId), any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void concurrentTabRaceWithinGraceIsRejectedWithoutRevokingTheFamily() {
        RefreshToken justRotated = new RefreshToken(
                UUID.randomUUID(), "hash", Instant.now().plusSeconds(3600), null, null);
        justRotated.revoke(Instant.now());
        when(refreshTokenRepository.findByTokenHashForUpdate(any())).thenReturn(Optional.of(justRotated));

        assertThatThrownBy(() -> service.rotate("raced", metadata))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenRepository, never()).revokeAllActiveForUser(any(), any());
    }
}
