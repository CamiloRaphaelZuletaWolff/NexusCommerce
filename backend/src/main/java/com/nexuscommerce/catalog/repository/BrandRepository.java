package com.nexuscommerce.catalog.repository;

import com.nexuscommerce.catalog.domain.Brand;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BrandRepository extends MongoRepository<Brand, String> {

    Optional<Brand> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
