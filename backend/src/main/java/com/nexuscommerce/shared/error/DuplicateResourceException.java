package com.nexuscommerce.shared.error;

/** Thrown when creating a resource that violates a uniqueness constraint. Mapped to HTTP 409. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
