package com.nexuscommerce.bootstrap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexuscommerce.auth.refresh.RefreshTokenService;
import com.nexuscommerce.stores.repository.StoreRepository;
import com.nexuscommerce.stores.service.StoreService;
import com.nexuscommerce.users.domain.User;
import com.nexuscommerce.users.service.UserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** The demo admin must never be reachable with a published password. */
@ExtendWith(MockitoExtension.class)
class DevDataInitializerTest {

    private static final String ADMIN = "admin@nexus.local";
    private static final String STRONG = "a-long-private-demo-secret";

    @Mock private UserService userService;
    @Mock private StoreService storeService;
    @Mock private StoreRepository storeRepository;
    @Mock private RefreshTokenService refreshTokenService;

    private DevDataInitializer initializer(String adminPassword) {
        return new DevDataInitializer(userService, storeService, storeRepository, refreshTokenService,
                new DemoProperties(adminPassword));
    }

    private static User existingAdmin() {
        User admin = new User(ADMIN, "hash", "Platform Admin");
        ReflectionTestUtils.setField(admin, "id", UUID.randomUUID());
        return admin;
    }

    @Test
    void withoutPasswordNoAdminIsCreated() {
        when(userService.findByEmail(ADMIN)).thenReturn(Optional.empty());

        initializer("").ensureAdmin();

        verify(userService, never()).register(eq(ADMIN), any(), any(), any());
    }

    @Test
    void withoutPasswordAnExistingAdminIsDisabledAndLoggedOut() {
        User admin = existingAdmin();
        when(userService.findByEmail(ADMIN)).thenReturn(Optional.of(admin));

        initializer(null).ensureAdmin();

        verify(userService).disable(admin.getId());
        verify(refreshTokenService).revokeAllForUser(admin.getId());
    }

    @Test
    void configuredPasswordCreatesTheAdmin() {
        when(userService.findByEmail(ADMIN)).thenReturn(Optional.empty());

        initializer(STRONG).ensureAdmin();

        verify(userService).register(ADMIN, "Platform Admin", STRONG, "ADMIN");
    }

    @Test
    void existingAdminWithOldPasswordIsResyncedAndLoggedOut() {
        User admin = existingAdmin();
        when(userService.findByEmail(ADMIN)).thenReturn(Optional.of(admin));
        when(userService.passwordMatches(admin, STRONG)).thenReturn(false);

        initializer(STRONG).ensureAdmin();

        verify(userService).resetPassword(admin.getId(), STRONG);
        verify(refreshTokenService).revokeAllForUser(admin.getId());
    }

    @Test
    void existingAdminWithCurrentPasswordIsLeftAlone() {
        User admin = existingAdmin();
        when(userService.findByEmail(ADMIN)).thenReturn(Optional.of(admin));
        when(userService.passwordMatches(admin, STRONG)).thenReturn(true);

        initializer(STRONG).ensureAdmin();

        verify(userService, never()).resetPassword(any(), anyString());
        verify(refreshTokenService, never()).revokeAllForUser(any());
    }

    @Test
    void shortPasswordFailsStartup() {
        when(userService.findByEmail(ADMIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> initializer("short").ensureAdmin())
                .isInstanceOf(IllegalStateException.class);
    }
}
