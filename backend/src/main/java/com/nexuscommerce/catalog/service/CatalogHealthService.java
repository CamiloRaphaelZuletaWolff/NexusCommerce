package com.nexuscommerce.catalog.service;

import com.nexuscommerce.catalog.api.dto.CatalogHealthResponse;
import com.nexuscommerce.catalog.domain.CatalogMeta;
import com.nexuscommerce.catalog.repository.CatalogMetaRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/** Validates MongoDB integration by performing a real write -> read -> delete round-trip. */
@Service
@RequiredArgsConstructor
public class CatalogHealthService {

    private static final Logger log = LoggerFactory.getLogger(CatalogHealthService.class);
    private static final String COLLECTION = "catalog_meta";

    private final CatalogMetaRepository catalogMetaRepository;
    private final MongoTemplate mongoTemplate;

    public CatalogHealthResponse probe() {
        long startNanos = System.nanoTime();
        String probeId = "probe-" + UUID.randomUUID();
        try {
            catalogMetaRepository.save(new CatalogMeta(probeId, "health-probe", Instant.now(), "round-trip"));
            catalogMetaRepository.findById(probeId)
                    .orElseThrow(() -> new IllegalStateException("Probe document missing after write"));
            catalogMetaRepository.deleteById(probeId);
            return new CatalogHealthResponse("UP", databaseName(), COLLECTION, elapsedMillis(startNanos), null);
        } catch (RuntimeException ex) {
            log.warn("Catalog Mongo health probe failed", ex);
            return new CatalogHealthResponse("DOWN", databaseName(), COLLECTION, elapsedMillis(startNanos),
                    ex.getMessage());
        }
    }

    private String databaseName() {
        try {
            return mongoTemplate.getDb().getName();
        } catch (RuntimeException ex) {
            return "unknown";
        }
    }

    private static long elapsedMillis(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
    }
}
