package com.nexuscommerce.stores.domain;

import com.nexuscommerce.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A storefront owned by a user. Catalog/products live in MongoDB (Phase 3), linked by store UUID. */
@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor
public class Store extends AuditableEntity {

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 160)
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_type", nullable = false, length = 30)
    private StoreType storeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StoreStatus status = StoreStatus.ACTIVE;

    public Store(UUID ownerUserId, String name, String slug, String description, StoreType storeType) {
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.storeType = storeType;
        this.status = StoreStatus.ACTIVE;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void describe(String description) {
        this.description = description;
    }

    public void changeStatus(StoreStatus status) {
        this.status = status;
    }
}
