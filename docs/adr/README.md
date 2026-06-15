# Architecture Decision Records

Short records of significant architectural decisions, why they were made, and
their consequences. New ADRs are appended with the next sequential number and
never deleted (superseded ones are marked as such).

| # | Title | Status |
|---|-------|--------|
| [0001](0001-modular-monolith.md) | Modular monolith, package-by-feature | Accepted |
| [0002](0002-hybrid-postgres-mongo.md) | Hybrid PostgreSQL + MongoDB persistence | Accepted |
| [0003](0003-uuid-cross-db-integration.md) | UUID as the cross-database key | Accepted |
| [0004](0004-maven-build-tool.md) | Maven as the backend build tool | Accepted |
| [0005](0005-jwt-stateless-auth.md) | Stateless JWT authentication with RBAC | Accepted |
| [0006](0006-flyway-and-3nf-schema.md) | Flyway-managed, 3NF relational schema | Accepted |
| [0007](0007-uuid-generation-strategy.md) | UUID generation strategy (app-side v4) | Accepted |
| [0008](0008-token-auth-strategy.md) | Access token in header, refresh token in HttpOnly cookie | Accepted |
| [0009](0009-audit-logging.md) | Audit logging from Phase 2 | Accepted |
| [0010](0010-mongodb-catalog-modeling.md) | MongoDB catalog modeling + Decimal128 price storage | Accepted |
| [0011](0011-event-driven-customer-sync.md) | Event-driven cross-DB customer sync (UUID) | Accepted |
| [0012](0012-acid-checkout-inventory-procedure.md) | ACID checkout + stored-procedure inventory reservation | Accepted |
| [0013](0013-column-encryption-payments.md) | Column-level encryption of payment data (AES-256-GCM) | Accepted |
| [0014](0014-sql-reporting-views-functions.md) | SQL reporting: views, function, subqueries | Accepted |
