"use client";

import { useQuery } from "@tanstack/react-query";
import { Store } from "lucide-react";
import { Panel } from "@/components/dashboard/stat-card";
import { CreateStoreDialog } from "@/components/dashboard/create-store-dialog";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { api } from "@/lib/api";
import { hueFor } from "@/lib/store-theme";
import { formatDate, titleCase } from "@/lib/utils";

export default function DashboardStoresPage() {
  const stores = useQuery({ queryKey: ["stores", "all"], queryFn: () => api.stores.list({ size: 100 }) });
  const rows = stores.data?.content ?? [];

  return (
    <div className="space-y-8">
      <header className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="eyebrow">Manage</p>
          <h1 className="mt-1 text-3xl font-extrabold">Stores</h1>
          <p className="mt-1 text-ink-soft">Create and review store-worlds on the platform.</p>
        </div>
        <CreateStoreDialog />
      </header>

      <Panel title={`All stores (${rows.length})`}>
        {stores.isLoading ? (
          <Skeleton className="h-48 w-full" />
        ) : rows.length === 0 ? (
          <div className="flex flex-col items-center py-12 text-center">
            <div className="grid size-12 place-items-center rounded-full bg-paper-2">
              <Store className="size-5 text-ink-faint" />
            </div>
            <p className="mt-3 text-sm text-ink-soft">No stores yet. Create your first one.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-line text-left text-xs uppercase tracking-wide text-ink-faint">
                  <th className="pb-2 font-semibold">Store</th>
                  <th className="pb-2 font-semibold">Type</th>
                  <th className="pb-2 font-semibold">Status</th>
                  <th className="pb-2 font-semibold">Slug</th>
                  <th className="pb-2 text-right font-semibold">Created</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {rows.map((s) => {
                  const hue = hueFor(s.storeType);
                  return (
                    <tr key={s.id}>
                      <td className="py-3 font-semibold">{s.name}</td>
                      <td className="py-3">
                        <span className="inline-flex items-center gap-1.5">
                          <span className="size-2 rounded-full" style={{ background: hue.accent }} />
                          {hue.label}
                        </span>
                      </td>
                      <td className="py-3">
                        <Badge tone={s.status === "ACTIVE" ? "success" : "neutral"}>{titleCase(s.status)}</Badge>
                      </td>
                      <td className="tabular py-3 text-ink-faint">{s.slug}</td>
                      <td className="py-3 text-right text-ink-faint">{formatDate(s.createdAt)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </Panel>
    </div>
  );
}
