package com.nexuscommerce.analytics.repository;

import com.nexuscommerce.analytics.domain.OrderSummaryView;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSummaryViewRepository extends JpaRepository<OrderSummaryView, UUID> {

    List<OrderSummaryView> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
