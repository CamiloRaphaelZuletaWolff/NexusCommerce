# Data-flow diagram

How a request moves through the system, and how the two databases stay coherent
through the shared UUID. Two data lanes: public catalog reads are server-rendered
(RSC) directly; authenticated reads/writes go through the browser client with a
Bearer token + refresh cookie.

```mermaid
flowchart TD
    user([User / Browser])

    subgraph next["Next.js 15 (App Router)"]
        rsc["Server Components<br/>lib/server-api"]
        client["Client islands<br/>lib/api · TanStack Query"]
    end

    subgraph api["Spring Boot — modular monolith"]
        sec["Security filter<br/>JWT + RBAC"]
        svc["Feature services<br/>auth · catalog · cart · orders · payments · inventory · analytics"]
        integ["integration<br/>UUID sync"]
    end

    pg[("PostgreSQL<br/>users, stores, orders,<br/>payments, inventory, audit")]
    mongo[("MongoDB<br/>products, categories,<br/>brands, customer_profiles")]

    user -->|"page request"| rsc
    user -->|"interactions"| client
    rsc -->|"public reads (catalog, stores)"| sec
    client -->|"Bearer token + nexus_refresh cookie"| sec
    sec --> svc
    svc -->|"JPA / JDBC (ACID, parameterized)"| pg
    svc -->|"Spring Data Mongo (operator/array queries)"| mongo
    svc -. "publishes events" .-> integ
    integ -->|"upsert profile keyed by user UUID"| mongo
    pg <-. "shared UUID: storeId, productId, userId" .-> mongo
    svc -->|"JSON (DTOs)"| client
    rsc -->|"streamed HTML"| user

    classDef db fill:#e2f5ec,stroke:#11502f,color:#11502f;
    class pg,mongo db;
```

## The UUID bridge (no cross-engine foreign keys)

| Relational side (PostgreSQL) | Shared UUID | Document side (MongoDB) |
|---|---|---|
| `stores.id` | `storeId` | `products.storeId` |
| `cart_items.product_id`, `order_items.product_id`, `inventory.product_id` | `productId` | `products._id` |
| `users.id` | `userId` | `customer_profiles._id` |

The `integration` module keeps the document side coherent: on registration it
upserts a `customer_profiles` doc (ADR-0011); from Phase 6, the customer also writes
back favorites and recently-viewed through the same UUID (ADR-0016).
