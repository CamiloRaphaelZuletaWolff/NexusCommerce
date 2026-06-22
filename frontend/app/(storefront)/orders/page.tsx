"use client";

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { ChevronRight, Package } from "lucide-react";
import { RequireAuth } from "@/components/auth/require-auth";
import { OrderStatusBadge } from "@/components/orders/status-badge";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { ErrorState } from "@/components/ui/error-state";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/components/auth-provider";
import { api } from "@/lib/api";
import { formatDate, formatMoney } from "@/lib/utils";

export default function OrdersPage() {
  return (
    <RequireAuth>
      <OrdersList />
    </RequireAuth>
  );
}

function OrdersList() {
  const { status } = useAuth();
  const { data: orders, isLoading, isError, refetch } = useQuery({
    queryKey: ["orders"],
    queryFn: api.orders.list,
    enabled: status === "authenticated",
  });

  return (
    <div className="container-page py-10">
      <h1 className="text-3xl font-extrabold">Your orders</h1>

      {isLoading ? (
        <div className="mt-8 space-y-3">
          {[0, 1, 2].map((i) => (
            <Skeleton key={i} className="h-24 w-full rounded-xl" />
          ))}
        </div>
      ) : isError ? (
        <ErrorState
          className="mt-8"
          title="Couldn’t load your orders"
          onRetry={() => refetch()}
        />
      ) : !orders || orders.length === 0 ? (
        <EmptyState
          className="mt-10"
          icon={Package}
          title="No orders yet"
          description="When you place an order, it’ll show up here."
          action={
            <Button asChild variant="accent">
              <Link href="/catalog">Start shopping</Link>
            </Button>
          }
        />
      ) : (
        <div className="mt-8 space-y-3">
          {orders.map((order) => (
            <Link
              key={order.id}
              href={`/orders/${order.id}`}
              className="group flex flex-wrap items-center gap-4 rounded-xl border border-line bg-surface p-5 transition-all hover:border-line-strong hover:shadow-sm"
            >
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-3">
                  <span className="tabular font-bold text-ink">{order.orderNumber}</span>
                  <OrderStatusBadge status={order.status} />
                </div>
                <p className="mt-1 text-sm text-ink-soft">
                  {formatDate(order.createdAt)} · {order.items.length}{" "}
                  {order.items.length === 1 ? "item" : "items"}
                </p>
              </div>
              <span className="tabular text-lg font-bold">{formatMoney(order.total, order.currency)}</span>
              <ChevronRight className="size-5 text-ink-faint transition-transform group-hover:translate-x-0.5" />
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
