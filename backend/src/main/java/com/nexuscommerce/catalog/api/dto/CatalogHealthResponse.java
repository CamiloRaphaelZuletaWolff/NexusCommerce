package com.nexuscommerce.catalog.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Result of a real MongoDB write/read round-trip against the catalog. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CatalogHealthResponse(
        String status,
        String database,
        String collection,
        long roundTripMillis,
        String error) {
}
