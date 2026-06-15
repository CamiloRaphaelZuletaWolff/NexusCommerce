package com.nexuscommerce.catalog.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/** Contributes catalog-specific MongoDB health to {@code /actuator/health} (as "mongoCatalog"). */
@Component("mongoCatalog")
@RequiredArgsConstructor
public class MongoCatalogHealthIndicator implements HealthIndicator {

    private static final String COLLECTION = "catalog_meta";

    private final MongoTemplate mongoTemplate;

    @Override
    public Health health() {
        try {
            long documents = mongoTemplate.getCollection(COLLECTION).estimatedDocumentCount();
            return Health.up()
                    .withDetail("database", mongoTemplate.getDb().getName())
                    .withDetail("collection", COLLECTION)
                    .withDetail("documents", documents)
                    .build();
        } catch (RuntimeException ex) {
            return Health.down(ex).build();
        }
    }
}
