package com.nexuscommerce.catalog.api.dto;

import com.nexuscommerce.catalog.domain.Product;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProductResponse(
        String id,
        String storeId,
        String sku,
        String name,
        String slug,
        String description,
        BigDecimal price,
        String currency,
        String categorySlug,
        List<String> categoryLabels,
        List<String> tags,
        String brand,
        List<VariantDto> variants,
        Map<String, Object> attributes,
        String status,
        double ratingAverage,
        int ratingCount,
        Instant createdAt,
        Instant updatedAt) {

    public static ProductResponse from(Product product) {
        List<VariantDto> variants = product.getVariants() == null ? List.of()
                : product.getVariants().stream().map(VariantDto::from).toList();
        return new ProductResponse(
                product.getId(),
                product.getStoreId(),
                product.getSku(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                product.getCategorySlug(),
                product.getCategoryLabels(),
                product.getTags(),
                product.getBrand(),
                variants,
                product.getAttributes(),
                product.getStatus() == null ? null : product.getStatus().name(),
                product.getRatingAverage(),
                product.getRatingCount(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
