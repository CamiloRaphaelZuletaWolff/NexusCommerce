package com.nexuscommerce.users.service;

import com.nexuscommerce.shared.error.DuplicateResourceException;
import com.nexuscommerce.shared.error.ResourceNotFoundException;
import com.nexuscommerce.users.domain.Role;
import com.nexuscommerce.users.domain.User;
import com.nexuscommerce.users.event.UserRegisteredEvent;
import com.nexuscommerce.users.repository.RoleRepository;
import com.nexuscommerce.users.repository.UserRepository;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** User lifecycle and lookups. Centralizes user creation so passwords are always hashed. */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public User register(String email, String fullName, String rawPassword, String roleName) {
        String normalizedEmail = normalize(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email already registered: " + normalizedEmail);
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not configured: " + roleName));

        User user = new User(normalizedEmail, passwordEncoder.encode(rawPassword), fullName);
        user.addRole(role);
        User saved = userRepository.save(user);

        // Cross-DB sync trigger: a Mongo customer profile is created after commit (ADR-0003).
        eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId(), saved.getEmail(), saved.getFullName()));
        return saved;
    }

    /** True if {@code rawPassword} is the user's current password. */
    public boolean passwordMatches(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    /** Sets a new password and (re-)enables the account. */
    @Transactional
    public void resetPassword(UUID userId, String rawPassword) {
        User user = getByIdOrThrow(userId);
        user.changePasswordHash(passwordEncoder.encode(rawPassword));
        user.enable();
        userRepository.save(user);
    }

    @Transactional
    public void disable(UUID userId) {
        User user = getByIdOrThrow(userId);
        user.disable();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getByIdOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalize(email));
    }

    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
