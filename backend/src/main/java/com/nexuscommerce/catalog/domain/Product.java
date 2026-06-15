package com.nexuscommerce.catalog.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Catalog product document (MongoDB).
 *
 * <p>{@code id} is the product's UUID (business key == {@code _id}); {@code storeId}
 * is the UUID of the owning PostgreSQL store — the cross-database link (ADR-0003).
 * {@code attributes} holds category-specific dynamic fields; {@code tags},
 * {@code categoryLabels} and {@code variants} support array-based queries.
 */
@Document("products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    private String id;

    @Indexed
    private String storeId;

    @Indexed
    private String sku;

    private String name;

    @Indexed
    private String slug;

    private String description;

    private BigDecimal price;
    private String currency;

    @Indexed
    private String categorySlug;

    private List<String> categoryLabels;
    private List<String> tags;

    @Indexed
    private String brand;

    private List<Variant> variants;

    /** Category-specific dynamic attributes (e.g. {"ramGb": 16, "cpu": "M3"}). */
    private Map<String, Object> attributes;

    @Indexed
    private ProductStatus status;

    private double ratingAverage;
    private int ratingCount;

    private Instant createdAt;
    private Instant updatedAt;
}
