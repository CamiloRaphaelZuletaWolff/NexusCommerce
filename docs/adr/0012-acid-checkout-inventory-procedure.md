# ADR-0012: ACID checkout with stored-procedure inventory reservation

- **Status:** Accepted
- **Date:** 2026-06-15
- **Phase:** 4

## Context

Checkout must be correct under concurrency: stock can't be oversold, and an order
must never exist without its items/payment (or vice versa). This is the platform's
strongest consistency requirement.

## Decision

The entire checkout runs in a **single Spring `@Transactional` method**
(`CheckoutService.checkout`):

1. Reserve stock for each line by calling the PostgreSQL **stored procedure**
   `sp_reserve_inventory(product_id, qty)`, which locks the row (`SELECT … FOR
   UPDATE`) and `RAISE`s if stock is missing/insufficient.
2. Create the `order` + `order_items` (price snapshotted).
3. Record the `payment` (card fields encrypted — ADR-0013).
4. Mark the cart `CHECKED_OUT`.

Any failure throws, so the transaction **rolls back atomically** — no partial
order, no leaked stock. A stored-procedure `RAISE` is translated to an HTTP 400 and
a durable `CHECKOUT_FAILED` audit entry.

## Consequences

- ✅ True ACID semantics for the money path; `FOR UPDATE` serializes concurrent
  reservations of the same product.
- ✅ Demonstrates a stored procedure used in the transactional path (academic req).
- ✅ Verified live: insufficient stock → 400, inventory unchanged, zero orders created.
- ⚠️ Row locking can contend under high concurrency for a hot product — acceptable
  at this scale; a reservation queue would be the next step.
- ⚠️ Audit durability requires the audit write to be `REQUIRES_NEW` so it survives
  the rolled-back checkout (see ADR-0009; the public `AuditService` methods are the
  transactional boundary).
