package com.nexuscommerce.catalog.service;

import com.nexuscommerce.audit.domain.AuditEventType;
import com.nexuscommerce.audit.service.AuditService;
import com.nexuscommerce.catalog.api.dto.ProductCreateRequest;
import com.nexuscommerce.catalog.api.dto.ProductResponse;
import com.nexuscommerce.catalog.api.dto.ProductUpdateRequest;
import com.nexuscommerce.catalog.api.dto.VariantDto;
import com.nexuscommerce.catalog.domain.Product;
import com.nexuscommerce.catalog.domain.ProductStatus;
import com.nexuscommerce.catalog.repository.ProductRepository;
import com.nexuscommerce.catalog.repository.ProductSearchCriteria;
import com.nexuscommerce.shared.error.ResourceNotFoundException;
import com.nexuscommerce.shared.util.Slugs;
import com.nexuscommerce.shared.web.RequestMetadata;
import com.nexuscommerce.stores.api.dto.StoreResponse;
import com.nexuscommerce.stores.service.StoreService;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Product catalog operations (MongoDB).
 *
 * <p>Cross-database integration (ADR-0003): every product references its owning
 * store by UUID; create/update/delete validate the store exists in PostgreSQL and
 * that the caller owns it (or is ADMIN).
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String DEFAULT_CURRENCY = "USD";

    private final ProductRepository productRepository;
    private final StoreService storeService;
    private final AuditService auditService;

    public Page<ProductResponse> search(ProductSearchCriteria criteria, Pageable pageable) {
        return productRepository.search(criteria, pageable).map(ProductResponse::from);
    }

    public ProductResponse getById(String id) {
        return ProductResponse.from(productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id)));
    }

    public ProductResponse create(ProductCreateRequest request, UUID actingUserId, boolean isAdmin,
                                  RequestMetadata metadata) {
        StoreResponse store = storeService.getByIdOrThrow(request.storeId()); // cross-DB UUID validation
        requireOwnership(store, actingUserId, isAdmin);

        Product product = new Product();
        String id = UUID.randomUUID().toString();
        product.setId(id);
        product.setStoreId(store.id().toString());
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setSlug(Slugs.slugify(request.name()));
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCurrency(StringUtils.hasText(request.currency())
                ? request.currency().toUpperCase(Locale.ROOT) : DEFAULT_CURRENCY);
        product.setCategorySlug(request.categorySlug());
        product.setCategoryLabels(request.categoryLabels() == null ? List.of() : request.categoryLabels());
        product.setTags(request.tags() == null ? List.of() : request.tags());
        product.setBrand(request.brand());
        product.setVariants(request.variants() == null ? List.of()
                : request.variants().stream().map(VariantDto::toDomain).toList());
        product.setAttributes(request.attributes() == null ? Map.of() : request.attributes());
        product.setStatus(request.status() == null ? ProductStatus.ACTIVE : request.status());
        Instant now = Instant.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        Product saved = productRepository.save(product);
        auditService.success(AuditEventType.PRODUCT_CREATED, actingUserId, metadata,
                Map.of("productId", id, "storeId", store.id().toString(), "sku", saved.getSku()));
        return ProductResponse.from(saved);
    }

    public ProductResponse update(String id, ProductUpdateRequest request, UUID actingUserId, boolean isAdmin) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
        StoreResponse store = storeService.getByIdOrThrow(UUID.fromString(product.getStoreId()));
        requireOwnership(store, actingUserId, isAdmin);

        if (request.name() != null) {
            product.setName(request.name().trim());
            product.setSlug(Slugs.slugify(request.name()));
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.currency() != null) {
            product.setCurrency(request.currency().toUpperCase(Locale.ROOT));
        }
        if (request.categorySlug() != null) {
            product.setCategorySlug(request.categorySlug());
        }
        if (request.categoryLabels() != null) {
            product.setCategoryLabels(request.categoryLabels());
        }
        if (request.tags() != null) {
            product.setTags(request.tags());
        }
        if (request.brand() != null) {
            product.setBrand(request.brand());
        }
        if (request.variants() != null) {
            product.setVariants(request.variants().stream().map(VariantDto::toDomain).toList());
        }
        if (request.attributes() != null) {
            product.setAttributes(request.attributes());
        }
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        product.setUpdatedAt(Instant.now());
        return ProductResponse.from(productRepository.save(product));
    }

    public void delete(String id, UUID actingUserId, boolean isAdmin) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
        StoreResponse store = storeService.getByIdOrThrow(UUID.fromString(product.getStoreId()));
        requireOwnership(store, actingUserId, isAdmin);
        productRepository.deleteById(id);
    }

    private void requireOwnership(StoreResponse store, UUID actingUserId, boolean isAdmin) {
        if (!isAdmin && !store.ownerUserId().equals(actingUserId)) {
            throw new AccessDeniedException("You do not own this store");
        }
    }
}
