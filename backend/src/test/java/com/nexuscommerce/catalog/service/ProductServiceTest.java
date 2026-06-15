package com.nexuscommerce.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexuscommerce.audit.domain.AuditEventType;
import com.nexuscommerce.audit.service.AuditService;
import com.nexuscommerce.catalog.api.dto.ProductCreateRequest;
import com.nexuscommerce.catalog.api.dto.ProductResponse;
import com.nexuscommerce.catalog.domain.Product;
import com.nexuscommerce.catalog.domain.ProductStatus;
import com.nexuscommerce.catalog.repository.ProductRepository;
import com.nexuscommerce.shared.web.RequestMetadata;
import com.nexuscommerce.stores.api.dto.StoreResponse;
import com.nexuscommerce.stores.domain.StoreStatus;
import com.nexuscommerce.stores.domain.StoreType;
import com.nexuscommerce.stores.service.StoreService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private StoreService storeService;
    @Mock private AuditService auditService;

    @InjectMocks private ProductService productService;

    private final RequestMetadata metadata = new RequestMetadata("127.0.0.1", "JUnit");

    private StoreResponse store(UUID storeId, UUID ownerId) {
        return new StoreResponse(storeId, ownerId, "Shop", "shop", "d",
                StoreType.ELECTRONICS, StoreStatus.ACTIVE, null, null);
    }

    private ProductCreateRequest request(UUID storeId) {
        return new ProductCreateRequest(storeId, "SKU1", "Nexus Phone", "desc",
                new BigDecimal("99.99"), "usd", "smartphones",
                List.of("electronics"), List.of("5g"), "Nexies", List.of(),
                Map.of("storageGb", 128), null);
    }

    @Test
    void ownerCanCreateProductForOwnStore() {
        UUID storeId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(storeService.getByIdOrThrow(storeId)).thenReturn(store(storeId, ownerId));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.create(request(storeId), ownerId, false, metadata);

        assertThat(response.sku()).isEqualTo("SKU1");
        assertThat(response.currency()).isEqualTo("USD");          // normalized
        assertThat(response.storeId()).isEqualTo(storeId.toString());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ProductStatus.ACTIVE); // default
        verify(auditService).success(eq(AuditEventType.PRODUCT_CREATED), eq(ownerId), eq(metadata), any());
    }

    @Test
    void nonOwnerCannotCreateProduct() {
        UUID storeId = UUID.randomUUID();
        when(storeService.getByIdOrThrow(storeId)).thenReturn(store(storeId, UUID.randomUUID()));

        assertThatThrownBy(() -> productService.create(request(storeId), UUID.randomUUID(), false, metadata))
                .isInstanceOf(AccessDeniedException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void adminCanCreateProductForAnyStore() {
        UUID storeId = UUID.randomUUID();
        when(storeService.getByIdOrThrow(storeId)).thenReturn(store(storeId, UUID.randomUUID()));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.create(request(storeId), UUID.randomUUID(), true, metadata);

        assertThat(response.sku()).isEqualTo("SKU1");
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(productRepository.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById("ghost"))
                .isInstanceOf(com.nexuscommerce.shared.error.ResourceNotFoundException.class);
    }
}
