"use client";

import { useState } from "react";
import Link from "next/link";
import { Minus, Plus, ShoppingBag } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAddToCart } from "@/hooks/use-cart";
import { useAuth } from "@/components/auth-provider";

export function AddToCartPanel({ productId }: { productId: string }) {
  const [qty, setQty] = useState(1);
  const addToCart = useAddToCart();
  const { status } = useAuth();

  if (status === "unauthenticated") {
    return (
      <div className="rounded-xl border border-line bg-surface p-4">
        <p className="text-sm text-ink-soft">
          <Link href="/login" className="font-semibold text-accent underline-offset-2 hover:underline">
            Sign in
          </Link>{" "}
          to add this to your cart.
        </p>
      </div>
    );
  }

  return (
    <div className="flex flex-wrap items-center gap-3">
      <div className="flex h-12 items-center rounded-md border border-line-strong bg-surface">
        <button
          onClick={() => setQty((q) => Math.max(1, q - 1))}
          disabled={qty <= 1}
          className="grid size-11 place-items-center text-ink-soft transition-colors hover:text-ink disabled:opacity-40"
          aria-label="Decrease quantity"
        >
          <Minus className="size-4" />
        </button>
        <span className="tabular w-10 text-center font-semibold">{qty}</span>
        <button
          onClick={() => setQty((q) => q + 1)}
          className="grid size-11 place-items-center text-ink-soft transition-colors hover:text-ink"
          aria-label="Increase quantity"
        >
          <Plus className="size-4" />
        </button>
      </div>
      <Button
        variant="accent"
        size="lg"
        className="flex-1"
        loading={addToCart.isPending}
        onClick={() => addToCart.mutate({ productId, quantity: qty })}
      >
        <ShoppingBag className="size-5" />
        Add to cart
      </Button>
    </div>
  );
}
