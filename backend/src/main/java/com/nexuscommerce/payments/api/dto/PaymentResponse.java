package com.nexuscommerce.payments.api.dto;

import com.nexuscommerce.payments.domain.Payment;
import java.math.BigDecimal;

/** Safe payment view — never exposes encrypted card data, only brand + last4. */
public record PaymentResponse(
        String status,
        String method,
        BigDecimal amount,
        String currency,
        String cardBrand,
        String cardLast4) {

    public static PaymentResponse from(Payment payment) {
        if (payment == null) {
            return null;
        }
        return new PaymentResponse(
                payment.getStatus().name(),
                payment.getMethod().name(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getCardBrand(),
                payment.getCardLast4());
    }
}
