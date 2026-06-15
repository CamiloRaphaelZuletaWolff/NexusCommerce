package com.nexuscommerce.catalog.service;

import com.nexuscommerce.catalog.api.dto.AttributeDefinitionDto;
import com.nexuscommerce.catalog.api.dto.CategoryRequest;
import com.nexuscommerce.catalog.api.dto.CategoryResponse;
import com.nexuscommerce.catalog.domain.Category;
import com.nexuscommerce.catalog.repository.CategoryRepository;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> list() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }

    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", slug));
        return CategoryResponse.from(category);
    }

    public CategoryResponse create(CategoryRequest request) {
        String slug = StringUtils.hasText(request.slug())
                ? Slugs.slugify(request.slug()) : Slugs.slugify(request.name());
        if (categoryRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("Category slug already exists: " + slug);
        }
        Category category = new Category();
        category.setId(UUID.randomUUID().toString());
        category.setSlug(slug);
        category.setName(request.name().trim());
        category.setParentSlug(request.parentSlug());
        category.setAttributes(request.attributes() == null ? List.of()
                : request.attributes().stream().map(AttributeDefinitionDto::toDomain).toList());
        category.setCreatedAt(Instant.now());
        return CategoryResponse.from(categoryRepository.save(category));
    }
}
