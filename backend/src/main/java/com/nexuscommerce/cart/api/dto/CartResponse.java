package com.nexuscommerce.cart.api.dto;

import com.nexuscommerce.cart.domain.Cart;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        String status,
        List<CartItemResponse> items,
        int itemCount,
        BigDecimal subtotal) {

    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int itemCount = items.stream().mapToInt(CartItemResponse::quantity).sum();
        return new CartResponse(cart.getId(), cart.getStatus().name(), items, itemCount, subtotal);
    }
}
