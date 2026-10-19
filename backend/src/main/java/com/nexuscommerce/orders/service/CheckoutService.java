package com.nexuscommerce.orders.service;

import com.nexuscommerce.audit.domain.AuditEventType;
import com.nexuscommerce.audit.service.AuditService;
import com.nexuscommerce.cart.domain.Cart;
import com.nexuscommerce.cart.domain.CartItem;
import com.nexuscommerce.cart.service.CartService;
import com.nexuscommerce.inventory.service.InventoryService;
import com.nexuscommerce.orders.api.dto.CheckoutRequest;
import com.nexuscommerce.orders.api.dto.OrderResponse;
import com.nexuscommerce.orders.domain.Order;
import com.nexuscommerce.orders.domain.OrderItem;
import com.nexuscommerce.orders.repository.OrderRepository;
import com.nexuscommerce.payments.domain.Payment;
import com.nexuscommerce.payments.domain.PaymentMethod;
import com.nexuscommerce.payments.domain.PaymentStatus;
import com.nexuscommerce.payments.repository.PaymentRepository;
import com.nexuscommerce.shared.web.RequestMetadata;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Checkout orchestration — the ACID heart of the platform.
 *
 * <p>The whole flow runs in a single transaction: reserve stock (stored procedure),
 * create the order + items, record the (encrypted) payment, and mark the cart
 * checked-out. If any step fails (e.g. insufficient stock), the transaction rolls
 * back entirely — no partial order, no lost inventory.
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.07");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CartService cartService;
    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final AuditService auditService;

    @Transactional
    public OrderResponse checkout(UUID userId, CheckoutRequest request, RequestMetadata metadata) {
        Cart cart = cartService.getActiveCart(userId)
                .filter(c -> !c.getItems().isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty"));

        try {
            // 1) Reserve stock for every line via the stored procedure (fails -> rollback).
            for (CartItem item : cart.getItems()) {
                reserveOrFail(item);
            }

            // 2) Build the order + items, snapshotting price.
            Order order = new Order(userId, generateOrderNumber(), "USD");
            applyShipping(order, request);
            BigDecimal subtotal = BigDecimal.ZERO;
            for (CartItem item : cart.getItems()) {
                OrderItem orderItem = new OrderItem(order, item.getProductId(), item.getStoreId(),
                        item.getSku(), item.getName(), item.getUnitPrice(), item.getQuantity());
                order.addItem(orderItem);
                subtotal = subtotal.add(orderItem.getLineTotal());
            }
            BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
            order.applyTotals(subtotal.setScale(2, RoundingMode.HALF_UP), tax);
            order.markPaid();
            Order saved = orderRepository.save(order); // cascades order_items

            // 3) Record the payment (card fields encrypted at rest).
            Payment payment = buildPayment(saved.getId(), saved.getTotal(), request.payment());
            paymentRepository.save(payment);

            // 4) Close the cart.
            cart.markCheckedOut();

            auditService.success(AuditEventType.ORDER_PLACED, userId, metadata, Map.of(
                    "orderId", String.valueOf(saved.getId()),
                    "orderNumber", saved.getOrderNumber(),
                    "total", saved.getTotal().toPlainString()));
            auditService.success(AuditEventType.PAYMENT_CAPTURED, userId, metadata, Map.of(
                    "orderId", String.valueOf(saved.getId()),
                    "amount", payment.getAmount().toPlainString(),
                    "last4", String.valueOf(payment.getCardLast4())));

            return OrderResponse.from(saved, payment);
        } catch (RuntimeException ex) {
            auditService.failure(AuditEventType.CHECKOUT_FAILED, userId, metadata,
                    Map.of("reason", String.valueOf(ex.getMessage())));
            throw ex;
        }
    }

    private void reserveOrFail(CartItem item) {
        try {
            inventoryService.reserve(item.getProductId(), item.getQuantity());
        } catch (RuntimeException ex) {
            // Stored procedure raised (missing/insufficient stock) -> client error, rolls back the tx.
            throw new IllegalArgumentException("Item is out of stock: " + item.getName());
        }
    }

    private void applyShipping(Order order, CheckoutRequest request) {
        CheckoutRequest.ShippingAddressRequest s = request.shipping();
        order.setShippingAddress(s.name(), s.line1(), s.line2(), s.city(), s.region(),
                s.postalCode(), s.country());
    }

    private Payment buildPayment(UUID orderId, BigDecimal amount, CheckoutRequest.CardPaymentRequest card) {
        String number = card.number();
        String last4 = number.substring(number.length() - 4);
        // The full PAN and the CVV are never persisted: payments are simulated, so there is
        // nothing to charge later, and a public demo must not become a store of real card
        // numbers people type in. Only a masked PAN (plus holder/expiry) is kept, still
        // encrypted at rest by the entity converter.
        String maskedNumber = "*".repeat(number.length() - 4) + last4;
        return new Payment(orderId, PaymentStatus.CAPTURED, PaymentMethod.CARD, amount, "USD",
                detectBrand(number), last4, card.holderName(), maskedNumber, card.expiry());
    }

    private static String detectBrand(String number) {
        if (number.startsWith("4")) {
            return "VISA";
        }
        if (number.matches("^5[1-5].*")) {
            return "MASTERCARD";
        }
        if (number.matches("^3[47].*")) {
            return "AMEX";
        }
        return "CARD";
    }

    private static String generateOrderNumber() {
        String ts = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
        String suffix = Integer.toString(RANDOM.nextInt(0x1000), 36).toUpperCase();
        return "NX-" + ts + "-" + suffix;
    }
}
