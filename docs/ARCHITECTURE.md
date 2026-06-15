# NexusCommerce — Architecture

> Status: living document. Phase 1 establishes the high-level design and the
> infrastructure foundation. Sections marked _(Phase N)_ are implemented in that
> phase and expanded here as they land.

## 1. Context

NexusCommerce is a multi-store (multi-tenant) e-commerce platform. Multiple
independent stores share one system; each owns its catalog, inventory,
promotions, orders, and analytics. The platform serves three audiences:

- **Customers** — browse stores, search/filter products, cart, checkout, order history.
- **Store managers** — manage their store's catalog, inventory, promotions, sales.
- **Admins** — oversee the whole platform.

## 2. High-level architecture

```
┌──────────────────────────────────────────────────────────────┐
│  Next.js 15 (App Router)                                       │
│  • Server Components for data-heavy pages                      │
│  • Client Components for forms, cart, animations               │
│  • Typed REST client (lib/) · TanStack Query · Zustand         │
└───────────────────────────┬──────────────────────────────────┘
                            │ HTTPS / JSON · JWT bearer
                            ▼
┌──────────────────────────────────────────────────────────────┐
│  Spring Boot 3 — Modular Monolith                              │
│                                                                │
│  auth · users · stores · catalog · cart · orders · payments    │
│  inventory · analytics · integration · security · shared       │
│                                                                │
│  • Stateless JWT auth + RBAC (security)                        │
│  • DTOs + Bean Validation at the edge                          │
│  • @Transactional service boundaries                           │
└───────────────┬────────────────────────────┬──────────────────┘
                │ JPA / JDBC                   │ Spring Data MongoDB
                ▼                              ▼
   ┌─────────────────────────┐    ┌──────────────────────────────┐
   │ PostgreSQL 16           │    │ MongoDB 7                     │
   │ transactional, 3NF,     │◄──►│ flexible product documents    │
   │ ACID, views, procedures │UUID│ category-specific attributes  │
   └─────────────────────────┘    └──────────────────────────────┘
```

## 3. Why a modular monolith

A modular monolith gives clean domain boundaries (each feature is a self-contained
package with its own api/domain/repository/service) without the operational
overhead of microservices (service discovery, network calls, distributed
transactions). It is the right altitude for a portfolio project that must be easy
to run locally yet demonstrate clean separation of concerns. Boundaries are
enforced by package structure today and could be split into services later
without rewriting the domain logic. See [ADR-0001](adr/0001-modular-monolith.md).

## 4. Hybrid persistence

The defining decision is using **two databases, each for what it is best at**:

### PostgreSQL — transactional / relational
Stores data that needs strong consistency, relationships, and ACID guarantees:
users, roles, stores, orders, order_items, payments, invoices, addresses,
audit_logs, cart metadata, and transactional inventory references. The relational
model is normalized to **3NF**. Checkout/payment run inside **ACID transactions**.
Reporting uses **views**, **subqueries**, and **stored procedures**. Sensitive
payment fields are **encrypted at the column level** (pgcrypto / app-side AES).

### MongoDB — flexible catalog
Stores the product catalog, where each category has different attributes (a laptop
has CPU/RAM; a sofa has material/dimensions). A rigid relational schema would
force sparse columns or EAV tables; MongoDB's documents model this naturally.
Holds products, categories, category-specific attributes, tags, variants, brand
metadata, and recommendation metadata. Queries demonstrate comparison operators
(`$gt`, `$lt`), logical operators (`$and`, `$or`), and array queries over tags,
variants, brands, and category labels.

See [ADR-0002](adr/0002-hybrid-postgres-mongo.md).

### The UUID bridge
The two databases are linked by a shared **UUID** (`storeId`, `productId`) rather
than DB-level foreign keys. A PostgreSQL `order_item` references a MongoDB product
by its UUID; the `integration` module keeps customer/store data in sync across
both stores by UUID. This decouples the relational and document models while
keeping them coherent. See [ADR-0003](adr/0003-uuid-cross-db-integration.md).

