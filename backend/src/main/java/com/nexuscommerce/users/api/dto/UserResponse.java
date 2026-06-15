package com.nexuscommerce.users.api.dto;

import com.nexuscommerce.users.domain.User;
import java.util.Set;
import java.util.UUID;

/** Public representation of a user (never exposes the password hash). */
public record UserResponse(UUID id, String email, String fullName, boolean enabled, Set<String> roles) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.isEnabled(),
                user.roleNames());
    }
}
