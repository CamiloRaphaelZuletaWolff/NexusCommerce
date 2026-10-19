package com.nexuscommerce.cart.api.dto;

import com.nexuscommerce.cart.domain.CartItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(@Min(1) @Max(CartItem.MAX_QUANTITY) int quantity) {
}
