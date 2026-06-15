# ADR-0014: SQL reporting layer — views, function, subqueries

- **Status:** Accepted
- **Date:** 2026-06-15
- **Phase:** 4

## Context

The platform needs sales reporting, and the project requires demonstrating SQL
views, subqueries, and stored functions in a meaningful way (not contrived).

## Decision

- **Views** (`V10`):
  - `v_store_revenue` — revenue/units/orders per store over paid orders
    (filtered via a subquery on `orders.status`).
  - `v_top_products` — units/revenue aggregated per product.
  - `v_order_summary` — per-order totals with a **correlated scalar subquery** for
    item count.
- **Stored function** `fn_store_sales_summary(store_id)` — sales rollup for one
  store, also using a subquery; invoked from the analytics layer.
- **Subquery** endpoint — "stores with revenue above the platform average":
  `… WHERE revenue > (SELECT AVG(revenue) FROM v_store_revenue)`.
- Views are mapped to read-only (`@Immutable`) JPA entities; the function and the
  above-average query are run via native SQL. All reporting endpoints are
  restricted to `ADMIN`/`STORE_MANAGER`.

## Consequences

- ✅ Demonstrates views, subqueries, and a stored function on real order data
  (verified live against the seeded catalog/orders).
- ✅ Read models are decoupled from write entities; reporting can't mutate state.
- ⚠️ Views recompute on read (fine at this scale); materialized views would be the
  optimization if data volume grew.
