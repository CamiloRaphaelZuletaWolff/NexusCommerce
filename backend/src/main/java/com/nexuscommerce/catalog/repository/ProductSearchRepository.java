package com.nexuscommerce.catalog.repository;

import com.nexuscommerce.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Custom repository fragment for dynamic product search (implemented with MongoTemplate). */
public interface ProductSearchRepository {

    Page<Product> search(ProductSearchCriteria criteria, Pageable pageable);
}
