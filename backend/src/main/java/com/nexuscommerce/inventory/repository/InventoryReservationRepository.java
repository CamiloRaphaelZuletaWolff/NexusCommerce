package com.nexuscommerce.inventory.repository;

import java.util.UUID;

/** Custom fragment: reserve stock via the {@code sp_reserve_inventory} stored procedure. */
public interface InventoryReservationRepository {

    /** Atomically decrements available stock; throws if missing/insufficient (rolls back the tx). */
    void reserve(UUID productId, int quantity);
}
