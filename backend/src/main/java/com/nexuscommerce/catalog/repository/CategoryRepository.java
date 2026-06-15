package com.nexuscommerce.catalog.repository;

import com.nexuscommerce.catalog.domain.Category;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
