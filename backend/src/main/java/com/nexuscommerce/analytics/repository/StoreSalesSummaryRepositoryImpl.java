package com.nexuscommerce.analytics.repository;

import com.nexuscommerce.analytics.api.dto.StoreSalesSummaryResponse;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;

/** Invokes the PostgreSQL reporting function {@code fn_store_sales_summary(uuid)}. */
public class StoreSalesSummaryRepositoryImpl implements StoreSalesSummaryRepository {

    private final EntityManager entityManager;

    public StoreSalesSummaryRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public StoreSalesSummaryResponse salesSummary(UUID storeId) {
        Object[] row = (Object[]) entityManager.createNativeQuery(
                        "SELECT orders_count, units_sold, revenue "
                                + "FROM fn_store_sales_summary(CAST(:storeId AS uuid))")
                .setParameter("storeId", storeId.toString())
                .getSingleResult();
        long ordersCount = ((Number) row[0]).longValue();
        long unitsSold = ((Number) row[1]).longValue();
        BigDecimal revenue = row[2] == null ? BigDecimal.ZERO : (BigDecimal) row[2];
        return new StoreSalesSummaryResponse(ordersCount, unitsSold, revenue);
    }
}
