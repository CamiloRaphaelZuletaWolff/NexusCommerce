package com.nexuscommerce.catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexuscommerce.catalog.domain.Product;
import com.nexuscommerce.catalog.domain.ProductStatus;
import java.math.BigDecimal;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

/** Verifies the dynamic search builds the required MongoDB operators ($and/$gt/$lt/$or/$in). */
@ExtendWith(MockitoExtension.class)
class ProductSearchRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ProductSearchRepositoryImpl repository;

    @Test
    void buildsQueryWithComparisonLogicalAndArrayOperators() {
        when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(List.of());

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "store-1", "phone",
                List.of("electronics"), List.of("5g"), List.of("Nexies"), List.of("SKU-1"),
                new BigDecimal("100"), new BigDecimal("1000"), ProductStatus.ACTIVE);

        repository.search(criteria, PageRequest.of(0, 10));

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Product.class));
        String json = captor.getValue().getQueryObject().toJson();

        assertThat(json)
                .contains("$and")  // combined filters
                .contains("$gt")   // minPrice
                .contains("$lt")   // maxPrice
                .contains("$or")   // name/description search
                .contains("$in");  // tags / brands / categoryLabels / variant SKUs
    }

    @Test
    void emptyCriteriaProducesNoFilters() {
        when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(List.of());

        ProductSearchCriteria empty = new ProductSearchCriteria(
                null, null, null, null, null, null, null, null, null);
        repository.search(empty, PageRequest.of(0, 10));

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Product.class));
        Document queryObject = captor.getValue().getQueryObject();
        assertThat(queryObject).isEmpty();
    }
}
