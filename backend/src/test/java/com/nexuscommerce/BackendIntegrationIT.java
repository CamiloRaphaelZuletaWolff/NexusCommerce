package com.nexuscommerce;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexuscommerce.catalog.domain.Product;
import com.nexuscommerce.catalog.domain.ProductStatus;
import com.nexuscommerce.catalog.repository.ProductRepository;
import com.nexuscommerce.catalog.repository.ProductSearchCriteria;
import com.nexuscommerce.catalog.service.CatalogHealthService;
import com.nexuscommerce.users.repository.RoleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full-context integration test against real PostgreSQL + MongoDB (Testcontainers).
 *
 * <p>Named {@code *IT} so it runs under Failsafe ({@code mvn verify}) and is excluded
 * from the Surefire ({@code mvn test}) gate — it requires a Docker-enabled host.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class BackendIntegrationIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("nexuscommerce")
            .withUsername("nexus")
            .withPassword("nexus");

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("app.jwt.secret",
                () -> Base64.getEncoder().encodeToString("integration-test-secret-key-0123456789".getBytes()));
    }

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CatalogHealthService catalogHealthService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void flywaySeedsRolesInPostgres() {
        assertThat(roleRepository.count()).isEqualTo(3);
    }

    @Test
    void mongoReadWriteProbeIsUp() {
        assertThat(catalogHealthService.probe().status()).isEqualTo("UP");
    }

    @Test
    void productSearchAppliesOperatorsAgainstRealMongo() {
        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setStoreId("store-it");
        product.setSku("IT-1");
        product.setName("Widget");
        product.setSlug("widget");
        product.setPrice(new BigDecimal("50.00"));
        product.setCurrency("USD");
        product.setCategorySlug("misc");
        product.setCategoryLabels(List.of("misc"));
        product.setTags(List.of("alpha"));
        product.setBrand("BrandA");
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);

        // tag $in + price $gt/$lt + status — should match.
        var match = productRepository.search(new ProductSearchCriteria(
                null, null, null, List.of("alpha"), null, null,
                new BigDecimal("10"), new BigDecimal("100"), ProductStatus.ACTIVE), PageRequest.of(0, 10));
        assertThat(match.getTotalElements()).isEqualTo(1);

        // price window that excludes the product — should not match.
        var noMatch = productRepository.search(new ProductSearchCriteria(
                null, null, null, List.of("alpha"), null, null,
                new BigDecimal("60"), new BigDecimal("100"), null), PageRequest.of(0, 10));
        assertThat(noMatch.getTotalElements()).isZero();
    }
}
