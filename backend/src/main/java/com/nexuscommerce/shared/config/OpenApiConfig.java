package com.nexuscommerce.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI / Swagger metadata + bearer JWT security scheme. */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI nexusOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("NexusCommerce API")
                        .description("Multi-store e-commerce platform — backend API")
                        .version("v0.2 (Phase 2 — Backend Core)")
                        .license(new License().name("Educational / portfolio use")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Access token issued by /api/auth/login")));
    }
}
