-- NexusCommerce — PostgreSQL bootstrap
-- Runs ONCE, automatically, on first container start (empty data volume),
-- executed as ${POSTGRES_USER} against ${POSTGRES_DB}.
--
-- Scope of this script: enable extensions + create the application schema.
-- All tables, views, stored procedures and seed data are owned by Flyway
-- migrations (added in Phase 2 and Phase 4) so the schema stays versioned.

-- pgcrypto: gen_random_uuid() for GUID primary keys, and pgp_sym_encrypt/
-- pgp_sym_decrypt for the column-level encryption requirement (Phase 4).
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- uuid-ossp: explicit UUID generators (uuid_generate_v4) used by some views.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Dedicated schema keeps application objects out of `public`.
CREATE SCHEMA IF NOT EXISTS commerce AUTHORIZATION CURRENT_USER;
COMMENT ON SCHEMA commerce IS 'NexusCommerce transactional / relational domain';

-- Make the app schema the default for the application user.
ALTER ROLE CURRENT_USER SET search_path TO commerce, public;
