# Manual Setup Guide — NexusCommerce

Step-by-step instructions to run NexusCommerce locally. Written so a first-time
reader can go from a fresh clone to running databases (and, in later phases, the
full stack).

---

## 1. Prerequisites

Install these before starting:

| Tool            | Version   | Check                       |
|-----------------|-----------|-----------------------------|
| Docker Desktop  | 24+       | `docker --version`          |
| Docker Compose  | v2+       | `docker compose version`    |
| Java (JDK)      | 21+       | `java -version`             |
| Node.js         | 20+       | `node --version`            |
| npm             | 10+       | `npm --version`             |
| Git             | any       | `git --version`             |

> Maven is **not** required — the backend ships a Maven Wrapper (`./mvnw`) that
> downloads the correct Maven version automatically (Phase 2).

## 2. Get the code

```bash
git clone <your-repo-url> NexusCommerce
cd NexusCommerce
```

## 3. Start the databases (Phase 1 — available now)

```bash
cd infra
cp .env.example .env        # Windows PowerShell: copy .env.example .env
docker compose up -d
```

This starts:

- **PostgreSQL** on `localhost:5432` (db `nexuscommerce`, user `nexus`)
- **MongoDB** on `localhost:27017` (db `nexuscatalog`, app user `nexus_app`)

On first start, bootstrap scripts run automatically:

- Postgres: enables `pgcrypto` + `uuid-ossp`, creates the `commerce` schema.
- Mongo: creates a least-privilege app user and base catalog collections.

### Verify they are healthy

```bash
docker compose ps
```

Both `nexus-postgres` and `nexus-mongo` should show `(healthy)`. To check
manually:

```bash
# PostgreSQL
docker exec nexus-postgres pg_isready -U nexus -d nexuscommerce

# MongoDB
docker exec nexus-mongo mongosh --quiet --eval "db.adminCommand('ping')"
```

### Optional: database admin UIs

```bash
docker compose --profile tools up -d
```

- **pgAdmin** → http://localhost:5050 (login `admin@nexus.local` / `admin`)
- **mongo-express** → http://localhost:8081

### Stopping

```bash
docker compose down       # stop, keep data
docker compose down -v    # stop, wipe data (re-runs bootstrap next time)
```

## 4. Run the backend (Phase 2 — available now)

**JDK 21 is the canonical build/runtime** (Spring Boot 3.5 targets ≤ JDK 21).
You don't need Maven or a JDK installed locally — everything runs in Docker.

### Easiest: full stack via Docker Compose (databases + backend)

```bash
cd infra
docker compose -f docker-compose.yml -f docker-compose.app.yml up -d --build
```

This builds the backend image (multi-stage, `backend/Dockerfile`) and starts it on
the shared network alongside PostgreSQL + MongoDB. The API comes up on
`http://localhost:8080` after ~30s (watch `docker logs -f nexus-backend`).

### Build & test only (no run)

```bash
# from the repo root — uses a cached .m2 volume so re-runs are fast
docker run --rm -v "$PWD/backend:/workspace" -v nexus-m2:/root/.m2 \
  -w /workspace maven:3.9-eclipse-temurin-21 mvn -B test
```

> Integration tests (`*IT`, Testcontainers) run with `mvn verify` on a
> Docker-enabled host; the `mvn test` gate runs unit + ArchUnit tests only.

### Key endpoints

- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health (db + mongo): `http://localhost:8080/actuator/health`
- Catalog Mongo probe: `http://localhost:8080/api/catalog/health`

### Dev demo accounts (dev profile only — never use in production)

| Email | Role | Password |
|-------|------|----------|
| `admin@nexus.local` | ADMIN | `Password123!` |
| `manager@nexus.local` | STORE_MANAGER | `Password123!` |
| `customer@nexus.local` | CUSTOMER | `Password123!` |

Example: log in and call an authenticated endpoint.

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"manager@nexus.local","password":"Password123!"}' \
  | grep -oE '"accessToken" *: *"[^"]*"' | sed -E 's/.*"accessToken" *: *"//;s/"$//')
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/auth/me
```

> The refresh token is set as an HttpOnly cookie (`nexus_refresh`); browsers send
> it automatically to `/api/auth/refresh`. From the SPA, call the API with
> `credentials: 'include'`.

### Stopping the backend

```bash
cd infra
docker compose -f docker-compose.yml -f docker-compose.app.yml stop backend  # stop app, keep DBs
```

## 5. Run the frontend (Phase 5 — coming later)

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

App will be available at `http://localhost:3000`.

## 6. Troubleshooting

| Symptom | Fix |
|---|---|
| Port already in use | Change the `*_PORT` value in `infra/.env` and re-run. |
| Mongo app user/login fails after changing `.env` | Bootstrap runs only on an empty volume. Run `docker compose down -v` then `up -d`. |
| Containers unhealthy | `docker compose logs postgres` / `... logs mongo`. |
| `cp` not found on Windows | Use `copy` in PowerShell/CMD, or copy the file in your editor. |

---

For the design rationale behind this setup, see
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and [docs/adr/](docs/adr/).
