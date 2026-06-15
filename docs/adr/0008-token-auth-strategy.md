# ADR-0008: Access token in header, refresh token in HttpOnly cookie

- **Status:** Accepted (supersedes the header-only refresh sketch in ADR-0005)
- **Date:** 2026-06-14
- **Phase:** 2

## Context

ADR-0005 chose stateless JWT auth. For a browser-based Next.js client, *where*
each token lives materially affects the security posture. Storing tokens in
`localStorage` exposes them to XSS; long-lived bearer tokens are hard to revoke.

## Decision

A hybrid token model:

- **Access token** — short-lived (~15 min) JWT (HS384, signed from `JWT_SECRET`),
  sent in the `Authorization: Bearer` header; held in client memory.
- **Refresh token** — opaque 256-bit random value delivered in an **HttpOnly +
  Secure + SameSite cookie** (so JavaScript/XSS cannot read it). Only its
  **SHA-256 hash** is persisted (`refresh_tokens`).
  - `/refresh` validates the cookie token against the stored hash, checks
    expiry/revocation, then **rotates** it (revoke old, issue new).
  - `/logout` revokes the token and clears the cookie.
  - Cookie attributes are profile-driven: dev `SameSite=Lax, Secure=false`
    (works on `localhost`); prod `SameSite=None, Secure=true` over HTTPS.
- CORS allows the frontend origin with `allow-credentials: true` (required to
  send the cookie); origins are explicit (no wildcard).

## Consequences

- ✅ XSS cannot exfiltrate the refresh token (HttpOnly); access tokens are
  short-lived and memory-only.
- ✅ Server-side hashing + rotation enables real revocation (logout, reuse
  detection) — a production-grade posture.
- ✅ No server session store; horizontally scalable.
- ⚠️ The frontend must call the API with `credentials: 'include'` and use a fixed
  allowed origin. Documented for Phase 5.
- ⚠️ Cross-site prod cookies require HTTPS (`SameSite=None; Secure`) — expected.
