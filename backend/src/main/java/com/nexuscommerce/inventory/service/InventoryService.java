package com.nexuscommerce.inventory.service;

import com.nexuscommerce.inventory.domain.InventoryItem;
import com.nexuscommerce.inventory.repository.InventoryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /** Reserve stock for a product. Must run inside the caller's transaction (e.g. checkout). */
    public void reserve(UUID productId, int quantity) {
        inventoryRepository.reserve(productId, quantity);
    }

    @Transactional
    public InventoryItem upsertStock(UUID storeId, UUID productId, String sku, int availableQuantity) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> new InventoryItem(storeId, productId, sku, 0));
        item.setStoreId(storeId);
        item.setSku(sku);
        item.setAvailableQuantity(availableQuantity);
        item.setUpdatedAt(java.time.Instant.now());
        return inventoryRepository.save(item);
    }
}
