package com.nexuscommerce.catalog;

import com.nexuscommerce.catalog.domain.CatalogMeta;
import com.nexuscommerce.catalog.repository.CatalogMetaRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/** Ensures the minimal {@code catalog_meta} collection exists and carries a marker document. */
@Component
@RequiredArgsConstructor
public class CatalogInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogInitializer.class);
    private static final String COLLECTION = "catalog_meta";

    private final MongoTemplate mongoTemplate;
    private final CatalogMetaRepository catalogMetaRepository;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        if (!mongoTemplate.collectionExists(COLLECTION)) {
            mongoTemplate.createCollection(COLLECTION);
            log.info("Created MongoDB collection '{}'", COLLECTION);
        }
        // Idempotent marker document (fixed id => upsert on restart).
        catalogMetaRepository.save(new CatalogMeta(
                "catalog-initialized", "bootstrap", Instant.now(),
                "Catalog metadata collection initialized by NexusCommerce backend"));
    }
}
