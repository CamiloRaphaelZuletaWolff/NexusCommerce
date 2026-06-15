package com.nexuscommerce.catalog.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Declares a dynamic attribute that products in a category are expected to carry. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDefinition {

    private String key;
    private String label;
    private AttributeType type;
    private boolean required;
    private String unit; // optional, e.g. "GB", "cm"
}
