package com.nexuscommerce.bootstrap;

import com.nexuscommerce.stores.api.dto.StoreCreateRequest;
import com.nexuscommerce.stores.domain.StoreType;
import com.nexuscommerce.stores.repository.StoreRepository;
import com.nexuscommerce.stores.service.StoreService;
import com.nexuscommerce.users.domain.User;
import com.nexuscommerce.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds demo data for local development only.
 *
 * <p>DEV-ONLY credentials (documented in MANUAL_SETUP_GUIDE.md):
 * admin@nexus.local / manager@nexus.local / customer@nexus.local, password {@code Password123!}.
 * Never enabled outside the {@code dev} profile.
 */
@Component
@Profile("dev")
@Order(1)
@RequiredArgsConstructor
public class DevDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);
    private static final String DEMO_PASSWORD = "Password123!";

    private final UserService userService;
    private final StoreService storeService;
    private final StoreRepository storeRepository;

    @Override
    public void run(ApplicationArguments args) {
        ensureUser("admin@nexus.local", "Platform Admin", "ADMIN");
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

    private User ensureUser(String email, String fullName, String role) {
        return userService.findByEmail(email).orElseGet(() -> {
            User created = userService.register(email, fullName, DEMO_PASSWORD, role);
            log.info("Seeded demo user {} [{}]", email, role);
            return created;
        });
    }
}
