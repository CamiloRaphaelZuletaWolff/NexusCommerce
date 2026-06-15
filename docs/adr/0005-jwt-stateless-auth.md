# ADR-0005: Stateless JWT authentication with RBAC

- **Status:** Accepted
- **Date:** 2026-06-14
- **Phase:** 1 (decision) · implemented Phase 2

## Context

The platform needs secure authentication and role-based authorization across a
Spring Boot API consumed by a Next.js frontend. Options include server-side
sessions (stateful) or token-based stateless auth.

## Decision

Use **stateless JWT** authentication:

- **Access token** — short-lived (≈15 min), sent as a `Bearer` token.
- **Refresh token** — longer-lived (≈7 days), used to mint new access tokens.
- Passwords hashed with **BCrypt**.
- **RBAC** roles (`CUSTOMER`, `STORE_MANAGER`, `ADMIN`) enforced via Spring
  Security and method-level security.
- Secret and TTLs are externalized via environment variables (`JWT_SECRET`,
  `JWT_ACCESS_TTL_MINUTES`, `JWT_REFRESH_TTL_DAYS`).

## Consequences

- ✅ No server-side session store → fits the modular-monolith/stateless API model
  and scales horizontally.
- ✅ Clean fit with a separate frontend (token in `Authorization` header).
- ✅ RBAC maps directly to the three platform audiences.
- ⚠️ Token revocation is non-trivial with pure JWT → mitigated by short access
  TTL + refresh-token rotation/invalidation (revisited if needed in Phase 4/8).
- ⚠️ Secret management matters → secrets come from env, never committed
  (see CLAUDE.md A5).
