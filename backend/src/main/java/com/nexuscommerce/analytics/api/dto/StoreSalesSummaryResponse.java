package com.nexuscommerce.analytics.api.dto;

import java.math.BigDecimal;

/** Result of the {@code fn_store_sales_summary} stored function. */
public record StoreSalesSummaryResponse(long ordersCount, long unitsSold, BigDecimal revenue) {
}
