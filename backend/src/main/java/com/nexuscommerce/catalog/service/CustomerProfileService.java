package com.nexuscommerce.catalog.service;

import com.nexuscommerce.catalog.api.dto.CustomerProfileResponse;
import com.nexuscommerce.catalog.api.dto.UpdateCustomerProfileRequest;
import com.nexuscommerce.catalog.domain.CustomerProfile;
import com.nexuscommerce.catalog.repository.CustomerProfileRepository;
import com.nexuscommerce.catalog.repository.ProductRepository;
import com.nexuscommerce.shared.error.ResourceNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Reads and writes the MongoDB customer profile that is keyed by the PostgreSQL user
 * UUID (the cross-database bridge, ADR-0003). The profile is normally created on
 * registration (ADR-0011); here it becomes bidirectional — customers update their
 * favorites and the catalog records what they browse. Access is get-or-create, so a
 * user who predates the register-time sync still gets a profile on first use.
 */
@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    /** Cap on the recently-viewed list so the write stays cheap. */
    private static final int MAX_RECENTLY_VIEWED = 12;

    private final CustomerProfileRepository customerProfileRepository;
    private final ProductRepository productRepository;

    public CustomerProfileResponse getForUser(UUID userId, String email) {
        return CustomerProfileResponse.from(requireOrCreate(userId, email));
    }

    /** Replaces the favorite sets that were provided (null = leave unchanged). */
    public CustomerProfileResponse updatePreferences(UUID userId, String email,
                                                     UpdateCustomerProfileRequest request) {
        CustomerProfile profile = requireOrCreate(userId, email);
        if (request.favoriteCategories() != null) {
            profile.setFavoriteCategories(List.copyOf(request.favoriteCategories()));
        }
        if (request.favoriteBrands() != null) {
            profile.setFavoriteBrands(List.copyOf(request.favoriteBrands()));
        }
        profile.setUpdatedAt(Instant.now());
        return CustomerProfileResponse.from(customerProfileRepository.save(profile));
    }

    /** Records a product view (most-recent-first, de-duplicated, capped). */
    public CustomerProfileResponse recordRecentlyViewed(UUID userId, String email, String productId) {
        if (!productRepository.existsById(productId)) {
            throw ResourceNotFoundException.of("Product", productId);
        }
        CustomerProfile profile = requireOrCreate(userId, email);

        List<String> recent = new ArrayList<>(
                profile.getRecentlyViewedProductIds() == null ? List.of() : profile.getRecentlyViewedProductIds());
        recent.remove(productId);            // de-dup: drop any earlier occurrence
        recent.add(0, productId);            // most-recent-first
        if (recent.size() > MAX_RECENTLY_VIEWED) {
            recent = new ArrayList<>(recent.subList(0, MAX_RECENTLY_VIEWED));
        }
        profile.setRecentlyViewedProductIds(recent);
        profile.setUpdatedAt(Instant.now());
        return CustomerProfileResponse.from(customerProfileRepository.save(profile));
    }

    /** Returns the profile for the user, creating a minimal one (keyed by UUID) if absent. */
    private CustomerProfile requireOrCreate(UUID userId, String email) {
        return customerProfileRepository.findById(userId.toString())
                .orElseGet(() -> {
                    CustomerProfile profile = new CustomerProfile();
                    profile.setId(userId.toString());   // == PostgreSQL user UUID (the bridge)
                    profile.setEmail(email);
                    profile.setFavoriteCategories(List.of());
                    profile.setFavoriteBrands(List.of());
                    profile.setRecentlyViewedProductIds(List.of());
                    profile.setCreatedAt(Instant.now());
                    profile.setUpdatedAt(Instant.now());
                    return customerProfileRepository.save(profile);
                });
    }
}
