package com.nexuscommerce.catalog.repository;

import com.nexuscommerce.catalog.domain.Product;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String>, ProductSearchRepository {

    Optional<Product> findBySlug(String slug);

    long countByStoreId(String storeId);
}
