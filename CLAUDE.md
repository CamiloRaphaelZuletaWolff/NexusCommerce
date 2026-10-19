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

Tracked here so nothing is missed (✅ = done, ◑ = partial):

- [x] 3NF relational model — *Phase 2* (roles/users/user_roles/stores, junction table)
- [x] ACID transactions for checkout/payment — *Phase 4* (single-tx checkout, ADR-0012)
- [x] Encryption of sensitive data (column-level, payments) — *Phase 4* (AES-256-GCM, ADR-0013)
- [x] RBAC authorization — *Phase 2* (roles + `@PreAuthorize` + ownership checks)
- [x] SQL injection protection (parameterized / JPA) — *Phase 2* (Spring Data only)
- [x] Stored procedures — *Phase 4* (`sp_reserve_inventory`, `fn_store_sales_summary`)
- [x] Views — *Phase 4* (`v_store_revenue`, `v_top_products`, `v_order_summary`)
- [x] Subqueries — *Phase 4* (views + above-average revenue query, ADR-0014)
- [x] Mongo operators: `$gt`, `$lt`, `$and`, `$or` — *Phase 3* (product search via MongoTemplate)
- [x] Mongo array queries: tags, variants, brands, category labels — *Phase 3* (`$in`)
- [x] UUID cross-DB integration — *Phase 3* (products→stores by UUID; user→Mongo customer profile sync, ADR-0011)
- [x] Audit logging (added requirement) — *Phase 2* (register/login±/refresh/logout/store-create)
- [x] Stateless JWT + refresh-cookie rotation (added requirement) — *Phase 2* (ADR-0008)

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
- **`next-best-practices`** + **`frontend-design`** — loaded and applied in
  Phase 5: RSC-by-default with a thin client boundary, async `params`/
  `searchParams`, the typed-client/no-scattered-fetch rule, and a distinctive,
  non-templated visual identity ("the nexus" convergence motif + per-store-world
  hues). See ADR-0015.
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
- **A7 — Frontend stack specifics (Phase 5):** TailwindCSS **v4** (what
  `create-next-app` ships today) configured via CSS `@theme`. We hand-build the
  UI primitives on **Radix** + Tailwind rather than pulling shadcn/ui's default
  components, to keep a distinctive, non-templated identity (the stack doc still
  lists shadcn/ui; this is the documented substitution, same Radix+Tailwind
  foundation). See ADR-0015.
- **A8 — No product images in the API:** the catalog API exposes no image URLs,
  so the frontend renders deterministic generative SVG art per product (seeded by
  product id, themed to the store-world hue) instead of placeholder photos.
- **A9 — Client auth storage:** the access token is held in memory only (never
  `localStorage`); the browser persists the session through the HttpOnly
  `nexus_refresh` cookie, and the client performs one silent refresh + retry on a
  401. See ADR-0015.

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
- **Phase 2 — Backend Core:** ✅ complete. Spring Boot 3.5.6 modular monolith:
  Spring Security (JWT access + HttpOnly refresh cookie w/ rotation, BCrypt,
  RBAC), users/roles + stores on PostgreSQL via Flyway (3NF), MongoDB
  connectivity + validated read/write probe, audit logging, ArchUnit boundary
  tests. Verified: 24 unit/ArchUnit tests pass in JDK-21 Docker; multi-stage
  image builds; app runs on `nexus-net` with all health checks UP; full
  register→login→refresh→create-store→audit flow exercised live with RBAC.
  ADRs 0006–0009. New: backend build/runtime is JDK 21 in Docker (ADR-0004/R1).
- **Phase 3 — Catalog + Multi-Store:** ✅ complete. MongoDB catalog (products,
  categories with dynamic attribute schema, brands) + `customer_profiles`.
  Product search via `MongoTemplate`/`Criteria` demonstrating `$gt`/`$lt`/`$and`/
  `$or` and array `$in` (tags/brands/categoryLabels/variants). Prices stored as
  Decimal128 (ADR-0010). Cross-DB: products reference stores by UUID with
  ownership validation; user→Mongo customer-profile sync via domain event
  (ADR-0011). RBAC-gated category/brand/product writes; PRODUCT_CREATED audit.
  Verified live: all operator/array queries, product CRUD (manager 201 /
  customer 403), customer sync, audit. 30 unit/ArchUnit tests pass (added catalog
  search + product-service tests) plus a Testcontainers IT covering real-Mongo
  product search. ADRs 0010–0011.
- **Phase 4 — Orders + Checkout + Security:** ✅ complete. Cart, ACID checkout
  (single transaction: reserve stock via stored procedure → order + items →
  encrypted payment → close cart; rolls back atomically), inventory with
  `sp_reserve_inventory` (`FOR UPDATE`), AES-256-GCM column encryption of card
  data (only brand/last4 in clear, CVV never stored), order history, and an
  analytics layer over SQL views + a stored function + a subquery (RBAC-gated).
  Audit: ORDER_PLACED / PAYMENT_CAPTURED / CHECKOUT_FAILED (durable REQUIRES_NEW —
  fixed self-invocation bug). Migrations V6–V10. ADRs 0012–0014. 36 unit/ArchUnit
  tests pass; verified live: checkout decrements stock, card encrypted at rest,
  insufficient stock → 400 + rollback, views/function/subquery return correct data.
