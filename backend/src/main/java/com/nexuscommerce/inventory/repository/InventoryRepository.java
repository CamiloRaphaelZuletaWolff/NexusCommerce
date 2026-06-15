package com.nexuscommerce.inventory.repository;

import com.nexuscommerce.inventory.domain.InventoryItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<InventoryItem, UUID>, InventoryReservationRepository {

    Optional<InventoryItem> findByProductId(UUID productId);

    boolean existsByProductId(UUID productId);
}
