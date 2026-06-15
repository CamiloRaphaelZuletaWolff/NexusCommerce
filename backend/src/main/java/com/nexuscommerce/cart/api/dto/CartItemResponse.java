package com.nexuscommerce.cart.api.dto;

import com.nexuscommerce.cart.domain.CartItem;
import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        UUID storeId,
        String sku,
        String name,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal) {

    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(item.getId(), item.getProductId(), item.getStoreId(),
                item.getSku(), item.getName(), item.getUnitPrice(), item.getQuantity(), item.lineTotal());
    }
}