- **Phase 5 — Frontend:** ✅ complete. Next.js 15 (App Router) + React 19 +
  TypeScript + Tailwind v4. Storefront (nexus hero, store directory, store pages,
  catalog with server-side filters/search/pagination, product detail), auth
  (login/register with access-token-in-memory + silent refresh via the HttpOnly
  cookie), cart drawer + cart page, ACID checkout flow, order confirmation +
  history, and a role-gated manager/admin dashboard (analytics over the SQL
  views/function/subquery, plus store & product creation). RSC-by-default with a
  thin client boundary; one typed API client; TanStack Query (server state) +
  Zustand (UI state) + RHF/Zod (forms). Distinctive design system: "the nexus"
  convergence signature + per-store-world hue theming via runtime CSS vars.
  Verified: `next build` (16 routes) + `tsc` + eslint clean; smoke-tested live
  against the running backend — SSR renders real catalog/store data, CORS allows
  the browser origin with credentials, and login→`/me` works. ADR-0015.
- **Phase 6 — Integration polish:** ✅ complete. Made the PostgreSQL↔MongoDB
  customer profile **bidirectional**: new `PATCH /api/catalog/me/profile`
  (favorite categories/brands) + `POST /api/catalog/me/recently-viewed/{productId}`
  (de-duped, capped at 12), both `isAuthenticated`, writing to `customer_profiles`
  keyed by the PostgreSQL user UUID. Profile access is now **get-or-create**, so
  users predating the register-time sync (ADR-0011) still get a profile. New
  customer **`/account`** page: a UUID-bridge visualization (same id as PG
  `users.id` and Mongo `_id`), editable favorites (write-back), and recently-viewed
  populated live from the product page (fire-and-forget). **UX states:**
  `global-error.tsx`, segment `error.tsx` (storefront + dashboard), route
  `loading.tsx` skeletons for the dynamic routes, and reusable `<EmptyState>` /
  `<ErrorState>`. No new cross-DB FK (ADR-0003 preserved). ADR-0016. Verified:
  backend `mvn test` green in JDK-21 Docker; `next build` (17 routes) + `tsc` +
  eslint clean; smoke-tested live (favorites + recently-viewed persist to Mongo).
- **Phase 7 — Testing + docs:** ✅ complete. Backend: added
  `CustomerProfileServiceTest` (get-or-create, favorites PATCH semantics,
  recently-viewed de-dup/cap/404) + `CartServiceTest` → **49** unit/ArchUnit/
  Testcontainers tests green in JDK-21 Docker. Frontend: introduced a test suite —
  **Vitest + Testing Library + jsdom** (`npm test`) covering `lib/utils`,
  `lib/store-theme`, `lib/api` (401→refresh→retry + query building), and the
  `FavoritesEditor` (toggle/dirty/save); 16 tests green, `tsc`/eslint/`next build`
  still clean. Docs: Mermaid diagrams in `docs/diagrams/` (ERD, Mongo schema,
  **data-flow**, sequences) + index; exported OpenAPI spec `docs/api/openapi.json`
  (28 paths) + `docs/api/README.md` reference; screenshot capture guide in
  `docs/diagrams/screenshots/`. Frontend testing runs on local Node (the
  JDK-21-in-Docker rule remains backend-only).
- **Phase 8:** not started. See README roadmap.

### Dev demo credentials (dev profile only, never in prod)
`admin@nexus.local`, `manager@nexus.local`, `customer@nexus.local` — all
password `Password123!`. Seeded by `DevDataInitializer`. The admin password comes
from `app.demo.admin-password` (`DEMO_ADMIN_PASSWORD`): defaulted only in the `dev`
profile; in the public `demo` profile it is a private secret (unset = no admin).

### Build / run the backend (JDK 21 canonical)
```bash
# Build + test (no local Maven needed):
docker run --rm -v "$PWD/backend:/workspace" -v nexus-m2:/root/.m2 \
  -w /workspace maven:3.9-eclipse-temurin-21 mvn -B test
# Run full stack (DBs + backend) from infra/:
docker compose -f docker-compose.yml -f docker-compose.app.yml up -d --build
```

### Build / run the frontend (Phase 5)
```bash
cd frontend
cp .env.example .env.local   # PowerShell: copy .env.example .env.local
npm install
npm run dev                  # http://localhost:3000 (backend must be on :8080)
npm run build && npm start   # production build + serve
```
Tooling note: frontend runs on the local Node (npm 10.9); the JDK-21-in-Docker
rule is backend-only. Data-driven listing pages (`/`, `/stores`, `/catalog`) are
server-rendered dynamically so they always reflect current backend data.

## 12. Service ports (local)

| Service        | Port  |
|----------------|-------|
| Backend API    | 8080  |
| Frontend       | 3000  |
| PostgreSQL     | 5432  |
| MongoDB        | 27017 |
| pgAdmin        | 5050  |
| mongo-express  | 8081  |

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
