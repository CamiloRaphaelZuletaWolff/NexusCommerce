package com.nexuscommerce.catalog.service;

import com.nexuscommerce.catalog.api.dto.CustomerProfileResponse;
import com.nexuscommerce.catalog.repository.CustomerProfileRepository;
import com.nexuscommerce.shared.error.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Reads the MongoDB customer profile that is synced from the PostgreSQL user by UUID. */
@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerProfileRepository customerProfileRepository;

    public CustomerProfileResponse getForUser(UUID userId) {
        return customerProfileRepository.findById(userId.toString())
                .map(CustomerProfileResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer profile", userId));
    }
}
