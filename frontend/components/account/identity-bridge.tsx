"use client";

import { Database, Leaf } from "lucide-react";
import { NexusLogo } from "@/components/nexus-mark";
import type { CustomerProfileResponse, UserResponse } from "@/types/api";

/**
 * The signature of the account page: one identity, two databases. The same UUID is
 * the primary key of the PostgreSQL `users` row and the `_id` of the MongoDB
 * `customer_profiles` document — this makes the cross-database bridge (ADR-0003)
 * visible and concrete.
 */
export function IdentityBridge({
  user,
  profile,
}: {
  user: UserResponse;
  profile: CustomerProfileResponse;
}) {
  const PG = "#1e7fff"; // PostgreSQL blue
  const MG = "#2fa56b"; // MongoDB green

  return (
    <section className="rounded-2xl border border-line bg-surface p-6 sm:p-8">
      <p className="eyebrow">The UUID bridge</p>
      <h2 className="mt-1 text-xl font-extrabold">One identity, two databases</h2>
      <p className="mt-1 max-w-prose text-sm text-ink-soft">
        Your account lives in PostgreSQL; your catalog preferences live in MongoDB.
        Nothing joins them at the database level — they share a single UUID, kept in
        sync by the platform.
      </p>

      <div className="mt-6 grid items-stretch gap-4 md:grid-cols-[1fr_auto_1fr]">
        {/* PostgreSQL side */}
        <div className="rounded-xl border border-line bg-paper-2/40 p-5">
          <div className="flex items-center gap-2">
            <span className="grid size-8 place-items-center rounded-lg" style={{ background: `${PG}1a`, color: PG }}>
              <Database className="size-4" />
            </span>
            <div className="leading-tight">
              <p className="text-sm font-bold text-ink">PostgreSQL</p>
              <p className="tabular text-[11px] text-ink-faint">commerce.users</p>
            </div>
          </div>
          <dl className="mt-4 space-y-2 text-sm">
            <Row label="Email" value={user.email} />
            <Row label="Name" value={user.fullName || "—"} />
            <Row label="Roles" value={user.roles.map((r) => r.replace("_", " ")).join(", ")} />
          </dl>
        </div>

        {/* Connector */}
        <div className="flex items-center justify-center md:flex-col">
          <span className="h-px w-12 bg-line md:h-12 md:w-px" />
          <NexusLogo className="mx-3 size-7 text-nexus md:my-3 md:mx-0" />
          <span className="h-px w-12 bg-line md:h-12 md:w-px" />
        </div>

        {/* MongoDB side */}
        <div className="rounded-xl border border-line bg-paper-2/40 p-5">
          <div className="flex items-center gap-2">
            <span className="grid size-8 place-items-center rounded-lg" style={{ background: `${MG}1a`, color: MG }}>
              <Leaf className="size-4" />
            </span>
            <div className="leading-tight">
              <p className="text-sm font-bold text-ink">MongoDB</p>
              <p className="tabular text-[11px] text-ink-faint">nexuscatalog.customer_profiles</p>
            </div>
          </div>
          <dl className="mt-4 space-y-2 text-sm">
            <Row label="Favorites" value={`${profile.favoriteCategories.length + profile.favoriteBrands.length} saved`} />
            <Row label="Recently viewed" value={`${profile.recentlyViewedProductIds.length} products`} />
            <Row label="Synced" value="on register · live writes" />
          </dl>
        </div>
      </div>

      {/* The shared key — the hero */}
      <div className="mt-6 rounded-xl border border-nexus/20 bg-nexus-tint/60 px-5 py-4 text-center">
        <p className="eyebrow text-nexus">Shared key · users.id == customer_profiles._id</p>
        <p className="tabular mt-1 break-all text-sm font-semibold text-nexus-deep sm:text-base">{profile.id}</p>
      </div>
    </section>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-baseline justify-between gap-3">
      <dt className="text-ink-faint">{label}</dt>
      <dd className="truncate text-right font-medium text-ink">{value}</dd>
    </div>
  );
}
