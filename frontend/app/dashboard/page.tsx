"use client";

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { ArrowRight, DollarSign, Package, ShoppingCart, Store, TrendingUp } from "lucide-react";
import { StatCard, Panel } from "@/components/dashboard/stat-card";
import { OrderStatusBadge } from "@/components/orders/status-badge";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/components/auth-provider";
import { api } from "@/lib/api";
import { hueFor } from "@/lib/store-theme";
import { formatCount, formatDate, formatMoney } from "@/lib/utils";
import type { StoreResponse } from "@/types/api";

export default function DashboardOverview() {
  const revenue = useQuery({ queryKey: ["an", "revenue"], queryFn: api.analytics.storeRevenue });
  const aboveAvg = useQuery({ queryKey: ["an", "above"], queryFn: api.analytics.aboveAverage });
  const topProducts = useQuery({ queryKey: ["an", "top", 5], queryFn: () => api.analytics.topProducts(5) });
  const { hasRole } = useAuth();
  const isAdmin = hasRole("ADMIN");
  // The cross-customer order feed is admin-only on the API; managers see only their stores' figures.
  const recent = useQuery({
    queryKey: ["an", "orders", 8],
    queryFn: () => api.analytics.ordersSummary(8),
    enabled: isAdmin,
  });
  const stores = useQuery({ queryKey: ["stores", "all"], queryFn: () => api.stores.list({ size: 100 }) });

  const storeMap = new Map<string, StoreResponse>(
    (stores.data?.content ?? []).map((s) => [s.id, s]),
  );
  const storeName = (id: string) => storeMap.get(id)?.name ?? `${id.slice(0, 8)}…`;

  const totals = (revenue.data ?? []).reduce(
    (acc, r) => ({
      revenue: acc.revenue + Number(r.revenue),
      orders: acc.orders + Number(r.ordersCount),
      units: acc.units + Number(r.unitsSold),
    }),
    { revenue: 0, orders: 0, units: 0 },
  );

  return (
    <div className="space-y-8">
      <header>
        <p className="eyebrow">Dashboard</p>
        <h1 className="mt-1 text-3xl font-extrabold">Overview</h1>
        <p className="mt-1 text-ink-soft">Sales across every store-world, from SQL views and a stored function.</p>
      </header>

      {/* KPIs */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {revenue.isLoading ? (
          [0, 1, 2, 3].map((i) => <Skeleton key={i} className="h-28 rounded-2xl" />)
        ) : (
          <>
            <StatCard label="Total revenue" value={formatMoney(totals.revenue)} icon={DollarSign} sub={isAdmin ? "Paid orders, all stores" : "Paid orders, your stores"} />
            <StatCard label="Orders" value={formatCount(totals.orders)} icon={ShoppingCart} accent="#1e7fff" />
            <StatCard label="Units sold" value={formatCount(totals.units)} icon={Package} accent="#2fa56b" />
            <StatCard label="Active stores" value={String(stores.data?.content.length ?? 0)} icon={Store} accent="#e14b86" />
          </>
        )}
      </div>

      <div className="grid gap-6 lg:grid-cols-[1.4fr_1fr]">
        {/* Recent orders (admin only) */}
        {isAdmin ? (
          <Panel
            title="Recent orders"
            action={
              <span className="eyebrow">v_order_summary</span>
            }
          >
            {recent.isLoading ? (
              <Skeleton className="h-40 w-full" />
            ) : (recent.data ?? []).length === 0 ? (
              <Empty>No orders yet.</Empty>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-line text-left text-xs uppercase tracking-wide text-ink-faint">
                      <th className="pb-2 font-semibold">Order</th>
                      <th className="pb-2 font-semibold">Status</th>
                      <th className="pb-2 text-right font-semibold">Items</th>
                      <th className="pb-2 text-right font-semibold">Total</th>
                      <th className="pb-2 text-right font-semibold">Placed</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line">
                    {(recent.data ?? []).map((o) => (
                      <tr key={o.orderId}>
                        <td className="tabular py-2.5 font-semibold">{o.orderNumber}</td>
                        <td className="py-2.5"><OrderStatusBadge status={o.status} /></td>
                        <td className="tabular py-2.5 text-right">{o.itemCount}</td>
                        <td className="tabular py-2.5 text-right font-semibold">{formatMoney(o.total)}</td>
                        <td className="py-2.5 text-right text-ink-faint">{formatDate(o.createdAt)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Panel>
        ) : (
          <Panel title="Recent orders">
            <Empty>The platform-wide order feed is visible to admins only.</Empty>
          </Panel>
        )}

        {/* Above-average + top products */}
        <div className="space-y-6">
          <Panel title="Above-average stores" action={<span className="eyebrow">subquery</span>}>
            {aboveAvg.isLoading ? (
              <Skeleton className="h-24 w-full" />
            ) : (aboveAvg.data ?? []).length === 0 ? (
              <Empty>No stores above the average yet.</Empty>
            ) : (
              <ul className="space-y-2.5">
                {(aboveAvg.data ?? []).map((r) => {
                  const s = storeMap.get(r.storeId);
                  const hue = hueFor(s?.storeType);
                  return (
                    <li key={r.storeId} className="flex items-center gap-3">
                      <span className="size-2 rounded-full" style={{ background: hue.accent }} />
                      <span className="min-w-0 flex-1 truncate text-sm font-medium">{storeName(r.storeId)}</span>
                      <TrendingUp className="size-3.5 text-success" />
                      <span className="tabular text-sm font-bold">{formatMoney(r.revenue)}</span>
                    </li>
                  );
                })}
              </ul>
            )}
          </Panel>

          <Panel
            title="Top products"
            action={
              <Link href="/dashboard/analytics" className="eyebrow flex items-center gap-1 hover:text-nexus">
                More <ArrowRight className="size-3" />
              </Link>
            }
          >
            {topProducts.isLoading ? (
              <Skeleton className="h-24 w-full" />
            ) : (topProducts.data ?? []).length === 0 ? (
              <Empty>No sales yet.</Empty>
            ) : (
              <ol className="space-y-2.5">
                {(topProducts.data ?? []).map((p, i) => (
                  <li key={p.productId} className="flex items-center gap-3 text-sm">
                    <span className="tabular w-5 text-ink-faint">{i + 1}</span>
                    <span className="min-w-0 flex-1 truncate font-medium">{p.name}</span>
                    <span className="tabular text-ink-faint">{p.unitsSold} sold</span>
                    <span className="tabular font-bold">{formatMoney(p.revenue)}</span>
                  </li>
                ))}
              </ol>
            )}
          </Panel>
        </div>
      </div>
    </div>
  );
}

function Empty({ children }: { children: React.ReactNode }) {
  return <p className="py-6 text-center text-sm text-ink-faint">{children}</p>;
}
