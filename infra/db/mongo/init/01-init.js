/*
 * NexusCommerce — MongoDB bootstrap
 * Runs ONCE, automatically, on first container start (empty data volume),
 * executed by mongosh authenticated as the root user.
 *
 * Responsibilities:
 *   1. Create a least-privilege application user scoped to the catalog DB.
 *   2. Create the base catalog collections so their namespaces exist.
 *
 * Schema validators, indexes and seed documents are added in Phase 3.
 *
 * Note: fallback defaults below intentionally mirror infra/.env.example so a
 * default setup works even if mongosh does not surface process.env. If you
 * change credentials in .env, run with a clean volume (`docker compose down -v`).
 */

const dbName = (typeof process !== "undefined" && process.env.MONGO_INITDB_DATABASE) || "nexuscatalog";
const appUser = (typeof process !== "undefined" && process.env.MONGO_APP_USER) || "nexus_app";
const appPwd = (typeof process !== "undefined" && process.env.MONGO_APP_PASSWORD) || "mongo_app_local_pw";

const catalog = db.getSiblingDB(dbName);

// Least-privilege: readWrite on the catalog DB only — no admin/root grants.
catalog.createUser({
  user: appUser,
  pwd: appPwd,
  roles: [{ role: "readWrite", db: dbName }],
});

// Create base collections (namespaces) used by the catalog domain.
["products", "categories", "brands"].forEach((name) => {
  if (!catalog.getCollectionNames().includes(name)) {
    catalog.createCollection(name);
  }
});

print(`[nexus-init] catalog DB '${dbName}' ready; app user '${appUser}' created; base collections ensured.`);
