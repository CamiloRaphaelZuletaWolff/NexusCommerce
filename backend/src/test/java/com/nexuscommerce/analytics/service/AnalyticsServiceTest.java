package com.nexuscommerce.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexuscommerce.analytics.api.dto.StoreRevenueResponse;
import com.nexuscommerce.analytics.domain.StoreRevenueView;
import com.nexuscommerce.analytics.repository.OrderSummaryViewRepository;
import com.nexuscommerce.analytics.repository.StoreRevenueViewRepository;
import com.nexuscommerce.analytics.repository.TopProductViewRepository;
import com.nexuscommerce.stores.api.dto.StoreResponse;
import com.nexuscommerce.stores.domain.StoreStatus;
import com.nexuscommerce.stores.domain.StoreType;
import com.nexuscommerce.stores.service.StoreService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private StoreRevenueViewRepository storeRevenueViewRepository;
    @Mock private TopProductViewRepository topProductViewRepository;
    @Mock private OrderSummaryViewRepository orderSummaryViewRepository;
    @Mock private StoreService storeService;

    @InjectMocks private AnalyticsService analyticsService;

    private final UUID managerId = UUID.randomUUID();
    private final UUID ownStore = UUID.randomUUID();
    private final UUID otherStore = UUID.randomUUID();

    private static StoreRevenueView revenue(UUID storeId, String amount) {
        StoreRevenueView view = new StoreRevenueView();
        ReflectionTestUtils.setField(view, "storeId", storeId);
        ReflectionTestUtils.setField(view, "revenue", new BigDecimal(amount));
        return view;
    }

    @Test
    void managerOnlySeesRevenueOfOwnedStores() {
        when(storeRevenueViewRepository.findAllByOrderByRevenueDesc())
                .thenReturn(List.of(revenue(otherStore, "900.00"), revenue(ownStore, "100.00")));
        when(storeService.ownedStoreIds(managerId)).thenReturn(Set.of(ownStore));

        List<StoreRevenueResponse> rows = analyticsService.storeRevenue(managerId, false);

        assertThat(rows).extracting(StoreRevenueResponse::storeId).containsExactly(ownStore);
    }

    @Test
    void adminSeesEveryStore() {
        when(storeRevenueViewRepository.findAllByOrderByRevenueDesc())
                .thenReturn(List.of(revenue(otherStore, "900.00"), revenue(ownStore, "100.00")));

        assertThat(analyticsService.storeRevenue(UUID.randomUUID(), true)).hasSize(2);
        verify(storeService, never()).ownedStoreIds(any());
    }

    @Test
    void managerCannotReadAnotherStoresSummary() {
        when(storeService.getByIdOrThrow(otherStore)).thenReturn(new StoreResponse(otherStore, UUID.randomUUID(),
                "Other", "other", null, StoreType.FASHION, StoreStatus.ACTIVE, null, null));

        assertThatThrownBy(() -> analyticsService.storeSalesSummary(otherStore, managerId, false))
                .isInstanceOf(AccessDeniedException.class);
        verify(storeRevenueViewRepository, never()).salesSummary(any());
    }

    @Test
    void managerTopProductsAreScopedAndLimitIsClamped() {
        when(storeService.ownedStoreIds(managerId)).thenReturn(Set.of(ownStore));
        when(topProductViewRepository.findByStoreIdInOrderByUnitsSoldDesc(Set.of(ownStore),
                PageRequest.of(0, AnalyticsService.MAX_LIMIT))).thenReturn(List.of());

        assertThat(analyticsService.topProducts(1_000_000, managerId, false)).isEmpty();
        verify(topProductViewRepository, never()).findAllByOrderByUnitsSoldDesc(any());
    }

    @Test
    void managerWithoutStoresGetsNoTopProducts() {
        when(storeService.ownedStoreIds(managerId)).thenReturn(Set.of());

        assertThat(analyticsService.topProducts(10, managerId, false)).isEmpty();
        verify(topProductViewRepository, never()).findByStoreIdInOrderByUnitsSoldDesc(any(), any());
    }
}
