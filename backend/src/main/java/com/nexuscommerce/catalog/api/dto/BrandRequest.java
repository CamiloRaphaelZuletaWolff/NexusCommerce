package com.nexuscommerce.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 120) String slug,
        @Size(max = 1000) String description) {
}
