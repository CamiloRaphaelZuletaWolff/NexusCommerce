"use client";

import { useEffect } from "react";
import Link from "next/link";
import { AnimatePresence, motion } from "framer-motion";
import { Minus, Plus, ShoppingBag, Trash2, X } from "lucide-react";
import { useUiStore } from "@/stores/ui";
import { useCart, useUpdateCartItem, useRemoveCartItem } from "@/hooks/use-cart";
import { useAuth } from "@/components/auth-provider";
import { Button } from "@/components/ui/button";
import { formatMoney } from "@/lib/utils";

export function CartDrawer() {
  const { cartOpen, closeCart } = useUiStore();
  const { status } = useAuth();
  const { data: cart, isLoading } = useCart();
  const updateItem = useUpdateCartItem();
  const removeItem = useRemoveCartItem();

  // Lock body scroll + close on Escape while open.
  useEffect(() => {
    if (!cartOpen) return;
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && closeCart();
    document.addEventListener("keydown", onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = prev;
    };
  }, [cartOpen, closeCart]);

  const items = cart?.items ?? [];

  return (
    <AnimatePresence>
      {cartOpen && (
        <>
          <motion.div
            className="fixed inset-0 z-50 bg-ink/30 backdrop-blur-[2px]"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={closeCart}
          />
          <motion.aside
            role="dialog"
            aria-modal="true"
            aria-label="Shopping cart"
            className="fixed inset-y-0 right-0 z-50 flex w-full max-w-md flex-col bg-paper shadow-lg"
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", stiffness: 360, damping: 38 }}
          >
            <header className="flex items-center justify-between border-b border-line px-5 py-4">
              <div className="flex items-center gap-2">
                <ShoppingBag className="size-5 text-nexus" />
                <h2 className="text-lg font-bold">Your cart</h2>
                {cart && cart.itemCount > 0 && (
                  <span className="tabular text-sm text-ink-faint">({cart.itemCount})</span>
                )}
              </div>
              <button
                onClick={closeCart}
                aria-label="Close cart"
                className="grid size-9 place-items-center rounded-md text-ink-soft transition-colors hover:bg-paper-2"
              >
                <X className="size-5" />
              </button>
            </header>

            <div className="flex-1 overflow-y-auto px-5 py-4">
              {status !== "authenticated" ? (
                <EmptyState
                  title="Sign in to shop"
                  body="Your cart is saved to your account so it's here when you come back."
                  action={
                    <Button asChild onClick={closeCart} className="mt-4">
                      <Link href="/login">Sign in</Link>
                    </Button>
                  }
                />
              ) : isLoading ? (
                <p className="py-10 text-center text-sm text-ink-faint">Loading your cart…</p>
              ) : items.length === 0 ? (
                <EmptyState
                  title="Your cart is empty"
                  body="Browse the stores and add something you love."
                  action={
                    <Button asChild variant="outline" onClick={closeCart} className="mt-4">
                      <Link href="/catalog">Browse catalog</Link>
                    </Button>
                  }
                />
              ) : (
                <ul className="space-y-3">
                  {items.map((item) => (
                    <li
                      key={item.id}
                      className="flex gap-3 rounded-lg border border-line bg-surface p-3"
                    >
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-semibold text-ink">{item.name}</p>
                        <p className="tabular mt-0.5 text-xs text-ink-faint">{item.sku}</p>
                        <div className="mt-2 flex items-center gap-2">
                          <div className="flex items-center rounded-md border border-line-strong">
                            <button
                              className="grid size-7 place-items-center text-ink-soft transition-colors hover:text-ink disabled:opacity-40"
                              aria-label="Decrease quantity"
                              disabled={item.quantity <= 1 || updateItem.isPending}
                              onClick={() =>
                                updateItem.mutate({ itemId: item.id, quantity: item.quantity - 1 })
                              }
                            >
                              <Minus className="size-3.5" />
                            </button>
                            <span className="tabular w-8 text-center text-sm font-semibold">
                              {item.quantity}
                            </span>
                            <button
                              className="grid size-7 place-items-center text-ink-soft transition-colors hover:text-ink disabled:opacity-40"
                              aria-label="Increase quantity"
                              disabled={updateItem.isPending}
                              onClick={() =>
                                updateItem.mutate({ itemId: item.id, quantity: item.quantity + 1 })
                              }
                            >
                              <Plus className="size-3.5" />
                            </button>
                          </div>
                          <button
                            className="ml-auto grid size-7 place-items-center rounded-md text-ink-faint transition-colors hover:bg-danger/10 hover:text-danger disabled:opacity-40"
                            aria-label={`Remove ${item.name}`}
                            disabled={removeItem.isPending}
                            onClick={() => removeItem.mutate(item.id)}
                          >
                            <Trash2 className="size-4" />
                          </button>
                        </div>
                      </div>
                      <p className="tabular shrink-0 text-sm font-semibold">
                        {formatMoney(item.lineTotal)}
                      </p>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            {status === "authenticated" && items.length > 0 && (
              <footer className="border-t border-line bg-surface px-5 py-4">
                <div className="mb-3 flex items-center justify-between">
                  <span className="text-sm text-ink-soft">Subtotal</span>
                  <span className="tabular text-lg font-bold">{formatMoney(cart?.subtotal)}</span>
                </div>
                <p className="mb-3 text-xs text-ink-faint">Tax calculated at checkout.</p>
                <Button asChild variant="accent" size="lg" className="w-full" onClick={closeCart}>
                  <Link href="/checkout">Checkout</Link>
                </Button>
                <Button asChild variant="ghost" className="mt-1.5 w-full" onClick={closeCart}>
                  <Link href="/cart">View full cart</Link>
                </Button>
              </footer>
            )}
          </motion.aside>
        </>
      )}
    </AnimatePresence>
  );
}

function EmptyState({
  title,
  body,
  action,
}: {
  title: string;
  body: string;
  action: React.ReactNode;
}) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="grid size-14 place-items-center rounded-full bg-nexus-tint">
        <ShoppingBag className="size-6 text-nexus" />
      </div>
      <h3 className="mt-4 text-base font-bold">{title}</h3>
      <p className="mt-1 max-w-xs text-sm text-ink-soft">{body}</p>
      {action}
    </div>
  );
}
