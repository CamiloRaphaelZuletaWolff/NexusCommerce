package com.nexuscommerce.orders.api.dto;

import com.nexuscommerce.orders.domain.OrderItem;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        UUID storeId,
        String sku,
        String name,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getId(), item.getProductId(), item.getStoreId(),
                item.getSku(), item.getName(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal());
    }
}
