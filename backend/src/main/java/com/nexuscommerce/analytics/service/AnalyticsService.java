package com.nexuscommerce.analytics.service;

import com.nexuscommerce.analytics.api.dto.OrderSummaryResponse;
import com.nexuscommerce.analytics.api.dto.StoreRevenueResponse;
import com.nexuscommerce.analytics.api.dto.StoreSalesSummaryResponse;
import com.nexuscommerce.analytics.api.dto.TopProductResponse;
import com.nexuscommerce.analytics.repository.OrderSummaryViewRepository;
import com.nexuscommerce.analytics.repository.StoreRevenueViewRepository;
import com.nexuscommerce.analytics.repository.TopProductViewRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final StoreRevenueViewRepository storeRevenueViewRepository;
    private final TopProductViewRepository topProductViewRepository;
    private final OrderSummaryViewRepository orderSummaryViewRepository;

    @Transactional(readOnly = true)
    public List<StoreRevenueResponse> storeRevenue() {
        return storeRevenueViewRepository.findAllByOrderByRevenueDesc().stream()
                .map(StoreRevenueResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<StoreRevenueResponse> storesAboveAverageRevenue() {
        return storeRevenueViewRepository.findAboveAverageRevenue().stream()
                .map(StoreRevenueResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TopProductResponse> topProducts(int limit) {
        return topProductViewRepository.findAllByOrderByUnitsSoldDesc(PageRequest.of(0, limit)).stream()
                .map(TopProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StoreSalesSummaryResponse storeSalesSummary(UUID storeId) {
        return storeRevenueViewRepository.salesSummary(storeId);
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> recentOrders(int limit) {
        return orderSummaryViewRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).stream()
                .map(OrderSummaryResponse::from).toList();
    }
}
