-- V6: Shopping cart (metadata in PostgreSQL; line items snapshot catalog data).

CREATE TABLE carts (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_cart_status CHECK (status IN ('ACTIVE', 'CHECKED_OUT', 'ABANDONED'))
);
-- At most one ACTIVE cart per user.
CREATE UNIQUE INDEX uq_carts_user_active ON carts (user_id) WHERE status = 'ACTIVE';

CREATE TABLE cart_items (
    id         UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id    UUID          NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    product_id UUID          NOT NULL,          -- MongoDB product UUID
    store_id   UUID          NOT NULL,
    sku        VARCHAR(80)   NOT NULL,
    name       VARCHAR(200)  NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    quantity   INT           NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_cart_product UNIQUE (cart_id, product_id)
);
CREATE INDEX idx_cart_items_cart ON cart_items (cart_id);
