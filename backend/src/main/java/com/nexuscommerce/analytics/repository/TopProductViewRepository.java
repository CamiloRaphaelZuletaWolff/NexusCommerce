package com.nexuscommerce.analytics.repository;

import com.nexuscommerce.analytics.domain.TopProductView;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopProductViewRepository extends JpaRepository<TopProductView, UUID> {

    List<TopProductView> findAllByOrderByUnitsSoldDesc(Pageable pageable);

    List<TopProductView> findByStoreIdInOrderByUnitsSoldDesc(Collection<UUID> storeIds, Pageable pageable);
}
