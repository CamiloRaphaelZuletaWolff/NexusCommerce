package com.nexuscommerce.catalog.service;

import com.nexuscommerce.catalog.api.dto.BrandRequest;
import com.nexuscommerce.catalog.api.dto.BrandResponse;
import com.nexuscommerce.catalog.domain.Brand;
import com.nexuscommerce.catalog.repository.BrandRepository;
import com.nexuscommerce.shared.error.DuplicateResourceException;
import com.nexuscommerce.shared.error.ResourceNotFoundException;
import com.nexuscommerce.shared.util.Slugs;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public List<BrandResponse> list() {
        return brandRepository.findAll().stream().map(BrandResponse::from).toList();
    }

    public BrandResponse getBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() -> ResourceNotFoundException.of("Brand", slug));
        return BrandResponse.from(brand);
    }

    public BrandResponse create(BrandRequest request) {
        String slug = StringUtils.hasText(request.slug())
                ? Slugs.slugify(request.slug()) : Slugs.slugify(request.name());
        if (brandRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("Brand slug already exists: " + slug);
        }
        Brand brand = new Brand();
        brand.setId(UUID.randomUUID().toString());
        brand.setSlug(slug);
        brand.setName(request.name().trim());
        brand.setDescription(request.description());
        brand.setCreatedAt(Instant.now());
        return BrandResponse.from(brandRepository.save(brand));
    }
}
