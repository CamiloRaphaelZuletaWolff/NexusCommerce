package com.nexuscommerce.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** Read-only mapping of the {@code v_order_summary} SQL view (uses a scalar subquery). */
@Entity
@Immutable
@Table(name = "v_order_summary")
@Getter
@NoArgsConstructor
public class OrderSummaryView {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "status")
    private String status;

    @Column(name = "total")
    private BigDecimal total;

    @Column(name = "item_count")
    private long itemCount;

    @Column(name = "created_at")
    private Instant createdAt;
}
