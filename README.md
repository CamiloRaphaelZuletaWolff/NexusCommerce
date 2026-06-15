<div align="center">

# 🛍️ NexusCommerce

### A multi-store e-commerce platform with a hybrid PostgreSQL + MongoDB architecture

**Spring Boot 3 · Next.js 15 · PostgreSQL · MongoDB · JWT + RBAC · Docker**

</div>

---

NexusCommerce is a production-style, multi-tenant commerce platform where many
independent stores — Electronics, Fashion, Furniture, Kitchenware, Home Decor —
operate inside a single system. Each store owns its catalog, inventory,
promotions, orders, and analytics.

It is intentionally built as a **serious engineering artifact**, not a CRUD demo:
it pairs the right database to each job and bridges them cleanly, treats security
as a first-class concern, and ships with real documentation and architecture
decision records.

## ✨ Highlights

- **Hybrid persistence done right** — PostgreSQL for ACID transactional data
  (users, orders, payments) and MongoDB for flexible, category-specific product
  catalogs, joined by a shared **UUID bridge**.
- **Multi-store / multi-tenant** — one platform, many stores, each with isolated
  catalog, inventory, and sales analytics.
- **Security-first** — BCrypt password hashing, stateless JWT auth, role-based
  authorization (RBAC), server-side validation, column-level encryption for
  sensitive payment fields, and audit logging.
- **Real relational engineering** — 3NF schema, ACID checkout transactions,
  stored procedures, views, and subqueries for reporting.
- **Polished frontend** — modern, responsive, accessible storefront and
  dashboards built with the Next.js App Router and React Server Components.
- **One-command local infra** — PostgreSQL + MongoDB (+ optional admin UIs) via
  Docker Compose.

## 🧱 Architecture at a glance

```
Next.js 15 (App Router, RSC)
        │  typed REST client
        ▼
Spring Boot 3 — modular monolith
  auth · users · stores · catalog · cart · orders
  payments · inventory · analytics · integration · security
        │                         │
        ▼                         ▼
   PostgreSQL  ◄────UUID────►   MongoDB
 (transactional)            (flexible catalog)
```

Full design: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) ·
Decision records: [docs/adr/](docs/adr/)

## 🚀 Quick start (databases)

```bash
cd infra
cp .env.example .env
docker compose up -d            # PostgreSQL + MongoDB
docker compose --profile tools up -d   # + pgAdmin & mongo-express
```

Full setup (backend + frontend) is in **[MANUAL_SETUP_GUIDE.md](MANUAL_SETUP_GUIDE.md)**.

## 🛠️ Tech stack

| Layer     | Technologies |
|-----------|--------------|
| Frontend  | Next.js 15, React 19, TypeScript, TailwindCSS, shadcn/ui, Framer Motion, Zustand, TanStack Query, React Hook Form, Zod |
| Backend   | Java 21, Spring Boot 3, Spring Security, Spring Data JPA, Spring Data MongoDB, Flyway, MapStruct, OpenAPI |
| Databases | PostgreSQL 16, MongoDB 7 |
| Tooling   | Docker Compose, JUnit 5, Testcontainers, GitHub Actions |

## 📦 Repository layout

```
backend/   Spring Boot modular monolith        (Phase 2)
frontend/  Next.js storefront + dashboards      (Phase 5)
infra/     Docker Compose + DB bootstrap        (Phase 1 ✅)
docs/      Architecture, ADRs, diagrams         (ongoing)
```

## 🗺️ Build roadmap

The project is built incrementally; each phase is completed, verified, and
documented before the next begins.

- [x] **Phase 1 — Foundation:** repo structure, Docker infra, docs scaffolding
- [ ] **Phase 2 — Backend core:** Spring Boot, security, auth/RBAC, persistence
- [ ] **Phase 3 — Catalog + multi-store:** MongoDB catalog, dynamic attributes
- [ ] **Phase 4 — Orders + checkout + security:** ACID checkout, encryption, procedures/views
- [ ] **Phase 5 — Frontend:** storefront, catalog, cart, checkout, dashboards
- [ ] **Phase 6 — Integration polish:** end-to-end wiring, UX states
- [ ] **Phase 7 — Testing + docs:** tests, diagrams, API docs, screenshots
- [ ] **Phase 8 — Final hardening:** security & quality review

## 📄 License

For educational and portfolio use.
