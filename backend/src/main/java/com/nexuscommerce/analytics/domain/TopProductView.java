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

/** Read-only mapping of the {@code v_top_products} SQL view. */
@Entity
@Immutable
@Table(name = "v_top_products")
@Getter
@NoArgsConstructor
public class TopProductView {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "name")
    private String name;

    @Column(name = "units_sold")
    private long unitsSold;

    @Column(name = "revenue")
    private BigDecimal revenue;
}
