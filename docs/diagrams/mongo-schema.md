# MongoDB catalog schema (database `nexuscatalog`)

The flexible document store. Each category carries its own attribute shape, which a
relational schema would model awkwardly (sparse columns / EAV). Collections:
`products`, `categories`, `brands`, `customer_profiles`.

## Document shapes

```jsonc
// products  — _id is the product UUID; storeId bridges to PostgreSQL stores
{
  "_id": "6f47e1fa-…",            // product UUID  (== cart_items.product_id, inventory.product_id)
  "storeId": "a1b2c3d4-…",        // -> PostgreSQL commerce.stores.id  (the UUID bridge)
  "sku": "ELEC-UB14",
  "name": "Nexus UltraBook 14",
  "slug": "elec-ub14",
  "price": NumberDecimal("1299.00"), // Decimal128 → numeric range comparisons (ADR-0010)
  "currency": "USD",
  "categorySlug": "laptops",
  "categoryLabels": ["electronics", "laptops"],   // array  ($in target)
  "tags": ["laptop", "ultrabook", "portable"],     // array  ($in target)
  "brand": "Nexies",                                // ($in target)
  "variants": [                                     // array of objects (variants.sku $in target)
    { "sku": "…-16-512", "attributes": { "ram": "16GB" }, "stock": 10 }
  ],
  "attributes": { "ramGb": 16, "cpu": "M3", "screenInch": 14 }, // category-specific, dynamic
  "status": "ACTIVE"
}

// categories — defines the attribute schema each category expects
{ "_id": "…", "slug": "laptops", "name": "Laptops",
  "attributes": [ { "key": "ramGb", "label": "RAM", "type": "NUMBER", "required": true, "unit": "GB" } ] }

// brands
{ "_id": "…", "slug": "nexies", "name": "Nexies", "description": "…" }

// customer_profiles — _id == PostgreSQL users.id (the bridge); written bidirectionally (Phase 6)
{ "_id": "b0ca05c3-…",            // == commerce.users.id
  "email": "customer@nexus.local",
  "favoriteCategories": ["electronics", "laptops"],
  "favoriteBrands": ["Nexies"],
  "recentlyViewedProductIds": ["6f47e1fa-…"] }   // most-recent-first, capped at 12
```

## Required query operators (demonstrated in `ProductSearchRepositoryImpl`)

```js
db.products.find({ $and: [                                  // $and — combine filters
  { status: "ACTIVE" },
  { price: { $gt: NumberDecimal("200"), $lt: NumberDecimal("1000") } }, // $gt / $lt
  { tags: { $in: ["portable"] } },                          // array $in
  { categoryLabels: { $in: ["electronics"] } },             // array $in
  { brand: { $in: ["Nexies"] } },                           // array $in
  { "variants.sku": { $in: ["ELEC-UB14-16-512"] } },        // array-of-objects $in
  { $or: [ { name: /nexus/i }, { description: /nexus/i } ] }// $or — free-text
]})
```

| Operator | Where | Field |
|---|---|---|
| `$gt` / `$lt` | price range | `price` (Decimal128) |
| `$and` | combines all active filters | top-level |
| `$or` | free-text search | `name`, `description` |
| `$in` | array membership | `tags`, `brand`, `categoryLabels`, `variants.sku` |
