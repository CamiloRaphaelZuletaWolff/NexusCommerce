package com.nexuscommerce.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexuscommerce.cart.api.dto.AddCartItemRequest;
import com.nexuscommerce.cart.domain.Cart;
import com.nexuscommerce.cart.domain.CartItem;
import com.nexuscommerce.cart.domain.CartStatus;
import com.nexuscommerce.cart.repository.CartRepository;
import com.nexuscommerce.catalog.api.dto.ProductResponse;
import com.nexuscommerce.catalog.service.ProductService;
import com.nexuscommerce.shared.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private ProductService productService;
    @InjectMocks private CartService cartService;

    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();

    private ProductResponse product() {
        return new ProductResponse(productId.toString(), storeId.toString(), "SKU1", "Nexus Phone",
                "nexus-phone", "desc", new BigDecimal("99.99"), "USD", "smartphones",
                List.of("electronics"), List.of("5g"), "Nexies", List.of(), Map.of(),
                "ACTIVE", 0.0, 0, null, null);
    }

    private CartItem lineItem(Cart cart, int quantity) {
        return new CartItem(cart, productId, storeId, "SKU1", "Nexus Phone",
                new BigDecimal("99.99"), quantity);
    }

    private void echoSave() {
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void getCartViewCreatesActiveCartWhenNoneExists() {
        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Optional.empty());
        echoSave();

        assertThat(cartService.getCartView(userId)).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItemAddsNewLine() {
        Cart cart = new Cart(userId);
        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(productService.getById(productId.toString())).thenReturn(product());
        echoSave();

        cartService.addItem(userId, new AddCartItemRequest(productId.toString(), 2));

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(cart.getItems().get(0).getProductId()).isEqualTo(productId);
    }

    @Test
    void addItemMergesQuantityForExistingProduct() {
        Cart cart = new Cart(userId);
        cart.addItem(lineItem(cart, 2));
        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        when(productService.getById(productId.toString())).thenReturn(product());
        echoSave();

        cartService.addItem(userId, new AddCartItemRequest(productId.toString(), 3));

        assertThat(cart.getItems()).hasSize(1);          // merged, not a second line
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void updateItemChangesQuantity() {
        Cart cart = new Cart(userId);
        CartItem item = lineItem(cart, 1);
        UUID itemId = UUID.randomUUID();
        ReflectionTestUtils.setField(item, "id", itemId);
        cart.addItem(item);
        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        echoSave();

        cartService.updateItem(userId, itemId, 7);

        assertThat(item.getQuantity()).isEqualTo(7);
    }

    @Test
    void updateItemThrowsWhenItemMissing() {
        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(new Cart(userId)));

        assertThatThrownBy(() -> cartService.updateItem(userId, UUID.randomUUID(), 3))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void removeItemThrowsWhenItemMissing() {
        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(new Cart(userId)));

        assertThatThrownBy(() -> cartService.removeItem(userId, UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void clearEmptiesTheCart() {
        Cart cart = new Cart(userId);
        cart.addItem(lineItem(cart, 2));
        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
        echoSave();

        cartService.clear(userId);

        assertThat(cart.getItems()).isEmpty();
    }
}
