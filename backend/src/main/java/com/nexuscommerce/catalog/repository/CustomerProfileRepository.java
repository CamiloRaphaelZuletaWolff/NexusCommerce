package com.nexuscommerce.catalog.repository;

import com.nexuscommerce.catalog.domain.CustomerProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerProfileRepository extends MongoRepository<CustomerProfile, String> {
}
