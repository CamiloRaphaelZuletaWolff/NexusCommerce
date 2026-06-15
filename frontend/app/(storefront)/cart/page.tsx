"use client";

import Link from "next/link";
import { Minus, Plus, ShoppingBag, Trash2 } from "lucide-react";
import { RequireAuth } from "@/components/auth/require-auth";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import {
  useCart,
  useClearCart,
  useRemoveCartItem,
  useUpdateCartItem,
} from "@/hooks/use-cart";
import { formatMoney } from "@/lib/utils";

export default function CartPage() {
  return (
    <RequireAuth>
      <CartView />
    </RequireAuth>
  );
}

function CartView() {
  const { data: cart, isLoading } = useCart();
  const updateItem = useUpdateCartItem();
  const removeItem = useRemoveCartItem();
  const clearCart = useClearCart();

  const items = cart?.items ?? [];

  return (
    <div className="container-page py-10">
      <h1 className="text-3xl font-extrabold">Your cart</h1>

      {isLoading ? (
        <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_360px]">
          <div className="space-y-3">
            {[0, 1, 2].map((i) => (
              <Skeleton key={i} className="h-24 w-full rounded-xl" />
            ))}
          </div>
          <Skeleton className="h-56 w-full rounded-xl" />
        </div>
      ) : items.length === 0 ? (
        <div className="mt-10 flex flex-col items-center justify-center rounded-2xl border border-dashed border-line-strong bg-surface py-20 text-center">
          <div className="grid size-16 place-items-center rounded-full bg-nexus-tint">
            <ShoppingBag className="size-7 text-nexus" />
          </div>
          <h2 className="mt-5 text-xl font-bold">Your cart is empty</h2>
          <p className="mt-1 max-w-sm text-ink-soft">
            Explore the store-worlds and add something you love.
          </p>
          <Button asChild variant="accent" className="mt-6">
            <Link href="/catalog">Browse the catalog</Link>
          </Button>
        </div>
      ) : (
        <div className="mt-8 grid items-start gap-8 lg:grid-cols-[1fr_360px]">
          {/* Items */}
          <div className="divide-y divide-line overflow-hidden rounded-2xl border border-line bg-surface">
            {items.map((item) => (
              <div key={item.id} className="flex items-center gap-4 p-4 sm:p-5">
                <div className="min-w-0 flex-1">
                  <Link
                    href={`/products/${item.productId}`}
                    className="font-semibold text-ink transition-colors hover:text-nexus"
                  >
                    {item.name}
                  </Link>
                  <p className="tabular mt-0.5 text-xs text-ink-faint">{item.sku}</p>
                  <p className="tabular mt-1 text-sm text-ink-soft">
                    {formatMoney(item.unitPrice)} each
                  </p>
                </div>

                <div className="flex items-center rounded-md border border-line-strong">
                  <button
                    className="grid size-9 place-items-center text-ink-soft transition-colors hover:text-ink disabled:opacity-40"
                    aria-label="Decrease quantity"
                    disabled={item.quantity <= 1 || updateItem.isPending}
                    onClick={() => updateItem.mutate({ itemId: item.id, quantity: item.quantity - 1 })}
                  >
                    <Minus className="size-4" />
                  </button>
                  <span className="tabular w-10 text-center text-sm font-semibold">{item.quantity}</span>
                  <button
                    className="grid size-9 place-items-center text-ink-soft transition-colors hover:text-ink disabled:opacity-40"
                    aria-label="Increase quantity"
                    disabled={updateItem.isPending}
                    onClick={() => updateItem.mutate({ itemId: item.id, quantity: item.quantity + 1 })}
                  >
                    <Plus className="size-4" />
                  </button>
                </div>

                <p className="tabular w-24 text-right font-bold">{formatMoney(item.lineTotal)}</p>

                <button
                  className="grid size-9 place-items-center rounded-md text-ink-faint transition-colors hover:bg-danger/10 hover:text-danger disabled:opacity-40"
                  aria-label={`Remove ${item.name}`}
                  disabled={removeItem.isPending}
                  onClick={() => removeItem.mutate(item.id)}
                >
                  <Trash2 className="size-4" />
                </button>
              </div>
            ))}
          </div>

          {/* Summary */}
          <div className="sticky top-20 rounded-2xl border border-line bg-surface p-5">
            <h2 className="text-lg font-bold">Order summary</h2>
            <dl className="mt-4 space-y-2.5 text-sm">
              <div className="flex justify-between">
                <dt className="text-ink-soft">Subtotal ({cart?.itemCount} items)</dt>
                <dd className="tabular font-semibold">{formatMoney(cart?.subtotal)}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-ink-soft">Tax</dt>
                <dd className="text-ink-faint">Calculated at checkout</dd>
              </div>
            </dl>
            <div className="my-4 h-px bg-line" />
            <div className="flex justify-between">
              <span className="font-semibold">Estimated total</span>
              <span className="tabular text-lg font-bold">{formatMoney(cart?.subtotal)}</span>
            </div>
            <Button asChild variant="accent" size="lg" className="mt-5 w-full">
              <Link href="/checkout">Proceed to checkout</Link>
            </Button>
            <button
              onClick={() => clearCart.mutate()}
              disabled={clearCart.isPending}
              className="mt-3 w-full text-center text-sm font-medium text-ink-faint transition-colors hover:text-danger disabled:opacity-40"
            >
              Clear cart
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
