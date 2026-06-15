package com.nexuscommerce.stores.api.dto;

import com.nexuscommerce.stores.domain.StoreStatus;
import jakarta.validation.constraints.Size;

/** Partial update — null fields are left unchanged. Slug and type are immutable after creation. */
public record StoreUpdateRequest(
        @Size(max = 150) String name,
        @Size(max = 2000) String description,
        StoreStatus status) {
}
