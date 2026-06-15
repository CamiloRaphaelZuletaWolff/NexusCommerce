package com.nexuscommerce.auth.dto;

import com.nexuscommerce.users.api.dto.UserResponse;

/** Returned on register/login/refresh. The refresh token is delivered separately via cookie. */
public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, UserResponse user) {

    public static AuthResponse of(String accessToken, long expiresInSeconds, UserResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresInSeconds, user);
    }
}
