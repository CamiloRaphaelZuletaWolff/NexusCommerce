# ADR-0009: Audit logging from Phase 2

- **Status:** Accepted
- **Date:** 2026-06-14
- **Phase:** 2

## Context

Security-sensitive actions must be traceable (an explicit requirement). Auditing
needs to be reliable even when the surrounding business transaction fails (e.g. a
failed login has no successful transaction to hang off of), and flexible enough to
capture per-event context.

## Decision

- A single `audit_logs` table (PostgreSQL) records: `event_type`, `outcome`
  (SUCCESS/FAILURE), optional `actor_user_id`, client `ip_address` + `user_agent`,
  and a flexible `details` **JSONB** payload.
- `AuditService.record(...)` runs in a **`REQUIRES_NEW`** transaction so the audit
  entry is committed independently of the caller's transaction (durable even on
  rollback), and auditing failures never break the primary flow.
- Phase 2 captures: `USER_REGISTERED`, `LOGIN_SUCCESS`, `LOGIN_FAILURE`,
  `TOKEN_REFRESHED`, `LOGOUT`, `STORE_CREATED`. The event set grows in later phases
  (payments, role changes, …).
- Client context is extracted via `RequestMetadata` (X-Forwarded-For aware).

## Consequences

- ✅ Reliable, queryable audit trail; failures (e.g. bad-password logins) are
  recorded with the attempted email and a null actor.
- ✅ JSONB keeps per-event context flexible without schema churn.
- ⚠️ `REQUIRES_NEW` opens a second connection per audited action — negligible at
  this scale; could move to async if volume ever demands it.
