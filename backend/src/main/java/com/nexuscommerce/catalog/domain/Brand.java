package com.nexuscommerce.catalog.domain;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("brands")
@Getter
@Setter
@NoArgsConstructor
public class Brand {

    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;

    private String name;
    private String description;
    private Instant createdAt;
}
