package com.nexuscommerce.catalog.api.dto;

import com.nexuscommerce.catalog.domain.Variant;
import java.math.BigDecimal;
import java.util.Map;

public record VariantDto(String sku, Map<String, Object> attributes, BigDecimal priceOverride, Integer stock) {

    public static VariantDto from(Variant variant) {
        return new VariantDto(variant.getSku(), variant.getAttributes(),
                variant.getPriceOverride(), variant.getStock());
    }

    public Variant toDomain() {
        return new Variant(sku, attributes, priceOverride, stock);
    }
}
