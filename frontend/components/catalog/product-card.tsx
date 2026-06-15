"use client";

import Link from "next/link";
import { Plus, Star } from "lucide-react";
import { ProductArt } from "@/components/product-art";
import { useAddToCart } from "@/hooks/use-cart";
import { accentVars, hueFor } from "@/lib/store-theme";
import { cn, formatMoney } from "@/lib/utils";
import type { ProductResponse, StoreType } from "@/types/api";

export function ProductCard({
  product,
  storeType,
  storeName,
  className,
}: {
  product: ProductResponse;
  storeType?: StoreType | null;
  storeName?: string;
  className?: string;
}) {
  const addToCart = useAddToCart();
  const hue = hueFor(storeType);

  return (
    <article
      style={accentVars(storeType)}
      className={cn(
        "group relative flex flex-col overflow-hidden rounded-xl border border-line bg-surface transition-all duration-200 hover:-translate-y-0.5 hover:border-line-strong hover:shadow-md",
        className,
      )}
    >
      <Link
        href={`/products/${product.id}`}
        className="absolute inset-0 z-10"
        aria-label={product.name}
      />

      <div className="relative aspect-[4/3] overflow-hidden border-b border-line">
        <ProductArt
          seed={product.id}
          accent={hue.accent}
          label={product.name}
          className="h-full w-full transition-transform duration-300 group-hover:scale-[1.03]"
        />
        {storeName && (
          <span
            className="absolute left-3 top-3 rounded-full px-2.5 py-1 text-[11px] font-semibold text-white shadow-sm"
            style={{ background: hue.accent }}
          >
            {storeName}
          </span>
        )}
        <button
          type="button"
          onClick={() => addToCart.mutate({ productId: product.id })}
          disabled={addToCart.isPending}
          aria-label={`Add ${product.name} to cart`}
          className="absolute bottom-3 right-3 z-20 grid size-10 translate-y-2 place-items-center rounded-full bg-ink text-paper opacity-0 shadow-md transition-all duration-200 hover:scale-105 group-hover:translate-y-0 group-hover:opacity-100 focus-visible:translate-y-0 focus-visible:opacity-100"
        >
          <Plus className="size-5" />
        </button>
      </div>

      <div className="flex flex-1 flex-col p-4">
        <div className="flex items-center justify-between gap-2">
          {product.brand ? (
            <span className="eyebrow truncate">{product.brand}</span>
          ) : (
            <span className="eyebrow truncate">{product.categorySlug}</span>
          )}
          {product.ratingCount > 0 && (
            <span className="tabular flex items-center gap-0.5 text-xs text-ink-faint">
              <Star className="size-3 fill-warning text-warning" />
              {product.ratingAverage.toFixed(1)}
            </span>
          )}
        </div>
        <h3 className="mt-1 line-clamp-2 text-sm font-semibold leading-snug text-ink">
          {product.name}
        </h3>
        <p className="tabular mt-auto pt-3 text-base font-bold text-ink">
          {formatMoney(product.price, product.currency)}
        </p>
      </div>
    </article>
  );
}
