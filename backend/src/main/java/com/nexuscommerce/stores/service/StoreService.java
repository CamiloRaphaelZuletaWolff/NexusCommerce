package com.nexuscommerce.stores.service;

import com.nexuscommerce.audit.domain.AuditEventType;
import com.nexuscommerce.audit.service.AuditService;
import com.nexuscommerce.shared.error.ResourceNotFoundException;
import com.nexuscommerce.shared.web.RequestMetadata;
import com.nexuscommerce.stores.api.dto.StoreCreateRequest;
import com.nexuscommerce.stores.api.dto.StoreResponse;
import com.nexuscommerce.stores.api.dto.StoreUpdateRequest;
import com.nexuscommerce.stores.domain.Store;
import com.nexuscommerce.stores.domain.StoreStatus;
import com.nexuscommerce.stores.mapper.StoreMapper;
import com.nexuscommerce.stores.repository.StoreRepository;
import com.nexuscommerce.shared.util.Slugs;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final AuditService auditService;

    /** Public browsing defaults to ACTIVE stores; an explicit status filters otherwise. */
    @Transactional(readOnly = true)
    public Page<StoreResponse> list(StoreStatus status, Pageable pageable) {
        StoreStatus effective = status != null ? status : StoreStatus.ACTIVE;
        return storeRepository.findByStatus(effective, pageable).map(storeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public StoreResponse getBySlug(String slug) {
        Store store = storeRepository.findBySlug(slug)
                .orElseThrow(() -> ResourceNotFoundException.of("Store", slug));
        return storeMapper.toResponse(store);
    }

    /** Cross-feature lookup (used by the catalog to validate a product's owning store by UUID). */
    @Transactional(readOnly = true)
    public StoreResponse getByIdOrThrow(UUID id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Store", id));
        return storeMapper.toResponse(store);
    }

    /** Ids of the stores a user owns (tenant scope for store managers). */
    @Transactional(readOnly = true)
    public Set<UUID> ownedStoreIds(UUID ownerUserId) {
        return storeRepository.findByOwnerUserId(ownerUserId).stream()
                .map(Store::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Transactional
    public StoreResponse create(StoreCreateRequest request, UUID ownerUserId, RequestMetadata metadata) {
        String slug = uniqueSlug(request.name());
        Store store = new Store(ownerUserId, request.name().trim(), slug,
                request.description(), request.storeType());
        Store saved = storeRepository.save(store);
        auditService.success(AuditEventType.STORE_CREATED, ownerUserId, metadata,
                Map.of("storeId", String.valueOf(saved.getId()), "slug", slug, "type", saved.getStoreType().name()));
        return storeMapper.toResponse(saved);
    }

    @Transactional
    public StoreResponse update(UUID id, StoreUpdateRequest request, UUID actingUserId, boolean isAdmin) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Store", id));
        if (!isAdmin && !store.getOwnerUserId().equals(actingUserId)) {
            throw new AccessDeniedException("You do not have permission to modify this store");
        }
        if (request.name() != null) {
            store.rename(request.name().trim());
        }
        if (request.description() != null) {
            store.describe(request.description());
        }
        if (request.status() != null) {
            // SUSPENDED is a platform moderation action: only an admin may suspend a store
            // or lift a suspension; owners may only toggle ACTIVE/INACTIVE.
            boolean touchesSuspension = request.status() == StoreStatus.SUSPENDED
                    || store.getStatus() == StoreStatus.SUSPENDED;
            if (touchesSuspension && !isAdmin && request.status() != store.getStatus()) {
                throw new AccessDeniedException("Only an admin can suspend or reinstate a store");
            }
            store.changeStatus(request.status());
        }
        return storeMapper.toResponse(store);
    }

    @Transactional
    public void delete(UUID id) {
        if (!storeRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Store", id);
        }
        storeRepository.deleteById(id);
    }

    private String uniqueSlug(String name) {
        String base = Slugs.slugify(name);
        String candidate = base;
        int suffix = 2;
        while (storeRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }
}
