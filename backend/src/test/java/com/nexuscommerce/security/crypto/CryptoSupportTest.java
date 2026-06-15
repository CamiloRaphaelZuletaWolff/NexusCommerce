package com.nexuscommerce.security.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CryptoSupportTest {

    @BeforeAll
    static void initKey() {
        CryptoSupport.initialize(Base64.getDecoder().decode("kCh2wkMoNc9+NNU11xepaMDcIW9rG3KkCSP6XBIH/TA="));
    }

    @Test
    void encryptThenDecryptRoundTrips() {
        String plaintext = "4111111111111111";
        String ciphertext = CryptoSupport.encrypt(plaintext);

        assertThat(ciphertext).isNotEqualTo(plaintext);
        assertThat(CryptoSupport.decrypt(ciphertext)).isEqualTo(plaintext);
    }

    @Test
    void nullsPassThrough() {
        assertThat(CryptoSupport.encrypt(null)).isNull();
        assertThat(CryptoSupport.decrypt(null)).isNull();
    }

    @Test
    void usesRandomIvSoCiphertextDiffersEachTime() {
        assertThat(CryptoSupport.encrypt("same")).isNotEqualTo(CryptoSupport.encrypt("same"));
    }
}
