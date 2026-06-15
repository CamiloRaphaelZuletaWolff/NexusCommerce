package com.nexuscommerce.cart.repository;

import com.nexuscommerce.cart.domain.Cart;
import com.nexuscommerce.cart.domain.CartStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUserIdAndStatus(UUID userId, CartStatus status);
}
