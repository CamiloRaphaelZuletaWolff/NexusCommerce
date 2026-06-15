# NexusCommerce — Frontend

Next.js 15 (App Router) + React 19 storefront and dashboards. **Built in Phase 5.**

## Stack

- Next.js 15 · React 19 · TypeScript · TailwindCSS v4
- TanStack Query (server state) · Zustand (ephemeral UI state)
- React Hook Form + Zod (forms & validation) · Radix UI primitives
- Framer Motion · Lucide React
- Fonts: Bricolage Grotesque (display) · Plus Jakarta Sans (body) · JetBrains Mono (numerics)

## Run

```bash
cp .env.example .env.local   # PowerShell: copy .env.example .env.local
npm install
npm run dev                  # http://localhost:3000
```

The backend must be running on `http://localhost:8080` (see the repo root
`MANUAL_SETUP_GUIDE.md`). The backend's CORS already allows `http://localhost:3000`
with credentials.

```bash
npm run build   # production build
npm start       # serve the production build
npm run lint    # eslint
```

## Layout

```
app/
├── (storefront)/   # navbar + footer shell: home, stores, catalog, product, cart, checkout, orders
├── (auth)/         # login, register (split-screen brand panel)
└── dashboard/      # manager/admin: overview, analytics, products, stores (role-gated)
components/
├── ui/             # design-system primitives (button, input, dialog, badge, …)
├── layout/         # navbar, footer, user menu
├── catalog/        # product card, filters, pagination, add-to-cart
├── cart/ checkout/ orders/ stores/ home/ dashboard/   # feature components
├── nexus-mark.tsx  # the signature convergence motif
└── product-art.tsx # deterministic generated art (the catalog API has no images)
lib/                # api.ts (typed client + silent refresh), server-api.ts (RSC reads),
                    # store-theme.ts (per-store-world hues), token-store.ts, utils.ts
stores/             # zustand: ui (cart drawer), toast
hooks/              # use-cart and friends
types/api.ts        # contracts mirroring the Spring Boot DTOs
```

## Conventions

- **Server Components by default**; `"use client"` only for interactivity.
- Public catalog/store reads happen in Server Components (`lib/server-api.ts`,
  cached). Authenticated data (cart, orders, analytics) is fetched on the client
  via `lib/api.ts`, because the access token lives in browser memory.
- **Auth:** short-lived access token in memory + the HttpOnly `nexus_refresh`
  cookie; a 401 triggers one silent refresh + retry.
- All API calls go through the typed client — no scattered `fetch`.
- Every store-world carries its own hue, applied at runtime via `--accent*` CSS
  variables (`lib/store-theme.ts`).

See [../docs/ARCHITECTURE.md](../docs/ARCHITECTURE.md) for the full design.
