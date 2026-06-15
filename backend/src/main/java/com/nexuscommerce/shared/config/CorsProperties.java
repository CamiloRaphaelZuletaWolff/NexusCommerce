package com.nexuscommerce.shared.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** CORS configuration. Origins must be explicit (no wildcard) because the refresh
 *  cookie requires {@code allow-credentials: true}. */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
