package com.nexuscommerce.shared.config;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** CORS configuration. Origins must be explicit (no wildcard) because the refresh
 *  cookie requires {@code allow-credentials: true}. */
@Validated
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(@NotEmpty List<String> allowedOrigins) {
}
