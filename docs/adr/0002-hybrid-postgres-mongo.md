# ADR-0002: Hybrid persistence — PostgreSQL + MongoDB

- **Status:** Accepted
- **Date:** 2026-06-14
- **Phase:** 1

## Context

The platform has two very different data shapes:

1. **Transactional/relational** — users, orders, payments, inventory. These need
   strong consistency, relationships, ACID guarantees, and benefit from
   normalization (3NF), views, stored procedures, and subqueries.
2. **Flexible catalog** — products whose attributes differ by category (a laptop
   has CPU/RAM; a sofa has material/dimensions). Forcing this into relational
   tables leads to sparse columns or an awkward EAV model.

## Decision

Use **PostgreSQL** for all transactional/relational data and **MongoDB** for the
product catalog. Each database is used for what it is genuinely best at, rather
than forcing one tool to do both jobs.

- PostgreSQL: users, roles, stores, orders, order_items, payments, invoices,
  addresses, audit_logs, cart metadata, transactional inventory references.
- MongoDB: products, categories, category-specific attributes, tags, variants,
  brand metadata, recommendation metadata.

## Consequences

- ✅ Natural fit for both data shapes; demonstrates polyglot persistence.
- ✅ Enables required academic features on each side (ACID/views/procedures on
  Postgres; flexible documents and array/operator queries on Mongo).
- ⚠️ No native cross-database joins or foreign keys → resolved by the UUID bridge
  (ADR-0003) and an `integration` sync module.
- ⚠️ Two datastores to operate locally → mitigated by Docker Compose with
  healthchecks (Phase 1).
