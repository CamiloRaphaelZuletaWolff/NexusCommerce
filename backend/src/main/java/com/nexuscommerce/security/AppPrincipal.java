package com.nexuscommerce.security;

import java.util.UUID;

/** Authenticated principal carried in the security context (derived from the access token). */
public record AppPrincipal(UUID userId, String email) {
}
