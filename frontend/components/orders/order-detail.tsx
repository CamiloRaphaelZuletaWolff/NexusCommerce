"use client";

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { CheckCircle2, CreditCard, MapPin, PackageX } from "lucide-react";
import { OrderStatusBadge } from "@/components/orders/status-badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuth } from "@/components/auth-provider";
import { api } from "@/lib/api";
import { formatDate, formatMoney } from "@/lib/utils";

export function OrderDetail({ id, success }: { id: string; success?: boolean }) {
  const { status } = useAuth();
  const { data: order, isLoading, isError } = useQuery({
    queryKey: ["orders", id],
    queryFn: () => api.orders.byId(id),
    enabled: status === "authenticated",
  });

  if (isLoading) {
    return (
      <div className="container-page py-10">
        <Skeleton className="h-8 w-48" />
        <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_340px]">
          <Skeleton className="h-72 rounded-2xl" />
          <Skeleton className="h-72 rounded-2xl" />
        </div>
      </div>
    );
  }

  if (isError || !order) {
    return (
      <div className="container-page grid min-h-[40vh] place-items-center py-10">
        <div className="text-center">
          <div className="mx-auto grid size-14 place-items-center rounded-full bg-paper-2">
            <PackageX className="size-6 text-ink-faint" />
          </div>
          <h1 className="mt-4 text-2xl font-extrabold">Order not found</h1>
          <p className="mt-1 text-ink-soft">We couldn’t find that order on your account.</p>
          <Button asChild className="mt-6">
            <Link href="/orders">Back to orders</Link>
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="container-page py-10">
      {success && (
        <div className="mb-8 flex items-center gap-3 rounded-2xl border border-success/30 bg-success/8 p-5">
          <CheckCircle2 className="size-7 shrink-0 text-success" />
          <div>
            <h2 className="text-lg font-bold text-ink">Order placed</h2>
            <p className="text-sm text-ink-soft">
              Stock was reserved and your payment recorded in one atomic transaction.
            </p>
          </div>
        </div>
      )}

      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="eyebrow">Order</p>
          <h1 className="tabular mt-1 text-3xl font-extrabold">{order.orderNumber}</h1>
          <p className="mt-1 text-sm text-ink-soft">Placed {formatDate(order.createdAt)}</p>
        </div>
        <OrderStatusBadge status={order.status} />
      </div>

      <div className="mt-8 grid items-start gap-8 lg:grid-cols-[1fr_340px]">
        {/* Items */}
        <div className="divide-y divide-line overflow-hidden rounded-2xl border border-line bg-surface">
          {order.items.map((item) => (
            <div key={item.id} className="flex items-center gap-4 p-5">
              <div className="min-w-0 flex-1">
                <Link
                  href={`/products/${item.productId}`}
                  className="font-semibold text-ink transition-colors hover:text-nexus"
                >
                  {item.name}
                </Link>
                <p className="tabular mt-0.5 text-xs text-ink-faint">{item.sku}</p>
                <p className="tabular mt-1 text-sm text-ink-soft">
                  {formatMoney(item.unitPrice, order.currency)} × {item.quantity}
                </p>
              </div>
              <span className="tabular font-bold">{formatMoney(item.lineTotal, order.currency)}</span>
            </div>
          ))}
        </div>

        {/* Sidebar */}
        <div className="space-y-5">
          <div className="rounded-2xl border border-line bg-surface p-5">
            <h2 className="text-lg font-bold">Summary</h2>
            <dl className="mt-4 space-y-2.5 text-sm">
              <Row label="Subtotal" value={formatMoney(order.subtotal, order.currency)} />
              <Row label="Tax" value={formatMoney(order.tax, order.currency)} />
              <div className="my-3 h-px bg-line" />
              <div className="flex justify-between">
                <dt className="font-semibold">Total</dt>
                <dd className="tabular text-lg font-bold">{formatMoney(order.total, order.currency)}</dd>
              </div>
            </dl>
          </div>

          <div className="rounded-2xl border border-line bg-surface p-5">
            <h2 className="flex items-center gap-2 text-sm font-bold">
              <MapPin className="size-4 text-ink-soft" />
              Shipping
            </h2>
            <address className="mt-3 text-sm not-italic leading-relaxed text-ink-soft">
              <span className="block font-medium text-ink">{order.shipping.name}</span>
              {order.shipping.line1}
              {order.shipping.line2 && <>, {order.shipping.line2}</>}
              <br />
              {order.shipping.city}
              {order.shipping.region && <>, {order.shipping.region}</>} {order.shipping.postalCode}
              <br />
              {order.shipping.country}
            </address>
          </div>

          {order.payment && (
            <div className="rounded-2xl border border-line bg-surface p-5">
              <h2 className="flex items-center gap-2 text-sm font-bold">
                <CreditCard className="size-4 text-ink-soft" />
                Payment
              </h2>
              <div className="mt-3 flex items-center justify-between text-sm">
                <span className="text-ink-soft">
                  {order.payment.cardBrand ?? "Card"} ····{" "}
                  <span className="tabular">{order.payment.cardLast4 ?? "—"}</span>
                </span>
                <span className="font-semibold text-success">{order.payment.status}</span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between">
      <dt className="text-ink-soft">{label}</dt>
      <dd className="tabular font-semibold">{value}</dd>
    </div>
  );
}
