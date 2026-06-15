package com.nexuscommerce.orders.api.dto;

import com.nexuscommerce.orders.domain.Order;
import com.nexuscommerce.payments.api.dto.PaymentResponse;
import com.nexuscommerce.payments.domain.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        UUID userId,
        String status,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        String currency,
        ShippingAddressResponse shipping,
        List<OrderItemResponse> items,
        PaymentResponse payment,
        Instant createdAt) {

    public record ShippingAddressResponse(String name, String line1, String line2, String city,
                                          String region, String postalCode, String country) {
    }

    public static OrderResponse from(Order order, Payment payment) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();
        ShippingAddressResponse shipping = new ShippingAddressResponse(
                order.getShippingName(), order.getShippingLine1(), order.getShippingLine2(),
                order.getShippingCity(), order.getShippingRegion(), order.getShippingPostalCode(),
                order.getShippingCountry());
        return new OrderResponse(
                order.getId(), order.getOrderNumber(), order.getUserId(), order.getStatus().name(),
                order.getSubtotal(), order.getTax(), order.getTotal(), order.getCurrency(),
                shipping, items, PaymentResponse.from(payment), order.getCreatedAt());
    }
}
