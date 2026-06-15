package com.nexuscommerce.analytics.api.dto;

import com.nexuscommerce.analytics.domain.StoreRevenueView;
import java.math.BigDecimal;
import java.util.UUID;

public record StoreRevenueResponse(UUID storeId, long ordersCount, long unitsSold, BigDecimal revenue) {

    public static StoreRevenueResponse from(StoreRevenueView view) {
        return new StoreRevenueResponse(view.getStoreId(), view.getOrdersCount(),
                view.getUnitsSold(), view.getRevenue());
    }
}
