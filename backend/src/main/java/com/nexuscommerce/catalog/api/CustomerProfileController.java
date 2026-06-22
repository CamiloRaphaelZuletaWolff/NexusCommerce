package com.nexuscommerce.catalog.api;

import com.nexuscommerce.catalog.api.dto.CustomerProfileResponse;
import com.nexuscommerce.catalog.api.dto.UpdateCustomerProfileRequest;
import com.nexuscommerce.catalog.service.CustomerProfileService;
import com.nexuscommerce.security.AppPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the MongoDB customer profile synced from the PostgreSQL user by UUID, and
 * the write paths that make that bridge bidirectional: updating favorites and
 * recording recently-viewed products. All scoped to the authenticated caller.
 */
@RestController
@RequestMapping("/api/catalog/me")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public CustomerProfileResponse profile(@AuthenticationPrincipal AppPrincipal principal) {
        return customerProfileService.getForUser(principal.userId(), principal.email());
    }

    @PatchMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public CustomerProfileResponse updateProfile(@AuthenticationPrincipal AppPrincipal principal,
                                                 @Valid @RequestBody UpdateCustomerProfileRequest request) {
        return customerProfileService.updatePreferences(principal.userId(), principal.email(), request);
    }

    @PostMapping("/recently-viewed/{productId}")
    @PreAuthorize("isAuthenticated()")
    public CustomerProfileResponse recordRecentlyViewed(@AuthenticationPrincipal AppPrincipal principal,
                                                        @PathVariable String productId) {
        return customerProfileService.recordRecentlyViewed(principal.userId(), principal.email(), productId);
    }
}
