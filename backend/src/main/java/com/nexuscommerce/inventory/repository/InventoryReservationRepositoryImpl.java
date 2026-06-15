package com.nexuscommerce.inventory.repository;

import jakarta.persistence.EntityManager;
import java.sql.CallableStatement;
import java.util.UUID;
import org.hibernate.Session;

/**
 * Calls the {@code sp_reserve_inventory} PostgreSQL stored procedure on the current
 * transactional connection. A RAISE in the procedure surfaces as a SQL exception
 * and rolls back the surrounding checkout transaction (ACID).
 */
public class InventoryReservationRepositoryImpl implements InventoryReservationRepository {

    private final EntityManager entityManager;

    public InventoryReservationRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void reserve(UUID productId, int quantity) {
        entityManager.unwrap(Session.class).doWork(connection -> {
            try (CallableStatement statement = connection.prepareCall("CALL sp_reserve_inventory(?, ?)")) {
                statement.setObject(1, productId);
                statement.setInt(2, quantity);
                statement.execute();
            }
        });
    }
}
