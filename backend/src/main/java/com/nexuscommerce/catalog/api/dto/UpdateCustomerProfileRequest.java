package com.nexuscommerce.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Partial update of a customer's catalog preferences (PATCH semantics): a non-null
 * list replaces that preference set; a null list leaves it unchanged. Bounds keep
 * the write cheap and prevent abuse.
 */
public record UpdateCustomerProfileRequest(
        @Size(max = 50, message = "At most 50 favorite categories")
        List<@NotBlank @Size(max = 120) String> favoriteCategories,
        @Size(max = 50, message = "At most 50 favorite brands")
        List<@NotBlank @Size(max = 120) String> favoriteBrands) {
}
