package com.nexuscommerce.shared.error;

/** Thrown when a refresh token is missing, unknown, expired or revoked. Mapped to HTTP 401. */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
