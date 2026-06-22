# PostgreSQL ERD (3NF relational model)

The transactional store (schema `commerce`). Normalized to 3NF: the user↔role
many-to-many is resolved by the `user_roles` junction table; catalog data on
`cart_items` / `order_items` is an intentional point-in-time **snapshot**, not a
normalization violation (a sale must keep the price/name as they were at purchase).

`product_id` / `store_id` columns are **UUID references into MongoDB** (the catalog)
— there is no database-level foreign key across engines; the shared UUID is the
bridge (ADR-0003).

```mermaid
erDiagram
    roles          ||--o{ user_roles     : "granted in"
    users          ||--o{ user_roles     : "has"
    users          ||--o{ stores         : "owns"
    users          ||--o{ refresh_tokens : "issued"
    users          ||--o{ audit_logs     : "acted by"
    users          ||--o{ carts          : "owns"
    carts          ||--o{ cart_items     : "contains"
    users          ||--o{ orders         : "places"
    orders         ||--o{ order_items    : "contains"
    orders         ||--|| payments       : "paid by"

    roles {
        uuid    id    PK
        varchar name  UK
    }
    users {
        uuid    id            PK
        varchar email         UK
        varchar password_hash
        boolean enabled
    }
    user_roles {
        uuid user_id PK "FK -> users"
        uuid role_id PK "FK -> roles"
    }
    stores {
        uuid    id            PK
        uuid    owner_user_id FK
        varchar slug          UK
        varchar store_type
        varchar status
    }
    refresh_tokens {
        uuid    id         PK
        uuid    user_id    FK
        varchar token_hash UK "SHA-256 only"
        timestamptz expires_at
        timestamptz revoked_at
    }
    audit_logs {
        uuid    id            PK
        varchar event_type
        varchar outcome
        uuid    actor_user_id FK
        jsonb   details
    }
    carts {
        uuid    id      PK
        uuid    user_id FK
        varchar status  "one ACTIVE per user (partial unique idx)"
    }
    cart_items {
        uuid    id         PK
        uuid    cart_id    FK
        uuid    product_id "-> MongoDB product"
        numeric unit_price "snapshot"
        int     quantity
    }
    orders {
        uuid    id           PK
        varchar order_number UK
        uuid    user_id      FK
        varchar status
        numeric total
    }
    order_items {
        uuid    id         PK
        uuid    order_id   FK
        uuid    product_id "-> MongoDB product"
        numeric unit_price "snapshot"
        numeric line_total
    }
    payments {
        uuid    id              PK
        uuid    order_id        FK "UK · 1:1"
        varchar card_brand
        varchar card_last4
        text    card_number_enc "AES-256-GCM"
    }
    inventory {
        uuid    id                 PK
        uuid    product_id         UK "-> MongoDB product"
        uuid    store_id
        int     available_quantity
        int     reserved_quantity
    }
```

> `inventory` has no relational FK — it is keyed by the MongoDB product UUID and
> linked to orders only through that shared id. `sp_reserve_inventory` locks its row
> `FOR UPDATE` during the ACID checkout.
