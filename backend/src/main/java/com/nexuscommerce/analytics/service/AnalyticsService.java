package com.nexuscommerce.analytics.service;

import com.nexuscommerce.analytics.api.dto.OrderSummaryResponse;
import com.nexuscommerce.analytics.api.dto.StoreRevenueResponse;
import com.nexuscommerce.analytics.api.dto.StoreSalesSummaryResponse;
import com.nexuscommerce.analytics.api.dto.TopProductResponse;
import com.nexuscommerce.analytics.domain.StoreRevenueView;
import com.nexuscommerce.analytics.repository.OrderSummaryViewRepository;
import com.nexuscommerce.analytics.repository.StoreRevenueViewRepository;
import com.nexuscommerce.analytics.repository.TopProductViewRepository;
import com.nexuscommerce.stores.api.dto.StoreResponse;
import com.nexuscommerce.stores.service.StoreService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reporting over SQL views and a stored function.
 *
 * <p>Tenant scoping: an ADMIN sees the whole platform; a STORE_MANAGER only sees
 * figures for the stores they own (the same ownership rule as product/store writes).
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    /** Upper bound for the {@code limit} query parameters (keeps the queries cheap). */
    static final int MAX_LIMIT = 100;

    private final StoreRevenueViewRepository storeRevenueViewRepository;
    private final TopProductViewRepository topProductViewRepository;
    private final OrderSummaryViewRepository orderSummaryViewRepository;
    private final StoreService storeService;

    @Transactional(readOnly = true)
    public List<StoreRevenueResponse> storeRevenue(UUID actingUserId, boolean isAdmin) {
        return scoped(storeRevenueViewRepository.findAllByOrderByRevenueDesc(), actingUserId, isAdmin);
    }

    @Transactional(readOnly = true)
    public List<StoreRevenueResponse> storesAboveAverageRevenue(UUID actingUserId, boolean isAdmin) {
        return scoped(storeRevenueViewRepository.findAboveAverageRevenue(), actingUserId, isAdmin);
    }

    @Transactional(readOnly = true)
    public List<TopProductResponse> topProducts(int limit, UUID actingUserId, boolean isAdmin) {
        PageRequest page = PageRequest.of(0, clamp(limit));
        if (isAdmin) {
            return topProductViewRepository.findAllByOrderByUnitsSoldDesc(page).stream()
                    .map(TopProductResponse::from).toList();
        }
        Set<UUID> owned = storeService.ownedStoreIds(actingUserId);
        if (owned.isEmpty()) {
            return List.of();
        }
        return topProductViewRepository.findByStoreIdInOrderByUnitsSoldDesc(owned, page).stream()
                .map(TopProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StoreSalesSummaryResponse storeSalesSummary(UUID storeId, UUID actingUserId, boolean isAdmin) {
        StoreResponse store = storeService.getByIdOrThrow(storeId);
        if (!isAdmin && !store.ownerUserId().equals(actingUserId)) {
            throw new AccessDeniedException("You do not own this store");
        }
        return storeRevenueViewRepository.salesSummary(storeId);
    }

    /** Platform-wide order feed (every customer's orders): ADMIN only, enforced on the controller. */
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> recentOrders(int limit) {
        return orderSummaryViewRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, clamp(limit))).stream()
                .map(OrderSummaryResponse::from).toList();
    }

    private List<StoreRevenueResponse> scoped(List<StoreRevenueView> rows, UUID actingUserId, boolean isAdmin) {
        Set<UUID> owned = isAdmin ? null : storeService.ownedStoreIds(actingUserId);
        return rows.stream()
                .filter(row -> owned == null || owned.contains(row.getStoreId()))
                .map(StoreRevenueResponse::from)
                .toList();
    }

    private static int clamp(int limit) {
        return Math.max(1, Math.min(limit, MAX_LIMIT));
    }
}
