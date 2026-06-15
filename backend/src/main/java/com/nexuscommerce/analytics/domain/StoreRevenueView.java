package com.nexuscommerce.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** Read-only mapping of the {@code v_store_revenue} SQL view. */
@Entity
@Immutable
@Table(name = "v_store_revenue")
@Getter
@NoArgsConstructor
public class StoreRevenueView {

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "orders_count")
    private long ordersCount;

    @Column(name = "units_sold")
    private long unitsSold;

    @Column(name = "revenue")
    private BigDecimal revenue;
}
