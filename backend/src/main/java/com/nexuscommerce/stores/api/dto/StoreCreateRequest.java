package com.nexuscommerce.stores.api.dto;

import com.nexuscommerce.stores.domain.StoreType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StoreCreateRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @NotNull StoreType storeType) {
}
