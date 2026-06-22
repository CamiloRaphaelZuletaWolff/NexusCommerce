package com.nexuscommerce.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexuscommerce.catalog.api.dto.CustomerProfileResponse;
import com.nexuscommerce.catalog.api.dto.UpdateCustomerProfileRequest;
import com.nexuscommerce.catalog.domain.CustomerProfile;
import com.nexuscommerce.catalog.repository.CustomerProfileRepository;
import com.nexuscommerce.catalog.repository.ProductRepository;
import com.nexuscommerce.shared.error.ResourceNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {

    @Mock private CustomerProfileRepository customerProfileRepository;
    @Mock private ProductRepository productRepository;
    @InjectMocks private CustomerProfileService service;

    private final UUID userId = UUID.randomUUID();
    private final String email = "customer@nexus.local";

    private CustomerProfile existing() {
        CustomerProfile p = new CustomerProfile();
        p.setId(userId.toString());
        p.setEmail(email);
        p.setFavoriteCategories(new ArrayList<>(List.of("electronics")));
        p.setFavoriteBrands(new ArrayList<>());
        p.setRecentlyViewedProductIds(new ArrayList<>());
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        return p;
    }

    private CustomerProfile withRecent(int count) {
        CustomerProfile p = existing();
        List<String> seed = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            seed.add("p" + i);
        }
        p.setRecentlyViewedProductIds(seed);
        return p;
    }

    @Test
    void getForUserCreatesProfileWhenAbsent() {
        when(customerProfileRepository.findById(userId.toString())).thenReturn(Optional.empty());
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerProfileResponse res = service.getForUser(userId, email);

        // The profile is created keyed by the PostgreSQL user UUID (the bridge).
        assertThat(res.id()).isEqualTo(userId.toString());
        assertThat(res.email()).isEqualTo(email);
        assertThat(res.favoriteCategories()).isEmpty();
        assertThat(res.recentlyViewedProductIds()).isEmpty();
        verify(customerProfileRepository).save(any(CustomerProfile.class));
    }

    @Test
    void getForUserReturnsExistingProfileWithoutWriting() {
        when(customerProfileRepository.findById(userId.toString())).thenReturn(Optional.of(existing()));

        CustomerProfileResponse res = service.getForUser(userId, email);

        assertThat(res.favoriteCategories()).containsExactly("electronics");
        verify(customerProfileRepository, never()).save(any());
    }

    @Test
    void updatePreferencesReplacesOnlyProvidedLists() {
        when(customerProfileRepository.findById(userId.toString())).thenReturn(Optional.of(existing()));
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // categories = null (leave unchanged); brands provided (replace).
        UpdateCustomerProfileRequest req = new UpdateCustomerProfileRequest(null, List.of("Nexies"));
        CustomerProfileResponse res = service.updatePreferences(userId, email, req);

        assertThat(res.favoriteCategories()).containsExactly("electronics"); // unchanged
        assertThat(res.favoriteBrands()).containsExactly("Nexies");          // replaced
    }

    @Test
    void recordRecentlyViewedDedupesAndKeepsMostRecentFirst() {
        when(productRepository.existsById("p3")).thenReturn(true);
        when(customerProfileRepository.findById(userId.toString())).thenReturn(Optional.of(withRecent(12)));
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerProfileResponse res = service.recordRecentlyViewed(userId, email, "p3");

        assertThat(res.recentlyViewedProductIds()).hasSize(12);                  // no growth (de-dup)
        assertThat(res.recentlyViewedProductIds().get(0)).isEqualTo("p3");       // moved to front
        assertThat(res.recentlyViewedProductIds().stream().filter("p3"::equals).count()).isEqualTo(1L);
    }

    @Test
    void recordRecentlyViewedNewProductPrependsAndCapsAtTwelve() {
        when(productRepository.existsById("fresh")).thenReturn(true);
        when(customerProfileRepository.findById(userId.toString())).thenReturn(Optional.of(withRecent(12)));
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerProfileResponse res = service.recordRecentlyViewed(userId, email, "fresh");

        assertThat(res.recentlyViewedProductIds()).hasSize(12);                  // capped
        assertThat(res.recentlyViewedProductIds().get(0)).isEqualTo("fresh");    // newest first
        assertThat(res.recentlyViewedProductIds()).doesNotContain("p11");        // oldest dropped
    }

    @Test
    void recordRecentlyViewedThrowsWhenProductDoesNotExist() {
        when(productRepository.existsById("ghost")).thenReturn(false);

        assertThatThrownBy(() -> service.recordRecentlyViewed(userId, email, "ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(customerProfileRepository, never()).findById(any());
        verify(customerProfileRepository, never()).save(any());
    }
}
