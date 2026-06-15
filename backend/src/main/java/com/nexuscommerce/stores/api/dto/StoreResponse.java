package com.nexuscommerce.stores.api.dto;

import com.nexuscommerce.stores.domain.StoreStatus;
import com.nexuscommerce.stores.domain.StoreType;
import java.time.Instant;
import java.util.UUID;

public record StoreResponse(
        UUID id,
        UUID ownerUserId,
        String name,
        String slug,
        String description,
        StoreType storeType,
        StoreStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
