# ADR-0011: Event-driven cross-database customer sync

- **Status:** Accepted
- **Date:** 2026-06-15
- **Phase:** 3

## Context

Requirement #11 calls for a service layer that synchronizes customer data between
PostgreSQL and MongoDB through the shared UUID. The relational `users` table is
the system of record for identity; the catalog side needs a customer profile
(recommendation metadata) keyed by the same id. The `users` module must not
depend on the catalog/Mongo module.

## Decision

- On registration, `UserService` publishes a `UserRegisteredEvent`
  (`userId`, `email`, `fullName`) via Spring's `ApplicationEventPublisher`.
- A listener in the `integration` module
  (`@TransactionalEventListener(AFTER_COMMIT)`) upserts a MongoDB
  `CustomerProfile` whose `_id` **equals the PostgreSQL user UUID** — the
  cross-database bridge (ADR-0003).
- Running **after commit** means a rolled-back registration never creates an
  orphan Mongo profile. Failures are logged and never propagate (eventual
  consistency).

## Consequences

- ✅ Concrete demonstration of UUID-keyed PostgreSQL → MongoDB synchronization.
- ✅ Decoupled: `users` depends only on its own event; `integration` wires the
  two stores together. No circular module dependencies.
- ✅ Correctness: the Mongo write is tied to the relational commit.
- ⚠️ Eventual consistency — if the Mongo write fails, the profile is missing until
  re-synced. Acceptable for non-critical recommendation metadata; a retry/outbox
  could be added later if needed.
- ↪️ The same pattern generalizes to other cross-store projections (e.g. store
  reference data) in future phases.
