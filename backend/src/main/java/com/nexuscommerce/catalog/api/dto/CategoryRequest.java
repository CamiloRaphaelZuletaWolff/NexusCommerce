package com.nexuscommerce.catalog.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CategoryRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 120) String slug,
        String parentSlug,
        @Valid List<AttributeDefinitionDto> attributes) {
}
