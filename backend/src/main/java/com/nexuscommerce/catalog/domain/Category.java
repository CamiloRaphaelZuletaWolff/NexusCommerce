package com.nexuscommerce.catalog.domain;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Product category, including the dynamic-attribute schema products in it should carry. */
@Document("categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;

    private String name;
    private String parentSlug;
    private List<AttributeDefinition> attributes;
    private Instant createdAt;
}
