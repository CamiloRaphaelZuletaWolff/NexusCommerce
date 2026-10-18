package com.nexuscommerce.bootstrap;

import com.nexuscommerce.catalog.domain.AttributeDefinition;
import com.nexuscommerce.catalog.domain.AttributeType;
import com.nexuscommerce.catalog.domain.Brand;
import com.nexuscommerce.catalog.domain.Category;
import com.nexuscommerce.catalog.domain.Product;
import com.nexuscommerce.catalog.domain.ProductStatus;
import com.nexuscommerce.catalog.domain.Variant;
import com.nexuscommerce.catalog.repository.BrandRepository;
import com.nexuscommerce.catalog.repository.CategoryRepository;
import com.nexuscommerce.catalog.repository.ProductRepository;
import com.nexuscommerce.stores.domain.Store;
import com.nexuscommerce.stores.repository.StoreRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds demo catalog data (categories, brands, products) for local development
 * and the public demo deployment.
 * Runs after {@link DevDataInitializer} (which creates the stores it links to).
 */
@Component
@Profile({"dev", "demo"})
@Order(2)
@RequiredArgsConstructor
public class CatalogDevSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogDevSeeder.class);

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            return;
        }
        Optional<Store> electronics = storeRepository.findBySlug("nexus-electronics");
        Optional<Store> fashion = storeRepository.findBySlug("nexus-fashion");
        if (electronics.isEmpty() || fashion.isEmpty()) {
            log.warn("Demo stores not found; skipping catalog seed");
            return;
        }

        seedCategories();
        seedBrands();

        String elecId = electronics.get().getId().toString();
        String fashId = fashion.get().getId().toString();

        List<Product> products = List.of(
                product(elecId, "ELEC-UB14", "Nexus UltraBook 14", "laptops",
                        List.of("electronics", "laptops"), List.of("laptop", "ultrabook", "portable"),
                        "Nexies", new BigDecimal("1299.00"),
                        Map.of("ramGb", 16, "cpu", "M3", "screenInch", 14),
                        List.of(variant("ELEC-UB14-16-512", Map.of("ram", "16GB", "ssd", "512GB"), null, 10),
                                variant("ELEC-UB14-32-1T", Map.of("ram", "32GB", "ssd", "1TB"),
                                        new BigDecimal("1599.00"), 5))),
                product(elecId, "ELEC-PHX", "Nexus Phone X", "smartphones",
                        List.of("electronics", "smartphones"), List.of("phone", "5g", "portable"),
                        "Nexies", new BigDecimal("899.00"),
                        Map.of("storageGb", 128, "screenInch", 6.1),
                        List.of(variant("ELEC-PHX-BLK", Map.of("color", "black"), null, 25),
                                variant("ELEC-PHX-BLU", Map.of("color", "blue"), null, 18))),
                product(elecId, "ELEC-BUDS", "Acme Noise Buds", "audio",
                        List.of("electronics", "audio"), List.of("audio", "wireless", "earbuds"),
                        "Acme", new BigDecimal("149.99"),
                        Map.of("wireless", true, "batteryHours", 8),
                        List.of(variant("ELEC-BUDS-WHT", Map.of("color", "white"), null, 40))),
                product(fashId, "FASH-TEE", "Vogue Classic Tee", "t-shirts",
                        List.of("fashion", "apparel", "t-shirts"), List.of("shirt", "cotton", "casual"),
                        "Vogue", new BigDecimal("29.99"),
                        Map.of("material", "cotton"),
                        List.of(variant("FASH-TEE-S", Map.of("size", "S"), null, 30),
                                variant("FASH-TEE-M", Map.of("size", "M"), null, 50),
                                variant("FASH-TEE-L", Map.of("size", "L"), null, 20))),
                product(fashId, "FASH-JKT", "UrbanWear Rain Jacket", "jackets",
                        List.of("fashion", "apparel", "outerwear"), List.of("jacket", "waterproof"),
                        "UrbanWear", new BigDecimal("119.50"),
                        Map.of("material", "nylon", "waterproof", true),
                        List.of(variant("FASH-JKT-M", Map.of("size", "M"), null, 12),
                                variant("FASH-JKT-L", Map.of("size", "L"), null, 9))),
                product(fashId, "FASH-SNK", "UrbanWear Street Runner", "footwear",
                        List.of("fashion", "footwear"), List.of("shoes", "sneakers", "casual"),
                        "UrbanWear", new BigDecimal("79.00"),
                        Map.of("material", "mesh"),
                        List.of(variant("FASH-SNK-42", Map.of("size", "42"), null, 15),
                                variant("FASH-SNK-43", Map.of("size", "43"), null, 11))));

        productRepository.saveAll(products);
        log.info("Seeded demo catalog: {} products", productRepository.count());
    }

    private void seedCategories() {
        categoryRepository.saveAll(List.of(
                category("laptops", "Laptops",
                        attr("ramGb", "RAM", AttributeType.NUMBER, "GB"),
                        attr("cpu", "CPU", AttributeType.STRING, null),
                        attr("screenInch", "Screen", AttributeType.NUMBER, "in")),
                category("smartphones", "Smartphones",
                        attr("storageGb", "Storage", AttributeType.NUMBER, "GB"),
                        attr("screenInch", "Screen", AttributeType.NUMBER, "in")),
                category("audio", "Audio", attr("wireless", "Wireless", AttributeType.BOOLEAN, null)),
                category("t-shirts", "T-Shirts", attr("material", "Material", AttributeType.STRING, null)),
                category("jackets", "Jackets", attr("material", "Material", AttributeType.STRING, null)),
                category("footwear", "Footwear", attr("material", "Material", AttributeType.STRING, null))));
    }

    private void seedBrands() {
        brandRepository.saveAll(List.of(
                brand("Nexies", "First-party Nexus electronics"),
                brand("Acme", "Audio and accessories"),
                brand("Vogue", "Everyday fashion essentials"),
                brand("UrbanWear", "Streetwear and outerwear")));
    }

    private Category category(String slug, String name, AttributeDefinition... attributes) {
        Category category = new Category();
        category.setId(UUID.randomUUID().toString());
        category.setSlug(slug);
        category.setName(name);
        category.setAttributes(List.of(attributes));
        category.setCreatedAt(Instant.now());
        return category;
    }

    private AttributeDefinition attr(String key, String label, AttributeType type, String unit) {
        return new AttributeDefinition(key, label, type, false, unit);
    }

    private Brand brand(String name, String description) {
        Brand brand = new Brand();
        brand.setId(UUID.randomUUID().toString());
        brand.setSlug(name.toLowerCase());
        brand.setName(name);
        brand.setDescription(description);
        brand.setCreatedAt(Instant.now());
        return brand;
    }

    private Variant variant(String sku, Map<String, Object> attributes, BigDecimal priceOverride, int stock) {
        return new Variant(sku, attributes, priceOverride, stock);
    }

    private Product product(String storeId, String sku, String name, String categorySlug,
                            List<String> categoryLabels, List<String> tags, String brand,
                            BigDecimal price, Map<String, Object> attributes, List<Variant> variants) {
        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setStoreId(storeId);
        product.setSku(sku);
        product.setName(name);
        product.setSlug(sku.toLowerCase());
        product.setDescription(name + " — demo catalog item");
        product.setPrice(price);
        product.setCurrency("USD");
        product.setCategorySlug(categorySlug);
        product.setCategoryLabels(categoryLabels);
        product.setTags(tags);
        product.setBrand(brand);
        product.setVariants(variants);
        product.setAttributes(attributes);
        product.setStatus(ProductStatus.ACTIVE);
        Instant now = Instant.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return product;
    }
}
