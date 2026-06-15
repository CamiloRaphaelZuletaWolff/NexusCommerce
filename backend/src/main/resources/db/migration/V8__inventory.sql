-- V8: Transactional inventory. Keyed by the MongoDB product UUID (cross-DB link).

CREATE TABLE inventory (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id           UUID        NOT NULL,
    product_id         UUID        NOT NULL UNIQUE,   -- MongoDB product UUID
    sku                VARCHAR(80) NOT NULL,
    available_quantity INT         NOT NULL DEFAULT 0 CHECK (available_quantity >= 0),
    reserved_quantity  INT         NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_inventory_store ON inventory (store_id);
