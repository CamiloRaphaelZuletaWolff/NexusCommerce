package com.nexuscommerce.catalog.repository;

import com.nexuscommerce.catalog.domain.Product;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.bson.types.Decimal128;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Dynamic product search built with MongoTemplate + Criteria.
 *
 * <p>Demonstrates the required MongoDB query operators:
 * <ul>
 *   <li>comparison {@code $gt}/{@code $lt} — price range,</li>
 *   <li>logical {@code $and} — combined filters, {@code $or} — name/description search,</li>
 *   <li>array membership {@code $in} — tags, brands, category labels, variant SKUs.</li>
 * </ul>
 */
@RequiredArgsConstructor
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Product> search(ProductSearchCriteria criteria, Pageable pageable) {
        List<Criteria> filters = new ArrayList<>();

        if (StringUtils.hasText(criteria.storeId())) {
            filters.add(Criteria.where("storeId").is(criteria.storeId()));
        }
        if (criteria.status() != null) {
            filters.add(Criteria.where("status").is(criteria.status().name()));
        }

        // Comparison operators: $gt / $lt on price. Prices are stored as Decimal128,
        // so the query bounds are passed as Decimal128 to keep the comparison numeric
        // (a BigDecimal bound would be mapped to a String and never match).
        Decimal128 min = toDecimal128(criteria.minPrice());
        Decimal128 max = toDecimal128(criteria.maxPrice());
        if (min != null && max != null) {
            filters.add(Criteria.where("price").gt(min).lt(max));
        } else if (min != null) {
            filters.add(Criteria.where("price").gt(min));
        } else if (max != null) {
            filters.add(Criteria.where("price").lt(max));
        }

        // Array membership: $in over tags / brands / categoryLabels / variant SKUs.
        if (!CollectionUtils.isEmpty(criteria.tags())) {
            filters.add(Criteria.where("tags").in(criteria.tags()));
        }
        if (!CollectionUtils.isEmpty(criteria.brands())) {
            filters.add(Criteria.where("brand").in(criteria.brands()));
        }
        if (!CollectionUtils.isEmpty(criteria.categories())) {
            filters.add(Criteria.where("categoryLabels").in(criteria.categories()));
        }
        if (!CollectionUtils.isEmpty(criteria.variantSkus())) {
            filters.add(Criteria.where("variants.sku").in(criteria.variantSkus()));
        }

        // Logical $or over name/description for free-text search.
        if (StringUtils.hasText(criteria.q())) {
            String safe = Pattern.quote(criteria.q().trim());
            filters.add(new Criteria().orOperator(
                    Criteria.where("name").regex(safe, "i"),
                    Criteria.where("description").regex(safe, "i")));
        }

        Query query = new Query();
        if (!filters.isEmpty()) {
            // Combine all filters with logical $and.
            query.addCriteria(new Criteria().andOperator(filters));
        }

        long total = mongoTemplate.count(query, Product.class);
        query.with(pageable);
        List<Product> content = mongoTemplate.find(query, Product.class);
        return new PageImpl<>(content, pageable, total);
    }

    private static Decimal128 toDecimal128(BigDecimal value) {
        return value == null ? null : new Decimal128(value);
    }
}
