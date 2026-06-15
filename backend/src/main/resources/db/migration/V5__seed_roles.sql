-- V5: Seed the three RBAC roles (stable reference data).
-- Demo users/stores are created by the dev-profile DataInitializer (real hashing).

INSERT INTO roles (name, description) VALUES
    ('CUSTOMER',      'Can browse stores, manage a cart and place orders'),
    ('STORE_MANAGER', 'Can create and manage owned stores and their catalog'),
    ('ADMIN',         'Full platform administration')
ON CONFLICT (name) DO NOTHING;