## 5. Security architecture

Security is a first-class concern, layered defense-in-depth:

- **Authentication:** stateless **JWT**. The **access token** (~15 min, HS384) is
  sent in the `Authorization` header; the **refresh token** is an opaque value in
  an **HttpOnly + Secure + SameSite cookie**, stored server-side only as a
  SHA-256 hash, with **rotation on refresh** and **revocation on logout**.
  Passwords hashed with **BCrypt**. See [ADR-0005](adr/0005-jwt-stateless-auth.md)
  and [ADR-0008](adr/0008-token-auth-strategy.md).
- **Authorization:** **RBAC** with roles (e.g. `CUSTOMER`, `STORE_MANAGER`,
  `ADMIN`) enforced via Spring Security + method security.
- **Input handling:** server-side **Bean Validation** on every DTO; output
  encoding to prevent XSS.
- **SQL injection:** all relational access via Spring Data JPA / parameterized
  queries — no string-concatenated SQL.
- **Sensitive data:** card-related fields are **tokenized/encrypted**, never
  stored raw. Each stored sensitive value is documented with why it exists and
  how it is protected.
- **Auditing:** `audit_logs` records sensitive actions (auth, payment, role
  changes).
- **Least privilege:** the Mongo application user is scoped to the catalog DB
  only; the Postgres role uses a dedicated `commerce` schema.

## 6. Backend module map

| Module        | Store      | Responsibility |
|---------------|------------|----------------|
| `auth`        | PostgreSQL | login, registration, JWT issue/refresh |
| `users`       | PostgreSQL | user + role aggregate |
| `stores`      | PostgreSQL | multi-store management |
| `catalog`     | MongoDB    | products, categories, dynamic attributes |
| `cart`        | PostgreSQL | cart metadata + items |
| `orders`      | PostgreSQL | order + order_items, ACID checkout |
| `payments`    | PostgreSQL | payment records, encrypted fields |
| `inventory`   | PostgreSQL | stock levels, transactional reservations |
| `analytics`   | PostgreSQL | reporting via views/subqueries |
| `integration` | both       | UUID-based cross-DB sync |
| `security`    | —          | Spring Security, JWT filter, RBAC, encryption |
| `shared`      | —          | DTO base, error handling, audit, config |

> **Status:** `auth`, `users`, `stores`, `security`, `audit`, `shared` (Phase 2)
> and `catalog` + `integration` (Phase 3) implemented and verified.

### Phase 2 API surface

| Method | Path | Auth |
|---|---|---|
| POST | `/api/auth/register` · `/login` | public (sets refresh cookie) |
| POST | `/api/auth/refresh` · `/logout` | refresh cookie |
| GET | `/api/auth/me` | access JWT |
| GET | `/api/stores` · `/api/stores/{slug}` | public |
| POST | `/api/stores` | `STORE_MANAGER`/`ADMIN` |
| PUT | `/api/stores/{id}` | owner or `ADMIN` |
| DELETE | `/api/stores/{id}` | `ADMIN` |
| GET | `/api/catalog/health` · `/actuator/health` · `/swagger-ui.html` | public |

### Phase 3 catalog API surface

| Method | Path | Auth |
|---|---|---|
| GET | `/api/catalog/products` (search filters) · `/api/catalog/products/{id}` | public |
| POST | `/api/catalog/products` | `STORE_MANAGER`/`ADMIN` (store owner) |
| PUT · DELETE | `/api/catalog/products/{id}` | store owner or `ADMIN` |
| GET | `/api/catalog/categories` · `/{slug}` · `/api/catalog/brands` · `/{slug}` | public |
| POST | `/api/catalog/categories` · `/api/catalog/brands` | `ADMIN` |
| GET | `/api/catalog/me/profile` | authenticated |

## 6b. MongoDB catalog model & queries _(Phase 3)_

