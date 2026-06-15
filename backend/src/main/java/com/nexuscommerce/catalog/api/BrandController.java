package com.nexuscommerce.catalog.api;

import com.nexuscommerce.catalog.api.dto.BrandRequest;
import com.nexuscommerce.catalog.api.dto.BrandResponse;
import com.nexuscommerce.catalog.service.BrandService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public List<BrandResponse> list() {
        return brandService.list();
    }

    @GetMapping("/{slug}")
    public BrandResponse getBySlug(@PathVariable String slug) {
        return brandService.getBySlug(slug);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.create(request));
    }
}
