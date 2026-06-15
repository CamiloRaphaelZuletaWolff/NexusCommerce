package com.nexuscommerce.analytics.api.dto;

import com.nexuscommerce.analytics.domain.TopProductView;
import java.math.BigDecimal;
import java.util.UUID;

public record TopProductResponse(UUID productId, UUID storeId, String name, long unitsSold, BigDecimal revenue) {

    public static TopProductResponse from(TopProductView view) {
        return new TopProductResponse(view.getProductId(), view.getStoreId(), view.getName(),
                view.getUnitsSold(), view.getRevenue());
    }
}
