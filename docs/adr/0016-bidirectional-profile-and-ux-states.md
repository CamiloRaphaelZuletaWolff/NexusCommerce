# ADR-0016: Bidirectional customer profile & frontend UX states

- **Status:** Accepted
- **Date:** 2026-06-21
- **Phase:** 6

## Context

Phase 6 is integration polish: end-to-end wiring and UX states. Two gaps remained
after Phase 5:

1. The PostgreSQL↔MongoDB customer profile (ADR-0011) was **write-once** (created on
   registration) and **invisible** — the UUID bridge existed in code but the user
   never saw or fed it. The assignment's "Integración" requirement asks for customer
   records linked to their *preferences* in MongoDB by UUID; preferences were not yet
   real.
2. The frontend had strong primitives (Skeleton, RequireAuth, toast, `ApiError`) but
   no route-level error boundaries and no streaming loading states, and empty/error
   handling was ad-hoc per page.

## Decision

**Bidirectional profile (the bridge, made two-way and visible).**

- Added `PATCH /api/catalog/me/profile` (update favorite categories/brands) and
  `POST /api/catalog/me/recently-viewed/{productId}` (record a view). Both are scoped
  to the authenticated caller via `@PreAuthorize("isAuthenticated()")`, take/return
  DTOs, and write to the MongoDB `customer_profiles` document keyed by the caller's
  PostgreSQL user UUID. Recently-viewed is de-duplicated, most-recent-first, and
  capped (12) to keep the write cheap. No new cross-DB foreign key — the UUID-reference
  contract (ADR-0003) is preserved.
- Added a customer **`/account`** page whose signature element visualizes the bridge:
  the same UUID shown as the PostgreSQL `users` primary key and the MongoDB
  `customer_profiles` `_id`. Favorites are editable (write-back), and product views
  are recorded fire-and-forget from the product page, surfacing as "Recently viewed".

**UX states (Next.js conventions).**

- `global-error.tsx` (root boundary with its own `<html>/<body>`), segment
  `error.tsx` for `(storefront)` and `dashboard` (keep the shell, offer retry), and
  route-level `loading.tsx` skeletons for the dynamic data routes.
- Reusable `<EmptyState>` (successful-but-empty) and `<ErrorState>` (request failed,
  with retry) so failures and emptiness read consistently, in the interface voice.

## Consequences

- ✅ The cross-database integration is now interactive and demonstrable end-to-end:
  edit a favorite → see it in `customer_profiles`; open a product → it appears in
  "Recently viewed", all keyed by the shared UUID.
- ✅ Resilience: a thrown render error or an unreachable backend now yields a branded,
  recoverable state instead of a blank screen; navigations stream a skeleton.
- ✅ No architectural change — same modular monolith, same UUID bridge, no new
  cross-store FK.
- ⚠️ Recording a view adds a small MongoDB write per product open for signed-in users;
  the cap + fire-and-forget keep it non-blocking and bounded.
- ↪️ The stored favorites/recently-viewed signals are a foundation a real
  recommendation step could consume later (out of scope here).
