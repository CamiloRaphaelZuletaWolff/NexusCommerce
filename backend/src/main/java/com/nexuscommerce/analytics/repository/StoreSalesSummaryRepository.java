package com.nexuscommerce.analytics.repository;

import com.nexuscommerce.analytics.api.dto.StoreSalesSummaryResponse;
import java.util.UUID;

/** Custom fragment calling the {@code fn_store_sales_summary} stored function. */
public interface StoreSalesSummaryRepository {

    StoreSalesSummaryResponse salesSummary(UUID storeId);
}
