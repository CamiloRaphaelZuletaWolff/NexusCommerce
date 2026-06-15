package com.nexuscommerce;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexuscommerce.catalog.service.CatalogHealthService;
import com.nexuscommerce.users.repository.RoleRepository;
import java.util.Base64;
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

    @Test
    void flywaySeedsRolesInPostgres() {
        assertThat(roleRepository.count()).isEqualTo(3);
    }

    @Test
    void mongoReadWriteProbeIsUp() {
        assertThat(catalogHealthService.probe().status()).isEqualTo("UP");
    }
}
