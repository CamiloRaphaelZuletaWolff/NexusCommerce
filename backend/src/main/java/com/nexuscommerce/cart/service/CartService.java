package com.nexuscommerce.cart.service;

import com.nexuscommerce.cart.api.dto.AddCartItemRequest;
import com.nexuscommerce.cart.api.dto.CartResponse;
import com.nexuscommerce.cart.domain.Cart;
import com.nexuscommerce.cart.domain.CartItem;
import com.nexuscommerce.cart.domain.CartStatus;
import com.nexuscommerce.cart.repository.CartRepository;
import com.nexuscommerce.catalog.api.dto.ProductResponse;
import com.nexuscommerce.catalog.domain.ProductStatus;
import com.nexuscommerce.catalog.service.ProductService;
import com.nexuscommerce.shared.error.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;

    @Transactional
    public CartResponse getCartView(UUID userId) {
        return CartResponse.from(getOrCreate(userId));
    }

    @Transactional
    public CartResponse addItem(UUID userId, AddCartItemRequest request) {
        Cart cart = getOrCreate(userId);
        ProductResponse product = productService.getById(request.productId()); // validates + snapshots
        if (!ProductStatus.ACTIVE.name().equals(product.status())) {
            // Draft/archived products are not for sale, even if their id is known.
            throw ResourceNotFoundException.of("Product", request.productId());
        }
        UUID productId = UUID.fromString(product.id());

        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.addQuantity(request.quantity()),
                        () -> cart.addItem(new CartItem(cart, productId, UUID.fromString(product.storeId()),
                                product.sku(), product.name(), product.price(), request.quantity())));
        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(UUID userId, UUID itemId, int quantity) {
        Cart cart = getOrCreate(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> ResourceNotFoundException.of("Cart item", itemId));
        item.setQuantity(quantity);
        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(UUID userId, UUID itemId) {
        Cart cart = getOrCreate(userId);
        if (!cart.removeItem(itemId)) {
            throw ResourceNotFoundException.of("Cart item", itemId);
        }
        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse clear(UUID userId) {
        Cart cart = getOrCreate(userId);
        cart.clear();
        return CartResponse.from(cartRepository.save(cart));
    }

    /** Used by checkout; returns the managed active cart within the caller's transaction. */
    @Transactional
    public Optional<Cart> getActiveCart(UUID userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE);
    }

    private Cart getOrCreate(UUID userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));
    }
}
