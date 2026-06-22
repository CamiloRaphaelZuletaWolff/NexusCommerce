# API reference

NexusCommerce exposes one REST API (Spring Boot, base path `/api`). Auth is a
stateless JWT access token in the `Authorization: Bearer` header; the refresh token
is an HttpOnly cookie. Errors share one envelope (`ApiError`: `timestamp`, `status`,
`error`, `message`, `path`, optional `fieldErrors`).

- **Interactive docs (Swagger UI):** <http://localhost:8080/swagger-ui.html>
- **Machine-readable spec:** <http://localhost:8080/v3/api-docs> → committed at
  [`openapi.json`](openapi.json) (OpenAPI 3.1, 28 paths).

Auth legend: **public** · **auth** (any signed-in user) · **manager** (`STORE_MANAGER`/`ADMIN`) · **admin** · **owner** (resource owner or `ADMIN`).

## Auth
| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/register` | public (sets refresh cookie) |
| POST | `/api/auth/login` | public (sets refresh cookie) |
| POST | `/api/auth/refresh` | refresh cookie |
| POST | `/api/auth/logout` | refresh cookie |
| GET | `/api/auth/me` | auth |

## Catalog (MongoDB)
| Method | Path | Access |
|---|---|---|
| GET | `/api/catalog/products` (search: `q`, `minPrice`, `maxPrice`, `tags`, `brands`, `categories`, paging) | public |
| GET | `/api/catalog/products/{id}` | public |
| POST | `/api/catalog/products` | manager (store owner) |
| PUT · DELETE | `/api/catalog/products/{id}` | owner |
| GET | `/api/catalog/categories` · `/categories/{slug}` | public |
| POST | `/api/catalog/categories` | admin |
| GET | `/api/catalog/brands` · `/brands/{slug}` | public |
| POST | `/api/catalog/brands` | admin |
| GET | `/api/catalog/health` | public |

## Customer profile (UUID bridge — Phase 6)
| Method | Path | Access |
|---|---|---|
| GET | `/api/catalog/me/profile` | auth (get-or-create) |
| PATCH | `/api/catalog/me/profile` (favorite categories/brands) | auth |
| POST | `/api/catalog/me/recently-viewed/{productId}` | auth |

## Stores
| Method | Path | Access |
|---|---|---|
| GET | `/api/stores` · `/api/stores/{slug}` | public |
| POST | `/api/stores` | manager |
| PUT | `/api/stores/{id}` | owner |
| DELETE | `/api/stores/{id}` | admin |

## Cart · Checkout · Orders
| Method | Path | Access |
|---|---|---|
| GET · DELETE | `/api/cart` | auth |
| POST | `/api/cart/items` | auth |
| PUT · DELETE | `/api/cart/items/{itemId}` | auth |
| POST | `/api/checkout` (single ACID transaction) | auth |
| GET | `/api/orders` · `/api/orders/{id}` | auth (own orders) |

## Analytics (SQL views / function / subquery)
| Method | Path | Access |
|---|---|---|
| GET | `/api/analytics/store-revenue` (view) | manager |
| GET | `/api/analytics/store-revenue/above-average` (subquery) | manager |
| GET | `/api/analytics/top-products` (view) | manager |
| GET | `/api/analytics/stores/{storeId}/summary` (stored function) | manager |
| GET | `/api/analytics/orders-summary` (view) | manager |

## Re-exporting the spec

With the backend running:

```bash
curl -s http://localhost:8080/v3/api-docs | python -m json.tool > docs/api/openapi.json
```
