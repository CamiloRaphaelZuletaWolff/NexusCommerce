package com.nexuscommerce.catalog.api.dto;

import com.nexuscommerce.catalog.domain.Brand;
import java.time.Instant;

public record BrandResponse(String id, String slug, String name, String description, Instant createdAt) {

    public static BrandResponse from(Brand brand) {
        return new BrandResponse(brand.getId(), brand.getSlug(), brand.getName(),
                brand.getDescription(), brand.getCreatedAt());
    }
}
