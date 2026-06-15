package com.nexuscommerce.orders.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @Valid @NotNull ShippingAddressRequest shipping,
        @Valid @NotNull CardPaymentRequest payment) {

    public record ShippingAddressRequest(
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Size(max = 200) String line1,
            @Size(max = 200) String line2,
            @NotBlank @Size(max = 120) String city,
            @Size(max = 120) String region,
            @NotBlank @Size(max = 20) String postalCode,
            @NotBlank @Size(min = 2, max = 2) String country) {
    }

    /** CVV is validated but intentionally never stored. */
    public record CardPaymentRequest(
            @NotBlank @Pattern(regexp = "\\d{13,19}", message = "card number must be 13-19 digits") String number,
            @NotBlank @Size(max = 150) String holderName,
            @NotBlank @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "expiry must be MM/YY") String expiry,
            @NotBlank @Pattern(regexp = "\\d{3,4}", message = "cvv must be 3-4 digits") String cvv) {
    }
}
