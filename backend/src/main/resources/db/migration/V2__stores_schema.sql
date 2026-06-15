-- V2: Multi-store model. Each store is explicitly owned by a user (owner_user_id).

CREATE TABLE stores (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id UUID         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    name          VARCHAR(150) NOT NULL,
    slug          VARCHAR(160) NOT NULL UNIQUE,
    description   TEXT,
    store_type    VARCHAR(30)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_store_type
        CHECK (store_type IN ('ELECTRONICS', 'FASHION', 'FURNITURE', 'KITCHENWARE', 'HOME_DECOR')),
    CONSTRAINT chk_store_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);
CREATE INDEX idx_stores_owner  ON stores (owner_user_id);
CREATE INDEX idx_stores_type   ON stores (store_type);
CREATE INDEX idx_stores_status ON stores (status);
