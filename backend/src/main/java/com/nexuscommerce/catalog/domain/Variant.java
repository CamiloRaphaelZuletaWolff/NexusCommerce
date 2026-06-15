package com.nexuscommerce.catalog.domain;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A purchasable variant of a product (e.g. a specific colour/size), embedded in the product document. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Variant {

    private String sku;
    private Map<String, Object> attributes; // e.g. {"color": "red", "size": "M"}
    private BigDecimal priceOverride;        // null => inherit product price
    private Integer stock;
}
