package com.nexuscommerce.cart.api.dto;

import com.nexuscommerce.cart.domain.CartItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Only product + quantity are client-supplied; price/name/store are snapshotted server-side. */
public record AddCartItemRequest(
        @NotBlank @Size(max = 64) String productId,
        @Min(1) @Max(CartItem.MAX_QUANTITY) int quantity) {
}
