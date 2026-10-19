package com.nexuscommerce.orders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexuscommerce.audit.domain.AuditEventType;
import com.nexuscommerce.audit.service.AuditService;
import com.nexuscommerce.cart.domain.Cart;
import com.nexuscommerce.cart.domain.CartItem;
import com.nexuscommerce.cart.domain.CartStatus;
import com.nexuscommerce.cart.service.CartService;
import com.nexuscommerce.inventory.service.InventoryService;
import com.nexuscommerce.orders.api.dto.CheckoutRequest;
import com.nexuscommerce.orders.api.dto.OrderResponse;
import com.nexuscommerce.orders.repository.OrderRepository;
import com.nexuscommerce.payments.domain.Payment;
import com.nexuscommerce.payments.repository.PaymentRepository;
import com.nexuscommerce.shared.web.RequestMetadata;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock private CartService cartService;
    @Mock private InventoryService inventoryService;
    @Mock private OrderRepository orderRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private AuditService auditService;

    @InjectMocks private CheckoutService checkoutService;

    private final RequestMetadata metadata = new RequestMetadata("127.0.0.1", "JUnit");

    private CheckoutRequest request() {
        return new CheckoutRequest(
                new CheckoutRequest.ShippingAddressRequest("Alice", "1 Main St", null, "Town", "Region", "12345", "US"),
                new CheckoutRequest.CardPaymentRequest("4111111111111111", "Alice", "12/30", "123"));
    }

    private Cart cartWithItem(UUID userId) {
        Cart cart = new Cart(userId);
        cart.addItem(new CartItem(cart, UUID.randomUUID(), UUID.randomUUID(),
                "SKU1", "Widget", new BigDecimal("100.00"), 2));
        return cart;
    }

    @Test
    void checkoutCreatesOrderReservesStockAndCapturesPayment() {
        UUID userId = UUID.randomUUID();
        Cart cart = cartWithItem(userId);
        when(cartService.getActiveCart(userId)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = checkoutService.checkout(userId, request(), metadata);

        assertThat(response.subtotal()).isEqualByComparingTo("200.00");
        assertThat(response.tax()).isEqualByComparingTo("14.00");   // 200 * 0.07
        assertThat(response.total()).isEqualByComparingTo("214.00");
        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.payment().cardLast4()).isEqualTo("1111");
        assertThat(response.payment().cardBrand()).isEqualTo("VISA");
        assertThat(response.payment().status()).isEqualTo("CAPTURED");
        assertThat(cart.getStatus()).isEqualTo(CartStatus.CHECKED_OUT);

        verify(inventoryService).reserve(any(), eq(2));
        verify(paymentRepository).save(any());
        verify(auditService).success(eq(AuditEventType.ORDER_PLACED), eq(userId), eq(metadata), any());
        verify(auditService).success(eq(AuditEventType.PAYMENT_CAPTURED), eq(userId), eq(metadata), any());
    }

    @Test
    void checkoutRejectsEmptyCart() {
        UUID userId = UUID.randomUUID();
        when(cartService.getActiveCart(userId)).thenReturn(Optional.of(new Cart(userId)));

        assertThatThrownBy(() -> checkoutService.checkout(userId, request(), metadata))
                .isInstanceOf(IllegalArgumentException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void insufficientStockRollsBackAndAuditsFailure() {
        UUID userId = UUID.randomUUID();
        when(cartService.getActiveCart(userId)).thenReturn(Optional.of(cartWithItem(userId)));
        doThrow(new RuntimeException("Insufficient stock")).when(inventoryService).reserve(any(), anyInt());

        assertThatThrownBy(() -> checkoutService.checkout(userId, request(), metadata))
                .isInstanceOf(RuntimeException.class);

        verify(orderRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(auditService).failure(eq(AuditEventType.CHECKOUT_FAILED), eq(userId), eq(metadata), any());
    }

    @Test
    void checkoutNeverPersistsTheFullCardNumberOrCvv() {
        UUID userId = UUID.randomUUID();
        when(cartService.getActiveCart(userId)).thenReturn(Optional.of(cartWithItem(userId)));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        checkoutService.checkout(userId, request(), metadata);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment saved = captor.getValue();
        assertThat(saved.getCardNumber()).isEqualTo("************1111").doesNotContain("4111111111111111");
        assertThat(saved.getCardLast4()).isEqualTo("1111");
    }
}
