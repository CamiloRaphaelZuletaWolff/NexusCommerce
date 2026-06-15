# NexusCommerce — Frontend

Next.js 15 (App Router) + React 19 storefront and dashboards.
**Scaffolded in Phase 5** — this folder currently documents the planned layout.

## Stack

- Next.js 15 · React 19 · TypeScript
- TailwindCSS · shadcn/ui · Framer Motion · Lucide React
- Zustand (local UI state) · TanStack Query (server state)
- React Hook Form · Zod (validation, shared schemas)

## Layout (planned)

```
frontend/
├── app/              # App Router: routes, layouts, RSC pages
│   ├── (storefront)/ # home, stores, catalog, product, cart, checkout
│   ├── (auth)/       # login, register
│   └── dashboard/    # store + admin dashboards
├── components/       # reusable presentational components (ui/, layout/)
├── features/         # feature modules (catalog, cart, orders, ...)
├── hooks/            # shared React hooks
├── lib/              # typed API client, utils, query client
├── stores/           # Zustand stores (cart drawer, UI state)
├── types/            # shared TypeScript types / API contracts
└── styles/           # Tailwind base, design tokens
```

## Conventions

- **Server Components by default**; `"use client"` only for interactivity
  (forms, cart drawer, animations, state).
- Server state via TanStack Query; ephemeral UI state via Zustand.
- All API calls go through the typed client in `lib/` — no scattered `fetch`.
- Validation schemas in Zod, shared between forms and API boundaries.
- Accessible, responsive, mobile-first; loading skeletons + empty states everywhere.

See [../docs/ARCHITECTURE.md](../docs/ARCHITECTURE.md) for the full design.
