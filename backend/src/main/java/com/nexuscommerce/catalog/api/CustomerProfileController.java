package com.nexuscommerce.catalog.api;

import com.nexuscommerce.catalog.api.dto.CustomerProfileResponse;
import com.nexuscommerce.catalog.service.CustomerProfileService;
import com.nexuscommerce.security.AppPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes the MongoDB customer profile synced from the PostgreSQL user by UUID. */
@RestController
@RequestMapping("/api/catalog/me")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public CustomerProfileResponse profile(@AuthenticationPrincipal AppPrincipal principal) {
        return customerProfileService.getForUser(principal.userId());
    }
}
