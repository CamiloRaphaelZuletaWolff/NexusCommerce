package com.nexuscommerce.cart.api;

import com.nexuscommerce.cart.api.dto.AddCartItemRequest;
import com.nexuscommerce.cart.api.dto.CartResponse;
import com.nexuscommerce.cart.api.dto.UpdateCartItemRequest;
import com.nexuscommerce.cart.service.CartService;
import com.nexuscommerce.security.AppPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal AppPrincipal principal) {
        return cartService.getCartView(principal.userId());
    }

    @PostMapping("/items")
    public CartResponse addItem(@Valid @RequestBody AddCartItemRequest request,
                                @AuthenticationPrincipal AppPrincipal principal) {
        return cartService.addItem(principal.userId(), request);
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(@PathVariable UUID itemId,
                                   @Valid @RequestBody UpdateCartItemRequest request,
                                   @AuthenticationPrincipal AppPrincipal principal) {
        return cartService.updateItem(principal.userId(), itemId, request.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(@PathVariable UUID itemId,
                                   @AuthenticationPrincipal AppPrincipal principal) {
        return cartService.removeItem(principal.userId(), itemId);
    }

    @DeleteMapping
    public CartResponse clear(@AuthenticationPrincipal AppPrincipal principal) {
        return cartService.clear(principal.userId());
    }
}
