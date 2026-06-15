"use client";

import { useQuery } from "@tanstack/react-query";
import { Package } from "lucide-react";
import { Panel } from "@/components/dashboard/stat-card";
import { CreateProductDialog } from "@/components/dashboard/create-product-dialog";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/components/auth-provider";
import { api } from "@/lib/api";
import { formatMoney, titleCase } from "@/lib/utils";
import type { ProductStatus, StoreResponse } from "@/types/api";

const STATUS_TONE: Record<ProductStatus, "success" | "warning" | "neutral"> = {
  ACTIVE: "success",
  DRAFT: "warning",
  ARCHIVED: "neutral",
};

export default function DashboardProductsPage() {
  const { user, hasRole } = useAuth();
  const products = useQuery({
    queryKey: ["products", "dashboard"],
    queryFn: () => api.catalog.products({ size: 100, sort: "createdAt,desc" }),
  });
  const stores = useQuery({ queryKey: ["stores", "all"], queryFn: () => api.stores.list({ size: 100 }) });
  const categories = useQuery({ queryKey: ["categories"], queryFn: api.catalog.categories });

  const allStores = stores.data?.content ?? [];
  const storeMap = new Map<string, StoreResponse>(allStores.map((s) => [s.id, s]));
  // Managers can only add products to stores they own; admins to any.
  const ownStores = hasRole("ADMIN")
    ? allStores
    : allStores.filter((s) => s.ownerUserId === user?.id);

  const rows = products.data?.content ?? [];

  return (
    <div className="space-y-8">
      <header className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="eyebrow">Manage</p>
          <h1 className="mt-1 text-3xl font-extrabold">Products</h1>
          <p className="mt-1 text-ink-soft">Catalog stored in MongoDB, linked to stores by UUID.</p>
        </div>
        <CreateProductDialog stores={ownStores} categories={categories.data ?? []} />
      </header>

      <Panel title={`Catalog (${products.data?.totalElements ?? 0})`}>
        {products.isLoading ? (
          <Skeleton className="h-64 w-full" />
        ) : rows.length === 0 ? (
          <div className="flex flex-col items-center py-12 text-center">
            <div className="grid size-12 place-items-center rounded-full bg-paper-2">
              <Package className="size-5 text-ink-faint" />
            </div>
            <p className="mt-3 text-sm text-ink-soft">No products yet. Create your first one.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-line text-left text-xs uppercase tracking-wide text-ink-faint">
                  <th className="pb-2 font-semibold">Product</th>
                  <th className="pb-2 font-semibold">Store</th>
                  <th className="pb-2 font-semibold">SKU</th>
                  <th className="pb-2 font-semibold">Category</th>
                  <th className="pb-2 font-semibold">Status</th>
                  <th className="pb-2 text-right font-semibold">Price</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {rows.map((p) => (
                  <tr key={p.id}>
                    <td className="max-w-[16rem] truncate py-3 font-semibold">{p.name}</td>
                    <td className="py-3 text-ink-soft">{storeMap.get(p.storeId)?.name ?? "—"}</td>
                    <td className="tabular py-3 text-ink-faint">{p.sku}</td>
                    <td className="py-3 text-ink-soft">{p.categorySlug ?? "—"}</td>
                    <td className="py-3">
                      <Badge tone={STATUS_TONE[p.status] ?? "neutral"}>{titleCase(p.status)}</Badge>
                    </td>
                    <td className="tabular py-3 text-right font-semibold">{formatMoney(p.price, p.currency)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Panel>
    </div>
  );
}
