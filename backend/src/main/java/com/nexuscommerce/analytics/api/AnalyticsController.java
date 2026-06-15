package com.nexuscommerce.analytics.api;

import com.nexuscommerce.analytics.api.dto.OrderSummaryResponse;
import com.nexuscommerce.analytics.api.dto.StoreRevenueResponse;
import com.nexuscommerce.analytics.api.dto.StoreSalesSummaryResponse;
import com.nexuscommerce.analytics.api.dto.TopProductResponse;
import com.nexuscommerce.analytics.service.AnalyticsService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Reporting over SQL views, a stored function, and a subquery. Restricted to staff. */
@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/store-revenue")
    public List<StoreRevenueResponse> storeRevenue() {
        return analyticsService.storeRevenue();
    }

    @GetMapping("/store-revenue/above-average")
    public List<StoreRevenueResponse> aboveAverageRevenue() {
        return analyticsService.storesAboveAverageRevenue();
    }

    @GetMapping("/top-products")
    public List<TopProductResponse> topProducts(@RequestParam(defaultValue = "10") int limit) {
        return analyticsService.topProducts(limit);
    }

    @GetMapping("/stores/{storeId}/summary")
    public StoreSalesSummaryResponse storeSummary(@PathVariable UUID storeId) {
        return analyticsService.storeSalesSummary(storeId);
    }

    @GetMapping("/orders-summary")
    public List<OrderSummaryResponse> recentOrders(@RequestParam(defaultValue = "20") int limit) {
        return analyticsService.recentOrders(limit);
    }
}
