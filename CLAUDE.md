# CLAUDE.md — NexusCommerce working agreement

This file is the single source of truth for how NexusCommerce is built. It records
the conventions, decisions, and assumptions that aren't obvious from the code.
Keep it updated at the end of every phase.

> **Note for any AI/dev assistant:** read this file and `docs/ARCHITECTURE.md`
> before making changes. Build phase by phase; do not advance until the current
> phase compiles, is verified, and is documented.

---

## 1. What this project is

A multi-store (multi-tenant) e-commerce platform. Multiple stores (Electronics,
Fashion, Furniture, Kitchenware, Home Decor) operate in one system, each with its
own catalog, inventory, promotions, orders, and analytics.

It is a portfolio-grade engineering artifact: hybrid persistence, security-first,
clean architecture, fully documented.

## 2. Tech stack (authoritative)

- **Backend:** Java 21, Spring Boot 3.x, Maven (+ wrapper), Spring Web/Security/
  Data JPA/Data MongoDB, Flyway, Bean Validation, Lombok, MapStruct,
  springdoc-openapi, JUnit 5, Testcontainers.
- **Frontend:** Next.js 15 (App Router), React 19, TypeScript, TailwindCSS,
  shadcn/ui, Framer Motion, Zustand, TanStack Query, React Hook Form, Zod,
  Lucide React.
- **Databases:** PostgreSQL 16 (transactional), MongoDB 7 (catalog).
- **Infra/CI:** Docker Compose, GitHub Actions.

## 3. Architecture rules

- **Backend:** modular monolith, **package by feature** (`auth`, `users`,
  `stores`, `catalog`, `cart`, `orders`, `payments`, `inventory`, `analytics`,
  `integration`, `security`, `shared`). Inside each feature: `api` / `domain` /
  `repository` / `service` / `mapper`.
- Constructor injection only; fields `private final`. Thin controllers, logic in
  services. DTOs over HTTP (never raw entities). `@Valid` on every request DTO.
  Global `@ControllerAdvice` error handling. Schema changes via Flyway only.
- **Frontend:** Server Components by default; `"use client"` only for
  interactivity. TanStack Query for server state, Zustand for local UI state.
  One typed API client in `lib/`. Zod for validation.
- **Cross-database integration:** PostgreSQL and MongoDB share **UUID** keys
  (`storeId`, `productId`). No DB-level FKs across stores; the `integration`
  module keeps them in sync. See ADR-0003.

## 4. Database responsibilities

| PostgreSQL (relational, ACID) | MongoDB (flexible documents) |
|---|---|
| users, roles, stores | product catalog, categories |
| orders, order_items | category-specific attributes |
| payments, invoices, addresses | tags, variants, brand metadata |
| audit_logs, cart metadata | recommendation catalog metadata |
| transactional inventory refs | |

## 5. Academic / engineering requirements checklist

Tracked here so nothing is missed:

- [ ] 3NF relational model
- [ ] ACID transactions for checkout/payment
- [ ] Encryption of sensitive data (column-level, payments)
- [ ] RBAC authorization
- [ ] SQL injection protection (parameterized / JPA)
- [ ] Stored procedures
- [ ] Views
- [ ] Subqueries
- [ ] Mongo operators: `$gt`, `$lt`, `$and`, `$or`
- [ ] Mongo array queries: tags, variants, brands, category labels
- [ ] UUID-based customer-data sync between PostgreSQL and MongoDB

## 6. Security baseline

BCrypt password hashing · stateless JWT (short-lived access + refresh) · RBAC ·
server-side Bean Validation · parameterized/ORM querying · column-level
encryption for card-related fields (tokenized/encrypted, never raw) · audit logs
for sensitive actions · least-privilege DB users (separate Mongo app user, scoped
Postgres role).

## 7. Skill usage (per project requirements)

The project asked for `/frontend-design`, `/security-best-practices`,
`/backend-dev-guidelines`, `/next-best-practices`, `/java-springboot`.

- **`java-springboot`** — loaded; applied to backend structure & conventions
  (Phase 2+).
- **`next-best-practices`** + **`frontend-design`** — to be loaded in Phase 5/6
  (frontend implementation).
- **`security-best-practices`** — to be loaded in Phase 4 (security-heavy work).
  Caveat: this skill targets python/js/ts/go, so for Java it provides general
  guidance only; Spring-specific security follows `java-springboot` + OWASP.
- **`backend-dev-guidelines`** — **does not match our stack.** The available
  skill documents Langfuse's Next.js/tRPC/BullMQ monorepo, not Spring Boot.
  Substitution: we follow `java-springboot` for backend guidance instead.
  (Assumption A3 below.)

## 8. Assumptions (decided, not guessed silently)

- **A1 — Build tool:** Maven (spec left it open). Maven Wrapper committed so no
  local Maven install is needed. See ADR-0004.
- **A2 — Java level:** target Java **21** (LTS, spec requirement). Local JDK is
  **25**, which compiles to 21 without issue.
- **A3 — `backend-dev-guidelines` mismatch:** the available skill is
  Langfuse/tRPC-specific; we use `java-springboot` for the Spring Boot backend
  and note the substitution here (per "document the assumption" rule).
- **A4 — Skill sequencing:** skills are loaded in the phase where they apply
  (see §7) to keep each phase focused.
- **A5 — Local secrets:** live in gitignored `.env` files; only `.env.example`
  templates are committed.
- **A6 — Admin UIs (pgAdmin, mongo-express)** run behind a Docker Compose
  `tools` profile so the default `up` stays lightweight.

## 9. Environment notes (this machine)

- OS: Windows 10. Shell: PowerShell (primary) + Git Bash.
- Installed: JDK 25 (Temurin), Node + npm 10.9, Docker 28 + Compose v2.39.
- Not installed: Maven, Gradle (handled via Maven Wrapper in Phase 2).

## 10. Common commands

```bash
# Databases (from infra/)
docker compose up -d                    # Postgres + Mongo
docker compose --profile tools up -d    # + admin UIs
docker compose ps                       # status / health
docker compose down                     # stop (keep data)
docker compose down -v                  # stop + wipe data

# Backend (from backend/, Phase 2+)
./mvnw spring-boot:run
./mvnw test

# Frontend (from frontend/, Phase 5+)
npm run dev
npm run build
```

## 11. Phase status

- **Phase 1 — Foundation:** ✅ complete (repo structure, Docker infra, env
  templates, docs, ADRs). Verified: `docker compose config` valid; databases
  start healthy.
- **Phases 2–8:** not started. See README roadmap.

## 12. Service ports (local)

| Service        | Port  |
|----------------|-------|
| Backend API    | 8080  |
| Frontend       | 3000  |
| PostgreSQL     | 5432  |
| MongoDB        | 27017 |
| pgAdmin        | 5050  |
| mongo-express  | 8081  |
