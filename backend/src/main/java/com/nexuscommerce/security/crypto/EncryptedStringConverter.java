package com.nexuscommerce.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Transparently encrypts a String column at rest (AES-256-GCM).
 * Apply with {@code @Convert(converter = EncryptedStringConverter.class)} on entity fields.
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return CryptoSupport.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return CryptoSupport.decrypt(dbData);
    }
}
