package com.nexuscommerce.catalog.api.dto;

import com.nexuscommerce.catalog.domain.ProductStatus;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Partial update — null fields are left unchanged. */
public record ProductUpdateRequest(
        @Size(max = 200) String name,
        @Size(max = 5000) String description,
        @PositiveOrZero BigDecimal price,
        @Size(max = 3) String currency,
        String categorySlug,
        List<String> categoryLabels,
        List<String> tags,
        String brand,
        List<VariantDto> variants,
        Map<String, Object> attributes,
        ProductStatus status) {
}
