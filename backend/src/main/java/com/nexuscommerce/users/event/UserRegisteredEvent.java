package com.nexuscommerce.users.event;

import java.util.UUID;

/** Published after a user is registered (and committed). Consumed by the cross-DB sync. */
public record UserRegisteredEvent(UUID userId, String email, String fullName) {
}
