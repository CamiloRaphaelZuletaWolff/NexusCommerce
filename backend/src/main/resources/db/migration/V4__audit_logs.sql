-- V4: Audit trail for sensitive actions (auth + store lifecycle).
-- `details` is JSONB for flexible, queryable context.

CREATE TABLE audit_logs (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type    VARCHAR(40) NOT NULL,
    outcome       VARCHAR(10) NOT NULL,
    actor_user_id UUID        REFERENCES users (id) ON DELETE SET NULL,
    ip_address    VARCHAR(64),
    user_agent    VARCHAR(255),
    details       JSONB,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_audit_outcome CHECK (outcome IN ('SUCCESS', 'FAILURE'))
);
CREATE INDEX idx_audit_event_type ON audit_logs (event_type);
CREATE INDEX idx_audit_actor      ON audit_logs (actor_user_id);
CREATE INDEX idx_audit_created    ON audit_logs (created_at);
