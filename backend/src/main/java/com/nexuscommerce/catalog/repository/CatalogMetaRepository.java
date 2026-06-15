package com.nexuscommerce.catalog.repository;

import com.nexuscommerce.catalog.domain.CatalogMeta;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CatalogMetaRepository extends MongoRepository<CatalogMeta, String> {
}
