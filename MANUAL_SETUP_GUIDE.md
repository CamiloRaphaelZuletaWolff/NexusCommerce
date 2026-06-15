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

## 4. Run the backend (Phase 2 — coming next)

```bash
cd backend
cp .env.example .env       # adjust if you changed infra credentials
./mvnw spring-boot:run     # Windows: .\mvnw.cmd spring-boot:run
```

API will be available at `http://localhost:8080`, OpenAPI docs at
`http://localhost:8080/swagger-ui.html`.

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
