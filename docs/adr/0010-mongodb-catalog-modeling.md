# ADR-0010: MongoDB catalog modeling and numeric price storage

- **Status:** Accepted
- **Date:** 2026-06-15
- **Phase:** 3

## Context

The product catalog needs category-specific attributes (a laptop has RAM/CPU; a
jacket has material), array-valued fields (tags, category labels, variants,
brands), and numeric range search on price. This is exactly what MongoDB's
flexible documents are for (ADR-0002).

## Decision

- **Documents:** `products`, `categories`, `brands`, plus `customer_profiles`
  (ADR-0011). Each product carries:
  - a dynamic `attributes` map (category-specific fields),
  - array fields `tags`, `categoryLabels`, and embedded `variants`,
  - `storeId` (UUID) linking back to the PostgreSQL store (ADR-0003),
  - `categories` declare an attribute schema (`AttributeDefinition[]`) describing
    the dynamic attributes products in that category should carry.
- **Search** is implemented with `MongoTemplate` + `Criteria` in a custom
  repository fragment, demonstrating the required operators: comparison
  `$gt`/`$lt` (price range), logical `$and` (combined filters) / `$or`
  (name/description), and array `$in` (tags / brands / categoryLabels / variant
  SKUs).
- **Money:** `BigDecimal` prices are stored as BSON **`Decimal128`** via custom
  `BigDecimal ↔ Decimal128` converters. Spring Data's default maps `BigDecimal`
  to a *String*, which makes `$gt`/`$lt` compare lexicographically; storing
  `Decimal128` (and passing `Decimal128` bounds in the price criteria) keeps the
  comparison numeric and precise.

## Consequences

- ✅ Heterogeneous, category-specific products without sparse relational columns.
- ✅ Satisfies the required Mongo operator + array-query academic criteria.
- ✅ Correct, precise numeric price filtering.
- ⚠️ The `BigDecimal`-as-String pitfall is subtle; the converters + a
  Testcontainers integration test guard against regressions.
- ⚠️ Referential integrity to stores is application-enforced (validated on write
  against PostgreSQL by UUID), not DB-enforced.
