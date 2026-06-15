# ADR-0003: UUID as the cross-database integration key

- **Status:** Accepted
- **Date:** 2026-06-14
- **Phase:** 1

## Context

With data split across PostgreSQL and MongoDB (ADR-0002), there is no native
foreign key or join across the two. We still need stable, collision-free
references — e.g. a PostgreSQL `order_item` must point at a MongoDB product, and
store/customer data must stay coherent across both stores.

## Decision

Use **UUID (v4 / GUID)** as the shared identifier for cross-database entities,
primarily `storeId` and `productId`. UUIDs are generated such that the same value
identifies an entity on both sides:

- PostgreSQL: UUID primary keys (via `pgcrypto` `gen_random_uuid()`).
- MongoDB: the same UUID stored as the document's business key (alongside Mongo's
  own `_id`).
- The `integration` module synchronizes customer/store data between the two
  databases keyed by UUID.

## Consequences

- ✅ Globally unique, non-sequential keys safe to expose in APIs/URLs.
- ✅ Decouples the relational and document models; either side can be migrated
  independently.
- ✅ Directly satisfies the "cross-database integration via UUID/GUID" requirement.
- ⚠️ UUIDs are larger than integers and not naturally ordered → acceptable at this
  scale; indexes are defined where lookups happen.
- ⚠️ Referential integrity across DBs is application-enforced, not DB-enforced →
  handled in the `integration`/service layer with validation.
