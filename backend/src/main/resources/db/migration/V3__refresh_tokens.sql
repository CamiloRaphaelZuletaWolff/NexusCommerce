-- V3: Server-side refresh-token store.
-- Only the SHA-256 hash of the opaque token is persisted (never the raw value),
-- enabling rotation and revocation (logout / refresh).

CREATE TABLE refresh_tokens (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash VARCHAR(64)  NOT NULL UNIQUE,          -- SHA-256 hex digest
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked_at TIMESTAMPTZ,
    user_agent VARCHAR(255),
    ip_address VARCHAR(64),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
