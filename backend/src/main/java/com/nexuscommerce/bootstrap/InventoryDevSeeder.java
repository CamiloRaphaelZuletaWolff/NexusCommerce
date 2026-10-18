package com.nexuscommerce.bootstrap;

import com.nexuscommerce.catalog.repository.ProductRepository;
import com.nexuscommerce.inventory.domain.InventoryItem;
import com.nexuscommerce.inventory.repository.InventoryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Backfills transactional inventory (PostgreSQL) for every catalog product (MongoDB)
 * that lacks a stock record — keyed by the shared product UUID. Idempotent; runs
 * after the catalog seeder so it covers both fresh and pre-existing catalogs.
 */
@Component
@Profile({"dev", "demo"})
@Order(3)
@RequiredArgsConstructor
public class InventoryDevSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InventoryDevSeeder.class);
    private static final int DEFAULT_STOCK = 100;

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<InventoryItem> toCreate = productRepository.findAll().stream()
                .map(product -> {
                    UUID productId = UUID.fromString(product.getId());
                    if (inventoryRepository.existsByProductId(productId)) {
                        return null;
                    }
                    return new InventoryItem(UUID.fromString(product.getStoreId()),
                            productId, product.getSku(), DEFAULT_STOCK);
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        if (!toCreate.isEmpty()) {
            inventoryRepository.saveAll(toCreate);
            log.info("Seeded inventory for {} products", toCreate.size());
        }
    }
}
