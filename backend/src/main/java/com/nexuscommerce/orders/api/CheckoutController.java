package com.nexuscommerce.orders.api;

import com.nexuscommerce.orders.api.dto.CheckoutRequest;
import com.nexuscommerce.orders.api.dto.OrderResponse;
import com.nexuscommerce.orders.service.CheckoutService;
import com.nexuscommerce.security.AppPrincipal;
import com.nexuscommerce.shared.web.RequestMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request,
                                                  @AuthenticationPrincipal AppPrincipal principal,
                                                  HttpServletRequest httpRequest) {
        OrderResponse order = checkoutService.checkout(
                principal.userId(), request, RequestMetadata.from(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}
