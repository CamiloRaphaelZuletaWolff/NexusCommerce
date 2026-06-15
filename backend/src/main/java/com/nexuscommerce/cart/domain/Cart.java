package com.nexuscommerce.cart.domain;

import com.nexuscommerce.shared.domain.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor
public class Cart extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CartStatus status = CartStatus.ACTIVE;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public Cart(UUID userId) {
        this.userId = userId;
        this.status = CartStatus.ACTIVE;
    }

    public void addItem(CartItem item) {
        items.add(item);
    }

    public boolean removeItem(UUID itemId) {
        return items.removeIf(item -> item.getId().equals(itemId));
    }

    public void clear() {
        items.clear();
    }

    public void markCheckedOut() {
        this.status = CartStatus.CHECKED_OUT;
    }
}
