package com.nexuscommerce.security.crypto;

import jakarta.annotation.PostConstruct;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Installs the AES key into {@link CryptoSupport} at startup, before any persistence. */
@Component
@RequiredArgsConstructor
public class EncryptionInitializer {

    private final AppEncryptionProperties properties;

    @PostConstruct
    void init() {
        CryptoSupport.initialize(Base64.getDecoder().decode(properties.key()));
    }
}
