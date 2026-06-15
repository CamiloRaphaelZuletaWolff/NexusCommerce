package com.nexuscommerce.catalog.api.dto;

import com.nexuscommerce.catalog.domain.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductCreateRequest(
        @NotNull UUID storeId,
        @NotBlank @Size(max = 80) String sku,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 5000) String description,
        @NotNull @PositiveOrZero BigDecimal price,
        @Size(max = 3) String currency,
        @NotBlank String categorySlug,
        List<String> categoryLabels,
        List<String> tags,
        String brand,
        List<VariantDto> variants,
        Map<String, Object> attributes,
        ProductStatus status) {
}
