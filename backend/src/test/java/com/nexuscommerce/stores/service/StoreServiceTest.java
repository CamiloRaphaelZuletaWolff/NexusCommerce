package com.nexuscommerce.stores.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexuscommerce.audit.domain.AuditEventType;
import com.nexuscommerce.audit.service.AuditService;
import com.nexuscommerce.shared.error.ResourceNotFoundException;
import com.nexuscommerce.shared.web.RequestMetadata;
import com.nexuscommerce.stores.api.dto.StoreCreateRequest;
import com.nexuscommerce.stores.api.dto.StoreResponse;
import com.nexuscommerce.stores.api.dto.StoreUpdateRequest;
import com.nexuscommerce.stores.domain.Store;
import com.nexuscommerce.stores.domain.StoreStatus;
import com.nexuscommerce.stores.domain.StoreType;
import com.nexuscommerce.stores.mapper.StoreMapper;
import com.nexuscommerce.stores.repository.StoreRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock private StoreRepository storeRepository;
    @Mock private StoreMapper storeMapper;
    @Mock private AuditService auditService;

    private StoreService storeService;

    private final RequestMetadata metadata = new RequestMetadata("127.0.0.1", "JUnit");

    private StoreService service() {
        return new StoreService(storeRepository, storeMapper, auditService);
    }

    @Test
    void createGeneratesSlugAndAudits() {
        storeService = service();
        UUID ownerId = UUID.randomUUID();
        when(storeRepository.existsBySlug(any())).thenReturn(false);
        when(storeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        StoreResponse mapped = new StoreResponse(null, ownerId, "My Shop", "my-shop", "d",
                StoreType.ELECTRONICS, StoreStatus.ACTIVE, null, null);
        when(storeMapper.toResponse(any())).thenReturn(mapped);

        StoreResponse result = storeService.create(
                new StoreCreateRequest("My Shop", "d", StoreType.ELECTRONICS), ownerId, metadata);

        assertThat(result).isEqualTo(mapped);
        ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
        verify(storeRepository).save(captor.capture());
        assertThat(captor.getValue().getSlug()).isEqualTo("my-shop");
        assertThat(captor.getValue().getOwnerUserId()).isEqualTo(ownerId);
        verify(auditService).success(eq(AuditEventType.STORE_CREATED), eq(ownerId), eq(metadata), any());
    }

    @Test
    void getBySlugThrowsWhenMissing() {
        storeService = service();
        when(storeRepository.findBySlug("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.getBySlug("ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateDeniedForNonOwnerNonAdmin() {
        storeService = service();
        UUID storeId = UUID.randomUUID();
        Store store = new Store(UUID.randomUUID(), "Old", "old", "d", StoreType.ELECTRONICS);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> storeService.update(storeId,
                new StoreUpdateRequest("New", null, null), UUID.randomUUID(), false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminCanUpdateAnyStore() {
        storeService = service();
        UUID storeId = UUID.randomUUID();
        Store store = new Store(UUID.randomUUID(), "Old", "old", "d", StoreType.ELECTRONICS);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(storeMapper.toResponse(store)).thenReturn(
                new StoreResponse(null, store.getOwnerUserId(), "New", "old", "d",
                        StoreType.ELECTRONICS, StoreStatus.SUSPENDED, null, null));

        storeService.update(storeId, new StoreUpdateRequest("New", null, StoreStatus.SUSPENDED),
                UUID.randomUUID(), true);

        assertThat(store.getName()).isEqualTo("New");
        assertThat(store.getStatus()).isEqualTo(StoreStatus.SUSPENDED);
    }

    @Test
    void listDefaultsToActiveStores() {
        storeService = service();
        when(storeRepository.findByStatus(eq(StoreStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        storeService.list(null, Pageable.unpaged());

        verify(storeRepository).findByStatus(eq(StoreStatus.ACTIVE), any(Pageable.class));
    }
}
