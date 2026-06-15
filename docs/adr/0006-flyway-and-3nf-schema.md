# ADR-0006: Flyway-managed, 3NF relational schema

- **Status:** Accepted
- **Date:** 2026-06-14
- **Phase:** 2

## Context

The PostgreSQL schema must be versioned, reproducible, reviewable, and normalized
(an academic requirement). Hibernate `ddl-auto` schema generation is convenient
but non-deterministic and unreviewable, and it drifts from intent.

## Decision

- **Flyway owns the schema.** All DDL and reference data live in versioned
  migrations under `db/migration` (`V1`–`V5` in Phase 2). Hibernate runs with
  `ddl-auto: validate` — it never creates or alters tables, only verifies that the
  entity mappings match the Flyway-built schema.
- Objects live in a dedicated `commerce` schema (Flyway `default-schema=commerce`).
- The model is normalized to **3NF**: atomic columns, every non-key attribute
  fully dependent on the key, the user↔role many-to-many resolved via a
  `user_roles` junction table, no transitive dependencies. CHECK constraints
  enforce enumerated columns (`store_type`, `status`, audit `outcome`).
- Stable reference data (roles) is seeded by migration; demo users/stores are
  seeded by a dev-only runner so passwords go through the real encoder.

## Consequences

- ✅ Deterministic, reviewable, and replayable schema; drift is caught at startup
  by `validate`.
- ✅ Clean separation: structure in SQL migrations, behavior in Java.
- ⚠️ Every schema change requires a new migration (never edit an applied one) —
  enforced by convention and Flyway's checksum validation.
