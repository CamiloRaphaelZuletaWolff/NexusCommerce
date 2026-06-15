package com.nexuscommerce.catalog.domain;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Minimal catalog metadata document.
 *
 * <p>Phase 2 uses this collection purely to validate real MongoDB read/write
 * integration ahead of the full catalog domain in Phase 3.
 */
@Document(collection = "catalog_meta")
@Getter
@NoArgsConstructor
public class CatalogMeta {

    @Id
    private String id;
    private String key;
    private Instant updatedAt;
    private String note;

    public CatalogMeta(String id, String key, Instant updatedAt, String note) {
        this.id = id;
        this.key = key;
        this.updatedAt = updatedAt;
        this.note = note;
    }
}
