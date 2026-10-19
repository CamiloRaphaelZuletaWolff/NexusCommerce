package com.nexuscommerce.bootstrap;

import com.nexuscommerce.auth.refresh.RefreshTokenService;
import com.nexuscommerce.stores.api.dto.StoreCreateRequest;
import com.nexuscommerce.stores.domain.StoreType;
import com.nexuscommerce.stores.repository.StoreRepository;
import com.nexuscommerce.stores.service.StoreService;
import com.nexuscommerce.users.domain.User;
import com.nexuscommerce.users.service.UserService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Seeds demo data for local development and the public demo deployment.
 *
 * <p>Well-known DEMO credentials (documented in README.md / DEPLOY.md):
 * manager@nexus.local / customer@nexus.local, password {@code Password123!}.
 *
 * <p>The ADMIN account is different: on a public demo a published admin password means
 * anyone is admin. Its password comes from {@code app.demo.admin-password}
 * ({@code DEMO_ADMIN_PASSWORD}); the {@code dev} profile defaults it to the same local
 * password, the {@code demo} profile has no default. If it is unset the admin is not
 * created, and an admin left over from an earlier deploy is disabled. When it is set,
 * an existing admin's password is re-synced to it (and its sessions revoked), so
 * rotating the env var and redeploying is all it takes to change it.
 *
 * <p>Only enabled under the {@code dev} and {@code demo} profiles — never in a real production profile.
 */
@Component
@Profile({"dev", "demo"})
@Order(1)
@RequiredArgsConstructor
public class DevDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);
    private static final String DEMO_PASSWORD = "Password123!";
    private static final String ADMIN_EMAIL = "admin@nexus.local";
    static final int MIN_ADMIN_PASSWORD_LENGTH = 12;

    private final UserService userService;
    private final StoreService storeService;
    private final StoreRepository storeRepository;
    private final RefreshTokenService refreshTokenService;
    private final DemoProperties demoProperties;

    @Override
    public void run(ApplicationArguments args) {
        ensureAdmin();
        User manager = ensureUser("manager@nexus.local", "Store Manager", "STORE_MANAGER");
        ensureUser("customer@nexus.local", "Demo Customer", "CUSTOMER");

        if (storeRepository.count() == 0) {
            storeService.create(new StoreCreateRequest(
                    "Nexus Electronics", "Phones, laptops and gadgets", StoreType.ELECTRONICS),
                    manager.getId(), null);
            storeService.create(new StoreCreateRequest(
                    "Nexus Fashion", "Apparel and accessories", StoreType.FASHION),
                    manager.getId(), null);
            log.info("Seeded demo stores");
        }
    }

    void ensureAdmin() {
        String password = demoProperties.adminPassword();
        Optional<User> existing = userService.findByEmail(ADMIN_EMAIL);

        if (!StringUtils.hasText(password)) {
            existing.filter(User::isEnabled).ifPresentOrElse(admin -> {
                userService.disable(admin.getId());
                refreshTokenService.revokeAllForUser(admin.getId());
                log.warn("DEMO_ADMIN_PASSWORD is not set: disabled existing demo admin {}", ADMIN_EMAIL);
            }, () -> log.warn("DEMO_ADMIN_PASSWORD is not set: demo admin {} not available", ADMIN_EMAIL));
            return;
        }
        if (password.length() < MIN_ADMIN_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    "app.demo.admin-password (DEMO_ADMIN_PASSWORD) must be at least "
                            + MIN_ADMIN_PASSWORD_LENGTH + " characters");
        }

        existing.ifPresentOrElse(admin -> {
            if (!admin.isEnabled() || !userService.passwordMatches(admin, password)) {
                userService.resetPassword(admin.getId(), password);
                refreshTokenService.revokeAllForUser(admin.getId());
                log.info("Synced demo admin {} password from configuration", ADMIN_EMAIL);
            }
        }, () -> {
            userService.register(ADMIN_EMAIL, "Platform Admin", password, "ADMIN");
            log.info("Seeded demo user {} [ADMIN]", ADMIN_EMAIL);
        });
    }

    private User ensureUser(String email, String fullName, String role) {
        return userService.findByEmail(email).orElseGet(() -> {
            User created = userService.register(email, fullName, DEMO_PASSWORD, role);
            log.info("Seeded demo user {} [{}]", email, role);
            return created;
        });
    }
}
