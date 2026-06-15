package com.nexuscommerce.orders.service;

import com.nexuscommerce.orders.api.dto.OrderResponse;
import com.nexuscommerce.orders.domain.Order;
import com.nexuscommerce.orders.repository.OrderRepository;
import com.nexuscommerce.payments.repository.PaymentRepository;
import com.nexuscommerce.shared.error.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public List<OrderResponse> listForUser(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(order -> OrderResponse.from(order, paymentRepository.findByOrderId(order.getId()).orElse(null)))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getForUser(UUID userId, UUID orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));
        return OrderResponse.from(order, paymentRepository.findByOrderId(order.getId()).orElse(null));
    }
}
