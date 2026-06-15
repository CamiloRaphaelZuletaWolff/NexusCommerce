package com.nexuscommerce.catalog.api;

import com.nexuscommerce.catalog.api.dto.ProductCreateRequest;
import com.nexuscommerce.catalog.api.dto.ProductResponse;
import com.nexuscommerce.catalog.api.dto.ProductUpdateRequest;
import com.nexuscommerce.catalog.domain.ProductStatus;
import com.nexuscommerce.catalog.repository.ProductSearchCriteria;
import com.nexuscommerce.catalog.service.ProductService;
import com.nexuscommerce.security.AppPrincipal;
import com.nexuscommerce.shared.web.RequestMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
public class ProductController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final ProductService productService;

    /** Search with comparative ($gt/$lt price), logical ($or/$and) and array ($in) filters. */
    @GetMapping
    public Page<ProductResponse> search(
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) List<String> brands,
            @RequestParam(required = false) List<String> variantSkus,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ProductStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                storeId, q, categories, tags, brands, variantSkus, minPrice, maxPrice, status);
        return productService.search(criteria, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable String id) {
        return productService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STORE_MANAGER','ADMIN')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request,
                                                  Authentication authentication,
                                                  HttpServletRequest httpRequest) {
        AppPrincipal principal = (AppPrincipal) authentication.getPrincipal();
        ProductResponse created = productService.create(
                request, principal.userId(), isAdmin(authentication), RequestMetadata.from(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ProductResponse update(@PathVariable String id,
                                  @Valid @RequestBody ProductUpdateRequest request,
                                  Authentication authentication) {
        AppPrincipal principal = (AppPrincipal) authentication.getPrincipal();
        return productService.update(id, request, principal.userId(), isAdmin(authentication));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable String id, Authentication authentication) {
        AppPrincipal principal = (AppPrincipal) authentication.getPrincipal();
        productService.delete(id, principal.userId(), isAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }
}
