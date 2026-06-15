package com.nexuscommerce.catalog.repository;

import com.nexuscommerce.catalog.domain.ProductStatus;
import java.math.BigDecimal;
import java.util.List;

/** Immutable filter for product search. Null/empty fields are ignored. */
public record ProductSearchCriteria(
        String storeId,
        String q,
        List<String> categories,
        List<String> tags,
        List<String> brands,
        List<String> variantSkus,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        ProductStatus status) {
}
