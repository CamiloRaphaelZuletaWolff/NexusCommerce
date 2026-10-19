package com.nexuscommerce.payments.domain;

import com.nexuscommerce.security.crypto.EncryptedStringConverter;
import com.nexuscommerce.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Payment record. Card fields are encrypted at rest (AES-256-GCM via
 * {@link EncryptedStringConverter}); the raw PAN/CVV are never persisted — only a
 * masked PAN, holder and expiry (encrypted) plus the clear {@code last4}/brand for display.
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment extends BaseEntity {

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "card_brand", length = 20)
    private String cardBrand;

    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "card_holder_enc")
    private String cardHolder;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "card_number_enc")
    private String cardNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "card_exp_enc")
    private String cardExpiry;

    public Payment(UUID orderId, PaymentStatus status, PaymentMethod method, BigDecimal amount,
                   String currency, String cardBrand, String cardLast4,
                   String cardHolder, String cardNumber, String cardExpiry) {
        this.orderId = orderId;
        this.status = status;
        this.method = method;
        this.amount = amount;
        this.currency = currency;
        this.cardBrand = cardBrand;
        this.cardLast4 = cardLast4;
        this.cardHolder = cardHolder;
        this.cardNumber = cardNumber;
        this.cardExpiry = cardExpiry;
    }
}
