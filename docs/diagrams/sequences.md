# Sequence diagrams

## 1. Login + silent refresh on 401

Access token lives in browser memory; the refresh token is an HttpOnly cookie. A
401 triggers exactly one coalesced refresh + retry (ADR-0008 / ADR-0015).

```mermaid
sequenceDiagram
    actor U as Browser (client)
    participant API as Spring Boot
    participant PG as PostgreSQL

    U->>API: POST /api/auth/login {email, password}
    API->>PG: load user, verify BCrypt hash
    API-->>U: 200 {accessToken} + Set-Cookie nexus_refresh (HttpOnly)
    Note over U: access token kept in memory only

    U->>API: GET /api/orders  (Bearer access)
    API-->>U: 401 (expired)
    U->>API: POST /api/auth/refresh  (nexus_refresh cookie)
    API->>PG: validate token hash, rotate
    API-->>U: 200 {new accessToken} + rotated cookie
    U->>API: GET /api/orders  (retry, new Bearer)
    API-->>U: 200 [orders]
```

## 2. ACID checkout (single transaction)

Reserve stock → create order → encrypt payment → close cart, all-or-nothing
(ADR-0012 / ADR-0013).

```mermaid
sequenceDiagram
    actor U as Customer
    participant CO as CheckoutService
    participant INV as inventory (sp_reserve_inventory)
    participant PG as PostgreSQL
    participant AUD as audit_logs

    U->>CO: POST /api/checkout {shipping, card}
    activate CO
    Note over CO,PG: @Transactional — one unit of work
    loop each cart item
        CO->>INV: CALL sp_reserve_inventory(productId, qty)
        INV->>PG: SELECT ... FOR UPDATE; decrement / reserve
        alt insufficient stock
            INV-->>CO: RAISE (check_violation)
            CO->>AUD: CHECKOUT_FAILED (REQUIRES_NEW)
            CO-->>U: 400 — rolled back (no order, no charge)
        end
    end
    CO->>PG: INSERT order + order_items (price snapshot)
    CO->>PG: INSERT payment (card_number_enc = AES-256-GCM; CVV never stored)
    CO->>PG: UPDATE cart SET status = CHECKED_OUT
    CO->>AUD: ORDER_PLACED + PAYMENT_CAPTURED
    CO-->>U: 201 OrderResponse
    deactivate CO
```

## 3. Cross-database customer sync (the UUID bridge)

Registration seeds the Mongo profile after commit (ADR-0011); Phase 6 adds live
write-back keyed by the same UUID (ADR-0016).

```mermaid
sequenceDiagram
    actor U as User
    participant US as UserService
    participant PG as PostgreSQL (users)
    participant L as CustomerProfileSyncListener
    participant M as MongoDB (customer_profiles)

    U->>US: POST /api/auth/register
    US->>PG: INSERT user (commit)
    US-->>L: UserRegisteredEvent (AFTER_COMMIT)
    L->>M: upsert profile  _id = users.id
    Note over PG,M: same UUID on both sides

    U->>US: open product / edit favorites (Phase 6)
    US->>M: POST recently-viewed / PATCH profile (get-or-create, keyed by UUID)
```
