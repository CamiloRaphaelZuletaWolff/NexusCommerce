package com.nexuscommerce.auth.service;

import com.nexuscommerce.users.api.dto.UserResponse;
import java.time.Instant;

/** Internal result of an auth operation: access token (body) + raw refresh token (cookie). */
public record AuthResult(
        String accessToken,
        long accessTtlSeconds,
        String refreshToken,
        Instant refreshExpiresAt,
        UserResponse user) {
}
