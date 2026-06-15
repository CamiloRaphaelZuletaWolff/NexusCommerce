package com.nexuscommerce.catalog.api.dto;

import com.nexuscommerce.catalog.domain.AttributeDefinition;
import com.nexuscommerce.catalog.domain.AttributeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AttributeDefinitionDto(
        @NotBlank String key,
        String label,
        @NotNull AttributeType type,
        boolean required,
        String unit) {

    public static AttributeDefinitionDto from(AttributeDefinition definition) {
        return new AttributeDefinitionDto(definition.getKey(), definition.getLabel(),
                definition.getType(), definition.isRequired(), definition.getUnit());
    }

    public AttributeDefinition toDomain() {
        return new AttributeDefinition(key, label, type, required, unit);
    }
}
