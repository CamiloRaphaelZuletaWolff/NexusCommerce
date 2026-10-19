package com.nexuscommerce.analytics.api;

import com.nexuscommerce.analytics.api.dto.OrderSummaryResponse;
import com.nexuscommerce.analytics.api.dto.StoreRevenueResponse;
import com.nexuscommerce.analytics.api.dto.StoreSalesSummaryResponse;
import com.nexuscommerce.analytics.api.dto.TopProductResponse;
import com.nexuscommerce.analytics.service.AnalyticsService;
import com.nexuscommerce.security.AppPrincipal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reporting over SQL views, a stored function, and a subquery. Restricted to staff:
 * store managers only see their own stores; the cross-customer order feed is admin-only.
 */
@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
@RequiredArgsConstructor
public class AnalyticsController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final AnalyticsService analyticsService;

    @GetMapping("/store-revenue")
    public List<StoreRevenueResponse> storeRevenue(Authentication authentication) {
        return analyticsService.storeRevenue(userId(authentication), isAdmin(authentication));
    }

    @GetMapping("/store-revenue/above-average")
    public List<StoreRevenueResponse> aboveAverageRevenue(Authentication authentication) {
        return analyticsService.storesAboveAverageRevenue(userId(authentication), isAdmin(authentication));
    }

    @GetMapping("/top-products")
    public List<TopProductResponse> topProducts(@RequestParam(defaultValue = "10") int limit,
                                                Authentication authentication) {
        return analyticsService.topProducts(limit, userId(authentication), isAdmin(authentication));
    }

    @GetMapping("/stores/{storeId}/summary")
    public StoreSalesSummaryResponse storeSummary(@PathVariable UUID storeId, Authentication authentication) {
        return analyticsService.storeSalesSummary(storeId, userId(authentication), isAdmin(authentication));
    }

    @GetMapping("/orders-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderSummaryResponse> recentOrders(@RequestParam(defaultValue = "20") int limit) {
        return analyticsService.recentOrders(limit);
    }

    private static UUID userId(Authentication authentication) {
        return ((AppPrincipal) authentication.getPrincipal()).userId();
    }

    private static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }
}
