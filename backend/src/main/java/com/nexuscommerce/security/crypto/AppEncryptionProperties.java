package com.nexuscommerce.security.crypto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Column-encryption key (Base64-encoded AES key), bound from {@code app.encryption.*}. */
@Validated
@ConfigurationProperties(prefix = "app.encryption")
public record AppEncryptionProperties(@NotBlank String key) {
}
