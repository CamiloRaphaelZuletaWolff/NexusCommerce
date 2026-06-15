package com.nexuscommerce.catalog.api.dto;

import com.nexuscommerce.catalog.domain.Category;
import java.time.Instant;
import java.util.List;

public record CategoryResponse(
        String id,
        String slug,
        String name,
        String parentSlug,
        List<AttributeDefinitionDto> attributes,
        Instant createdAt) {

    public static CategoryResponse from(Category category) {
        List<AttributeDefinitionDto> attributes = category.getAttributes() == null ? List.of()
                : category.getAttributes().stream().map(AttributeDefinitionDto::from).toList();
        return new CategoryResponse(category.getId(), category.getSlug(), category.getName(),
                category.getParentSlug(), attributes, category.getCreatedAt());
    }
}