**Documents** (database `nexuscatalog`):

```jsonc
// products
{
  "_id": "<uuid>",                 // product UUID
  "storeId": "<uuid>",             // -> PostgreSQL store (UUID bridge)
  "sku": "ELEC-UB14", "name": "Nexus UltraBook 14", "slug": "elec-ub14",
  "price": NumberDecimal("1299.00"), "currency": "USD",
  "categorySlug": "laptops",
  "categoryLabels": ["electronics", "laptops"],   // array query target
  "tags": ["laptop", "ultrabook", "portable"],     // array query target
  "brand": "Nexies",
  "variants": [ { "sku": "...-16-512", "attributes": {"ram":"16GB"}, "stock": 10 } ],
  "attributes": { "ramGb": 16, "cpu": "M3", "screenInch": 14 },  // category-specific
  "status": "ACTIVE"
}
// categories: { slug, name, attributes: [{key,label,type,required,unit}] }
// brands:     { slug, name, description }
// customer_profiles: { _id: <user UUID>, email, favoriteCategories, ... }  // synced from PG
```

**Search** (`ProductSearchRepositoryImpl`, via `MongoTemplate` + `Criteria`) builds
the required operators. Example — "electronics products priced 200–1000, tagged
`portable`, matching 'nexus'":

```js
db.products.find({ $and: [
  { status: "ACTIVE" },
  { price: { $gt: NumberDecimal("200"), $lt: NumberDecimal("1000") } },  // $gt / $lt
  { tags: { $in: ["portable"] } },                                        // array $in
  { categoryLabels: { $in: ["electronics"] } },                          // array $in
  { $or: [ { name: /nexus/i }, { description: /nexus/i } ] }             // $or
]})
```

Array queries target `tags`, `brand`, `categoryLabels`, and `variants.sku`.
Prices are stored as `Decimal128` so range comparisons are numeric (ADR-0010).

## 7. Frontend architecture _(Phase 5)_

App Router with route groups for storefront, auth, and dashboards. Server
Components fetch data; Client Components handle interactivity (cart drawer, forms,
animations). Server state via TanStack Query; local UI state via Zustand; a single
typed API client in `lib/`; Zod validation shared between forms and API
boundaries. Design: polished, responsive, accessible, with loading skeletons and
empty states throughout.

## 8. Local infrastructure _(Phase 1 ✅)_

`infra/docker-compose.yml` provisions PostgreSQL and MongoDB with healthchecks
and one-time bootstrap scripts, plus optional pgAdmin and mongo-express behind a
`tools` profile. Credentials come from a gitignored `infra/.env` (template in
`.env.example`). See [MANUAL_SETUP_GUIDE.md](../MANUAL_SETUP_GUIDE.md).

## 9. Decision records

| ADR | Decision |
|-----|----------|
| [0001](adr/0001-modular-monolith.md) | Modular monolith, package-by-feature |
| [0002](adr/0002-hybrid-postgres-mongo.md) | Hybrid PostgreSQL + MongoDB persistence |
| [0003](adr/0003-uuid-cross-db-integration.md) | UUID as cross-database key |
| [0004](adr/0004-maven-build-tool.md) | Maven as backend build tool |
| [0005](adr/0005-jwt-stateless-auth.md) | Stateless JWT authentication |
| [0006](adr/0006-flyway-and-3nf-schema.md) | Flyway-managed, 3NF schema |
| [0007](adr/0007-uuid-generation-strategy.md) | UUID generation strategy (app-side v4) |
| [0008](adr/0008-token-auth-strategy.md) | Access header + refresh HttpOnly cookie |
| [0009](adr/0009-audit-logging.md) | Audit logging from Phase 2 |
| [0010](adr/0010-mongodb-catalog-modeling.md) | MongoDB catalog modeling + Decimal128 |
| [0011](adr/0011-event-driven-customer-sync.md) | Event-driven cross-DB customer sync |
