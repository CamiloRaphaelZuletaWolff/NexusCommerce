package com.nexuscommerce.orders.domain;

import com.nexuscommerce.shared.domain.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order extends AuditableEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "shipping_name", length = 150)
    private String shippingName;
    @Column(name = "shipping_line1", length = 200)
    private String shippingLine1;
    @Column(name = "shipping_line2", length = 200)
    private String shippingLine2;
    @Column(name = "shipping_city", length = 120)
    private String shippingCity;
    @Column(name = "shipping_region", length = 120)
    private String shippingRegion;
    @Column(name = "shipping_postal_code", length = 20)
    private String shippingPostalCode;
    @Column(name = "shipping_country", length = 2)
    private String shippingCountry;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order(UUID userId, String orderNumber, String currency) {
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.currency = currency;
        this.status = OrderStatus.PENDING;
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public void applyTotals(BigDecimal subtotal, BigDecimal tax) {
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = subtotal.add(tax);
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
    }

    public void setShippingAddress(String name, String line1, String line2, String city,
                                   String region, String postalCode, String country) {
        this.shippingName = name;
        this.shippingLine1 = line1;
        this.shippingLine2 = line2;
        this.shippingCity = city;
        this.shippingRegion = region;
        this.shippingPostalCode = postalCode;
        this.shippingCountry = country;
    }
}
