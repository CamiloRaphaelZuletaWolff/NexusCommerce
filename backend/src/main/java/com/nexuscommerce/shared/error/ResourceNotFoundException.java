package com.nexuscommerce.shared.error;

/** Thrown when a requested resource does not exist. Mapped to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object identifier) {
        return new ResourceNotFoundException("%s not found: %s".formatted(resource, identifier));
    }
}
