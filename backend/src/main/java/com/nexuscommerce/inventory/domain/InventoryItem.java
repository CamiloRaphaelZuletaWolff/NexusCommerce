package com.nexuscommerce.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/** Transactional stock for a product (keyed by the MongoDB product UUID). */
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class InventoryItem {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public InventoryItem(UUID storeId, UUID productId, String sku, int availableQuantity) {
        this.storeId = storeId;
        this.productId = productId;
        this.sku = sku;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = 0;
        this.updatedAt = Instant.now();
    }
}
