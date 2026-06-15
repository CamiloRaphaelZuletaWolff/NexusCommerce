package com.nexuscommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * NexusCommerce backend entry point.
 *
 * <p>Modular monolith over a hybrid datastore: PostgreSQL (transactional, via JPA)
 * and MongoDB (flexible catalog). See {@code docs/ARCHITECTURE.md}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
public class NexusCommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusCommerceApplication.class, args);
    }
}
