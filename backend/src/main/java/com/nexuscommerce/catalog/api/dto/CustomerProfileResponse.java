package com.nexuscommerce.catalog.api.dto;

import com.nexuscommerce.catalog.domain.CustomerProfile;
import java.time.Instant;
import java.util.List;

public record CustomerProfileResponse(
        String id,
        String email,
        String fullName,
        List<String> favoriteCategories,
        List<String> favoriteBrands,
        List<String> recentlyViewedProductIds,
        Instant createdAt,
        Instant updatedAt) {

    public static CustomerProfileResponse from(CustomerProfile profile) {
        return new CustomerProfileResponse(
                profile.getId(),
                profile.getEmail(),
                profile.getFullName(),
                profile.getFavoriteCategories(),
                profile.getFavoriteBrands(),
                profile.getRecentlyViewedProductIds(),
                profile.getCreatedAt(),
                profile.getUpdatedAt());
    }
}
