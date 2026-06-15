package com.nexuscommerce.analytics.repository;

import com.nexuscommerce.analytics.domain.StoreRevenueView;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoreRevenueViewRepository
        extends JpaRepository<StoreRevenueView, UUID>, StoreSalesSummaryRepository {

    List<StoreRevenueView> findAllByOrderByRevenueDesc();

    /** Stores whose revenue exceeds the platform average (demonstrates a subquery). */
    @Query(value = "SELECT * FROM v_store_revenue "
            + "WHERE revenue > (SELECT AVG(revenue) FROM v_store_revenue)", nativeQuery = true)
    List<StoreRevenueView> findAboveAverageRevenue();
}
