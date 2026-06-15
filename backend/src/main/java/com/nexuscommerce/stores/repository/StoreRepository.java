package com.nexuscommerce.stores.repository;

import com.nexuscommerce.stores.domain.Store;
import com.nexuscommerce.stores.domain.StoreStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Store> findByStatus(StoreStatus status, Pageable pageable);
}
