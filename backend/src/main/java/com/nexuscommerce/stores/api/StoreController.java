package com.nexuscommerce.stores.api;

import com.nexuscommerce.security.AppPrincipal;
import com.nexuscommerce.shared.web.RequestMetadata;
import com.nexuscommerce.stores.api.dto.StoreCreateRequest;
import com.nexuscommerce.stores.api.dto.StoreResponse;
import com.nexuscommerce.stores.api.dto.StoreUpdateRequest;
import com.nexuscommerce.stores.domain.StoreStatus;
import com.nexuscommerce.stores.service.StoreService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final StoreService storeService;

    @GetMapping
    public Page<StoreResponse> list(@RequestParam(required = false) StoreStatus status,
                                    @PageableDefault(size = 20) Pageable pageable) {
        return storeService.list(status, pageable);
    }

    @GetMapping("/{slug}")
    public StoreResponse getBySlug(@PathVariable String slug) {
        return storeService.getBySlug(slug);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STORE_MANAGER','ADMIN')")
    public ResponseEntity<StoreResponse> create(@Valid @RequestBody StoreCreateRequest request,
                                                @AuthenticationPrincipal AppPrincipal principal,
                                                HttpServletRequest httpRequest) {
        StoreResponse created = storeService.create(request, principal.userId(), RequestMetadata.from(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public StoreResponse update(@PathVariable UUID id,
                                @Valid @RequestBody StoreUpdateRequest request,
                                Authentication authentication) {
        AppPrincipal principal = (AppPrincipal) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
        return storeService.update(id, request, principal.userId(), isAdmin);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        storeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
