"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Panel } from "@/components/dashboard/stat-card";
import { Badge } from "@/components/ui/badge";
import { Select } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { api } from "@/lib/api";
import { hueFor } from "@/lib/store-theme";
import { formatMoney } from "@/lib/utils";
import type { StoreResponse } from "@/types/api";

export default function AnalyticsPage() {
  const revenue = useQuery({ queryKey: ["an", "revenue"], queryFn: api.analytics.storeRevenue });
  const top = useQuery({ queryKey: ["an", "top", 10], queryFn: () => api.analytics.topProducts(10) });
  const stores = useQuery({ queryKey: ["stores", "all"], queryFn: () => api.stores.list({ size: 100 }) });

  const [storeId, setStoreId] = useState("");
  const summary = useQuery({
    queryKey: ["an", "summary", storeId],
    queryFn: () => api.analytics.storeSummary(storeId),
    enabled: !!storeId,
  });

  const storeMap = new Map<string, StoreResponse>(
    (stores.data?.content ?? []).map((s) => [s.id, s]),
  );
  const storeName = (id: string) => storeMap.get(id)?.name ?? `${id.slice(0, 8)}…`;
  const rows = [...(revenue.data ?? [])].sort((a, b) => Number(b.revenue) - Number(a.revenue));
  const maxRevenue = Math.max(1, ...rows.map((r) => Number(r.revenue)));

  return (
    <div className="space-y-8">
      <header>
        <p className="eyebrow">Analytics</p>
        <h1 className="mt-1 text-3xl font-extrabold">Store performance</h1>
        <p className="mt-1 text-ink-soft">
          Reporting from PostgreSQL views, a stored function, and a subquery.
        </p>
      </header>

      <Panel title="Revenue by store" action={<span className="eyebrow">v_store_revenue</span>}>
        {revenue.isLoading ? (
          <Skeleton className="h-48 w-full" />
        ) : rows.length === 0 ? (
          <p className="py-6 text-center text-sm text-ink-faint">No revenue recorded yet.</p>
        ) : (
          <div className="space-y-4">
            {rows.map((r) => {
              const s = storeMap.get(r.storeId);
              const hue = hueFor(s?.storeType);
              const pct = (Number(r.revenue) / maxRevenue) * 100;
              return (
                <div key={r.storeId}>
                  <div className="mb-1.5 flex items-center justify-between gap-3 text-sm">
                    <span className="flex min-w-0 items-center gap-2">
                      <span className="size-2.5 rounded-full" style={{ background: hue.accent }} />
                      <span className="truncate font-semibold">{storeName(r.storeId)}</span>
                    </span>
                    <span className="tabular shrink-0 text-ink-soft">
                      {r.ordersCount} orders · {r.unitsSold} units ·{" "}
                      <span className="font-bold text-ink">{formatMoney(r.revenue)}</span>
                    </span>
                  </div>
                  <div className="h-2 overflow-hidden rounded-full bg-paper-2">
                    <div className="h-full rounded-full" style={{ width: `${pct}%`, background: hue.accent }} />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </Panel>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Stored function */}
        <Panel title="Per-store summary" action={<span className="eyebrow">fn_store_sales_summary()</span>}>
          <Select value={storeId} onChange={(e) => setStoreId(e.target.value)}>
            <option value="">Choose a store…</option>
            {(stores.data?.content ?? []).map((s) => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </Select>
          <div className="mt-5">
            {!storeId ? (
              <p className="py-6 text-center text-sm text-ink-faint">
                Pick a store to run the stored function.
              </p>
            ) : summary.isLoading ? (
              <Skeleton className="h-24 w-full" />
            ) : summary.data ? (
              <div className="grid grid-cols-3 gap-3">
                <Metric label="Orders" value={String(summary.data.ordersCount)} />
                <Metric label="Units" value={String(summary.data.unitsSold)} />
                <Metric label="Revenue" value={formatMoney(summary.data.revenue)} />
              </div>
            ) : null}
          </div>
        </Panel>

        {/* Top products */}
        <Panel title="Top products" action={<span className="eyebrow">v_top_products</span>}>
          {top.isLoading ? (
            <Skeleton className="h-40 w-full" />
          ) : (top.data ?? []).length === 0 ? (
            <p className="py-6 text-center text-sm text-ink-faint">No sales yet.</p>
          ) : (
            <ol className="space-y-3">
              {(top.data ?? []).map((p, i) => (
                <li key={p.productId} className="flex items-center gap-3 text-sm">
                  <span className="tabular grid size-6 place-items-center rounded-md bg-paper-2 text-xs font-bold text-ink-soft">
                    {i + 1}
                  </span>
                  <span className="min-w-0 flex-1">
                    <span className="block truncate font-medium">{p.name}</span>
                    <span className="text-xs text-ink-faint">{storeName(p.storeId)}</span>
                  </span>
                  <Badge tone="neutral" className="tabular">{p.unitsSold} sold</Badge>
                  <span className="tabular font-bold">{formatMoney(p.revenue)}</span>
                </li>
              ))}
            </ol>
          )}
        </Panel>
      </div>
    </div>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-line bg-paper-2/40 p-3 text-center">
      <p className="eyebrow">{label}</p>
      <p className="tabular mt-1 text-lg font-extrabold">{value}</p>
    </div>
  );
}
