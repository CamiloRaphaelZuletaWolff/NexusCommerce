package com.nexuscommerce.catalog.domain;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Customer catalog profile (recommendation metadata), kept in MongoDB.
 *
 * <p>{@code id} equals the customer's PostgreSQL user UUID — this document is
 * synchronized from the relational user via that shared UUID (the cross-database
 * bridge, requirement #11 / ADR-0003).
 */
@Document("customer_profiles")
@Getter
@Setter
@NoArgsConstructor
public class CustomerProfile {

    @Id
    private String id; // == PostgreSQL user UUID

    private String email;
    private String fullName;
    private List<String> favoriteCategories;
    private List<String> favoriteBrands;
    private List<String> recentlyViewedProductIds;
    private Instant createdAt;
    private Instant updatedAt;
}
