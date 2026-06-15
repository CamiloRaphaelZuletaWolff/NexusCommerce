package com.nexuscommerce.orders.api;

import com.nexuscommerce.orders.api.dto.OrderResponse;
import com.nexuscommerce.orders.service.OrderService;
import com.nexuscommerce.security.AppPrincipal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> myOrders(@AuthenticationPrincipal AppPrincipal principal) {
        return orderService.listForUser(principal.userId());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable UUID id, @AuthenticationPrincipal AppPrincipal principal) {
        return orderService.getForUser(principal.userId(), id);
    }
}
