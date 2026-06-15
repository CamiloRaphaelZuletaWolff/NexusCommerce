package com.nexuscommerce.integration;

import com.nexuscommerce.catalog.domain.CustomerProfile;
import com.nexuscommerce.catalog.repository.CustomerProfileRepository;
import com.nexuscommerce.users.event.UserRegisteredEvent;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Synchronizes customer data from PostgreSQL into MongoDB, keyed by the shared user
 * UUID (requirement #11 / ADR-0003).
 *
 * <p>Runs only after the registering transaction commits, so a rolled-back
 * registration never leaves an orphan Mongo profile. Failures here are logged and
 * never propagate (eventual consistency).
 */
@Component
@RequiredArgsConstructor
public class CustomerProfileSyncListener {

    private static final Logger log = LoggerFactory.getLogger(CustomerProfileSyncListener.class);

    private final CustomerProfileRepository customerProfileRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        try {
            String id = event.userId().toString();
            CustomerProfile profile = customerProfileRepository.findById(id).orElseGet(CustomerProfile::new);
            boolean isNew = profile.getCreatedAt() == null;

            profile.setId(id);
            profile.setEmail(event.email());
            profile.setFullName(event.fullName());
            if (isNew) {
                profile.setCreatedAt(Instant.now());
                profile.setFavoriteCategories(List.of());
                profile.setFavoriteBrands(List.of());
                profile.setRecentlyViewedProductIds(List.of());
            }
            profile.setUpdatedAt(Instant.now());

            customerProfileRepository.save(profile);
            log.debug("Synced MongoDB customer profile for user {}", id);
        } catch (RuntimeException ex) {
            log.warn("Failed to sync customer profile for user {}", event.userId(), ex);
        }
    }
}
