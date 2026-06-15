package com.nexuscommerce.analytics.api.dto;

import com.nexuscommerce.analytics.domain.OrderSummaryView;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID orderId,
        String orderNumber,
        UUID userId,
        String status,
        BigDecimal total,
        long itemCount,
        Instant createdAt) {

    public static OrderSummaryResponse from(OrderSummaryView view) {
        return new OrderSummaryResponse(view.getOrderId(), view.getOrderNumber(), view.getUserId(),
                view.getStatus(), view.getTotal(), view.getItemCount(), view.getCreatedAt());
    }
}
