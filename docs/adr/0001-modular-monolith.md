# ADR-0001: Modular monolith with package-by-feature

- **Status:** Accepted
- **Date:** 2026-06-14
- **Phase:** 1

## Context

The platform spans many domains (auth, stores, catalog, cart, orders, payments,
inventory, analytics). We need clear separation of concerns and the ability to
demonstrate clean architecture, but the project must remain easy to run locally
and reason about as a portfolio artifact. Microservices would add service
discovery, network hops, distributed transactions, and deployment complexity that
adds no learning value here.

## Decision

Build the backend as a **modular monolith**, organized **by feature/domain**
rather than by technical layer. Each feature is a self-contained package
(`auth`, `users`, `stores`, `catalog`, `cart`, `orders`, `payments`,
`inventory`, `analytics`, `integration`, `security`, `shared`) with a consistent
internal shape: `api` / `domain` / `repository` / `service` / `mapper`.

## Consequences

- ✅ Strong domain boundaries without distributed-systems overhead.
- ✅ Single deployable, single local process — trivial to run and debug.
- ✅ ACID checkout stays a single local transaction (see ADR-0002/0005).
- ✅ Boundaries are clear enough to extract a service later if ever needed.
- ⚠️ Discipline required to avoid cross-feature coupling; enforced by package
  structure and code review.
