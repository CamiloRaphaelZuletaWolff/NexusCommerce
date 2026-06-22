# Screenshots — capture guide

Capture these against the running stack and save the PNGs **in this folder** with the
exact file names below, so they can be embedded in the README / report.

## Prepare

```bash
# 1) databases + backend
cd infra && docker compose -f docker-compose.yml -f docker-compose.app.yml up -d --build
# 2) frontend
cd ../frontend && npm run dev      # http://localhost:3000
```

Sign in with a demo account (`Password123!`): `customer@nexus.local`,
`manager@nexus.local`, or `admin@nexus.local`. Use a ~1440px-wide window; capture the
viewport (not the whole desktop). On Windows: `Win+Shift+S`.

## Shot list

| # | File name | Page | What it shows |
|---|---|---|---|
| 1 | `01-home.png` | `/` | the nexus hero + five store-worlds |
| 2 | `02-catalog-filters.png` | `/catalog?minPrice=200&maxPrice=1000&q=nexus` | server-side filters → Mongo `$gt`/`$lt`/`$or` |
| 3 | `03-product.png` | `/products/{id}` | product detail with dynamic attributes |
| 4 | `04-cart.png` | `/cart` | cart with line items + summary |
| 5 | `05-checkout.png` | `/checkout` | checkout form (use the "Use test card" button) |
| 6 | `06-order-confirmation.png` | `/orders/{id}?success=1` | order confirmation; payment shows brand + last4 only |
| 7 | `07-account-bridge.png` | `/account` | **the UUID-bridge visualization** (PostgreSQL id == Mongo `_id`) |
| 8 | `08-dashboard.png` | `/dashboard` | manager/admin KPIs + recent orders |
| 9 | `09-analytics.png` | `/dashboard/analytics` | revenue chart over the SQL views/function |
| 10 | `10-swagger.png` | `localhost:8080/swagger-ui.html` | the live API surface |
| 11 | `11-pgadmin-erd.png` | pgAdmin → `nexuscommerce` → ERD For Database | the relational model in the DB itself |
| 12 | `12-mongo-express-product.png` | `localhost:8081` → `nexuscatalog.products` | a product document with dynamic `attributes` |

> Optional but high-value for a databases course: shots 11–12 prove the schema in the
> actual engines. For #7, having `customer_profiles` open in mongo-express beside it
> drives home the shared UUID.

Once captured, reference them from `README.md` or the project report. Keep the names
stable so links don't break.
