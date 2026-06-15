# ADR-0007: UUID generation strategy

- **Status:** Accepted
- **Date:** 2026-06-14
- **Phase:** 2

## Context

ADR-0003 established UUID as the cross-database key. We must decide *where and how*
UUIDs are generated: by the database (column default) or by the application; and
which version.

## Decision

- **Version:** UUID **v4** (random).
- **Where:** generated **application-side**. A shared `BaseEntity` uses Hibernate's
  `@UuidGenerator(style = RANDOM)`, so Hibernate assigns the id in the JVM at
  persist time (before INSERT).
- **Database safety net:** every UUID primary-key column also declares
  `DEFAULT gen_random_uuid()` (PostgreSQL 16 core), so any out-of-band insert
  still receives a valid id.
- **Cross-datastore entities (Phase 3+):** when the same logical entity must exist
  in both PostgreSQL and MongoDB (the UUID bridge), the **service explicitly
  assigns one UUID** and writes it to both stores, guaranteeing identical ids.

## Consequences

- ✅ Ids are available before commit — usable in responses, links, and for the
  cross-database bridge — without a database round-trip.
- ✅ Non-sequential, collision-free, safe to expose in URLs/APIs.
- ✅ Independent of any single database's id generation.
- ⚠️ v4 UUIDs are not time-ordered (less index-locality than sequential keys);
  acceptable at this scale. Could revisit UUIDv7 if write throughput ever demands it.
