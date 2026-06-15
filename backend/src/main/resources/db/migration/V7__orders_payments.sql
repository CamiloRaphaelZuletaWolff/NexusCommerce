-- V7: Orders, order items and payments. Shipping address is snapshotted on the
-- order for historical accuracy. Sensitive card fields are stored encrypted.

CREATE TABLE orders (
    id                   UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number         VARCHAR(30)   NOT NULL UNIQUE,
    user_id              UUID          NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    status               VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    subtotal             NUMERIC(12,2) NOT NULL,
    tax                  NUMERIC(12,2) NOT NULL,
    total                NUMERIC(12,2) NOT NULL,
    currency             VARCHAR(3)    NOT NULL DEFAULT 'USD',
    shipping_name        VARCHAR(150),
    shipping_line1       VARCHAR(200),
    shipping_line2       VARCHAR(200),
    shipping_city        VARCHAR(120),
    shipping_region      VARCHAR(120),
    shipping_postal_code VARCHAR(20),
    shipping_country     VARCHAR(2),
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_order_status CHECK (status IN ('PENDING', 'PAID', 'CANCELLED', 'FULFILLED'))
);
CREATE INDEX idx_orders_user ON orders (user_id);

CREATE TABLE order_items (
    id         UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id   UUID          NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id UUID          NOT NULL,
    store_id   UUID          NOT NULL,
    sku        VARCHAR(80)   NOT NULL,
    name       VARCHAR(200)  NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    quantity   INT           NOT NULL CHECK (quantity > 0),
    line_total NUMERIC(12,2) NOT NULL
);
CREATE INDEX idx_order_items_order ON order_items (order_id);
CREATE INDEX idx_order_items_store ON order_items (store_id);

CREATE TABLE payments (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID          NOT NULL UNIQUE REFERENCES orders (id) ON DELETE CASCADE,
    status          VARCHAR(20)   NOT NULL,
    method          VARCHAR(20)   NOT NULL,
    amount          NUMERIC(12,2) NOT NULL,
    currency        VARCHAR(3)    NOT NULL DEFAULT 'USD',
    card_brand      VARCHAR(20),
    card_last4      VARCHAR(4),
    -- Encrypted at rest (AES-256-GCM, app-side). Raw PAN/CVV are never stored.
    card_holder_enc TEXT,
    card_number_enc TEXT,
    card_exp_enc    TEXT,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_payment_status CHECK (status IN ('AUTHORIZED', 'CAPTURED', 'FAILED', 'REFUNDED')),
    CONSTRAINT chk_payment_method CHECK (method IN ('CARD'))
);
