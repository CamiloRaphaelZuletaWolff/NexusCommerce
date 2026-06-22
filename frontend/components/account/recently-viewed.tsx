"use client";

import { useQuery } from "@tanstack/react-query";
import { Clock } from "lucide-react";
import { ProductCard } from "@/components/catalog/product-card";
import { Skeleton } from "@/components/ui/skeleton";
import { api } from "@/lib/api";
import type { ProductResponse, StoreResponse } from "@/types/api";

/**
 * Renders the products the customer recently opened — the ids come from the MongoDB
 * profile, populated as they browse (the read half of the recently-viewed loop).
 */
export function RecentlyViewed({ productIds }: { productIds: string[] }) {
  const { data: products, isLoading } = useQuery({
    queryKey: ["recentlyViewed", productIds],
    enabled: productIds.length > 0,
    queryFn: async () => {
      const results = await Promise.all(
        productIds.map((id) => api.catalog.product(id).catch(() => null)),
      );
      return results.filter((p): p is ProductResponse => p !== null);
    },
  });

  const { data: storesPage } = useQuery({
    queryKey: ["stores-all"],
    queryFn: () => api.stores.list({ size: 100 }),
    enabled: productIds.length > 0,
  });
  const storeMap = new Map<string, StoreResponse>(
    (storesPage?.content ?? []).map((s) => [s.id, s]),
  );

  return (
    <section>
      <div className="flex items-center gap-2">
        <Clock className="size-4 text-ink-faint" />
        <h2 className="text-xl font-extrabold">Recently viewed</h2>
      </div>

      {productIds.length === 0 ? (
        <p className="mt-3 text-sm text-ink-soft">
          Nothing yet. Open a product and it’ll appear here — recorded to your MongoDB
          profile as you browse.
        </p>
      ) : isLoading ? (
        <div className="mt-5 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
          {productIds.slice(0, 4).map((id) => (
            <Skeleton key={id} className="aspect-[3/4] w-full rounded-xl" />
          ))}
        </div>
      ) : (
        <div className="mt-5 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
          {(products ?? []).map((p) => (
            <ProductCard
              key={p.id}
              product={p}
              storeType={storeMap.get(p.storeId)?.storeType}
              storeName={storeMap.get(p.storeId)?.name}
            />
          ))}
        </div>
      )}
    </section>
  );
}
