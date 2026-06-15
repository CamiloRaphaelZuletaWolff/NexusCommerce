# ADR-0015: Frontend architecture, data strategy & client auth

- **Status:** Accepted
- **Date:** 2026-06-15
- **Phase:** 5

## Context

Phase 5 adds the storefront and dashboards. The stack is fixed by the project
(Next.js 15 App Router, React 19, TypeScript, Tailwind, TanStack Query, Zustand,
React Hook Form, Zod). Decisions were needed on: how to fetch from the Spring
Boot API, where the access token lives in the browser, how to keep a distinctive
visual identity, and how to render a catalog that exposes no image URLs.

## Decision

- **RSC by default, thin client boundary.** Pages are Server Components.
  `"use client"` is used only for interactivity (forms, cart drawer, dashboard
  widgets, animations). `params`/`searchParams` are awaited (Next 15 async APIs).
- **Two-lane data access, one typed contract.**
  - **Public reads** (catalog, stores) happen in Server Components via
    `lib/server-api.ts` (`fetch` with `next: { revalidate }`), so listings are
    fast and SEO-friendly. The three data-driven listing routes (`/`, `/stores`,
    `/catalog`) are `force-dynamic` so newly created stores/products appear
    immediately rather than waiting on ISR.
  - **Authenticated reads/writes** (cart, orders, checkout, analytics) happen on
    the client via `lib/api.ts` + TanStack Query, because the access token lives
    in browser memory.
  - Both lanes share `types/api.ts`, hand-mirrored from the backend DTOs. No
    scattered `fetch` — every call goes through the typed client.
- **Client auth = access-token-in-memory + refresh cookie.** The short-lived
  access token is kept in a module-level variable (never `localStorage`), sent as
  `Authorization: Bearer`. The session survives reloads via the HttpOnly
  `nexus_refresh` cookie (`credentials: "include"`). On a 401 the client performs
  exactly one silent refresh + retry (coalesced so concurrent calls share it);
  the `AuthProvider` bootstraps the session by attempting a refresh on mount.
  This mirrors the backend strategy from ADR-0008.
- **State split.** TanStack Query owns server state (cart, orders, analytics);
  Zustand owns ephemeral UI state (cart drawer, mobile nav, toasts). The cart's
  source of truth stays server-side.
- **Distinctive design system (per `/frontend-design`).** A deliberately
  non-template identity: the **"nexus"** convergence motif (five store-hue nodes
  wired to one center) as the signature, a Bricolage Grotesque / Plus Jakarta
  Sans / JetBrains Mono type trio, and **per-store-world hue theming** applied at
  runtime through `--accent*` CSS variables. Tailwind v4 tokens live in `@theme`.
- **UI primitives on Radix + Tailwind**, hand-built rather than shadcn/ui
  defaults, for a non-templated look on the same accessible foundation (see A7).
- **Generated product art.** The catalog API has no images, so each product gets
  deterministic SVG art seeded by its id and themed to the store hue (A8).

## Consequences

- ✅ Fast, SEO-friendly public pages; authenticated data stays correct and fresh.
- ✅ No token in `localStorage` (reduced XSS blast radius); refresh is invisible.
- ✅ One typed contract keeps the client honest against the backend DTOs;
  type-check + build catch drift.
- ✅ A recognizable identity that encodes the product (many stores, one system).
- ⚠️ `force-dynamic` listing pages trade some static-caching for always-fresh
  data — fine at this scale; could move to tag-based revalidation if needed.
- ⚠️ `types/api.ts` is maintained by hand; a generated OpenAPI client would remove
  that drift risk in a later phase.
