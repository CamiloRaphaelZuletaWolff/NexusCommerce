import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { ChevronRight, Star } from "lucide-react";
import { ProductArt } from "@/components/product-art";
import { ProductCard } from "@/components/catalog/product-card";
import { AddToCartPanel } from "@/components/catalog/add-to-cart-panel";
import { RecordRecentlyViewed } from "@/components/catalog/record-recently-viewed";
import { Badge } from "@/components/ui/badge";
import { serverApi } from "@/lib/server-api";
import { accentVars, hueFor } from "@/lib/store-theme";
import { formatMoney, titleCase } from "@/lib/utils";
import type { ProductResponse, StoreResponse } from "@/types/api";

async function loadProduct(id: string): Promise<ProductResponse | null> {
  try {
    return await serverApi.product(id);
  } catch {
    return null;
  }
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ id: string }>;
}): Promise<Metadata> {
  const { id } = await params;
  const product = await loadProduct(id);
  return {
    title: product?.name ?? "Product",
    description: product?.description ?? undefined,
  };
}

export default async function ProductPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const product = await loadProduct(id);
  if (!product) notFound();

  const storesPage = await serverApi.stores({ size: 100 });
  const store = storesPage.content.find((s) => s.id === product.storeId);
  const storeMap = new Map<string, StoreResponse>(storesPage.content.map((s) => [s.id, s]));
  const hue = hueFor(store?.storeType);

  // Related: same store, excluding this product.
  const related = (
    await serverApi.products({ storeId: product.storeId, status: "ACTIVE", size: 5 })
  ).content
    .filter((p) => p.id !== product.id)
    .slice(0, 4);

  const attrs = Object.entries(product.attributes ?? {}).filter(
    ([, v]) => v !== null && v !== undefined && v !== "",
  );

  return (
    <div style={accentVars(store?.storeType)} className="container-page py-8">
      <RecordRecentlyViewed productId={product.id} />
      {/* Breadcrumb */}
      <nav className="mb-6 flex items-center gap-1.5 text-sm text-ink-faint" aria-label="Breadcrumb">
        <Link href="/catalog" className="transition-colors hover:text-ink">Catalog</Link>
        <ChevronRight className="size-3.5" />
        {store && (
          <>
            <Link href={`/catalog?store=${store.id}`} className="transition-colors hover:text-ink">
              {store.name}
            </Link>
            <ChevronRight className="size-3.5" />
          </>
        )}
        <span className="truncate text-ink">{product.name}</span>
      </nav>

      <div className="grid gap-10 lg:grid-cols-2">
        {/* Art */}
        <div className="lg:sticky lg:top-20 lg:self-start">
          <div className="overflow-hidden rounded-2xl border border-line bg-surface">
            <ProductArt
              seed={product.id}
              accent={hue.accent}
              label={product.name}
              className="aspect-square w-full"
            />
          </div>
          {product.tags.length > 0 && (
            <div className="mt-4 flex flex-wrap gap-2">
              {product.tags.map((t) => (
                <Badge key={t} tone="neutral">{t}</Badge>
              ))}
            </div>
          )}
        </div>

        {/* Details */}
        <div>
          <div className="flex items-center gap-3">
            {store && (
              <Link
                href={`/catalog?store=${store.id}`}
                className="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold text-white"
                style={{ background: hue.accent }}
              >
                {store.name}
              </Link>
            )}
            {product.brand && <span className="eyebrow">{product.brand}</span>}
          </div>

          <h1 className="mt-3 text-3xl font-extrabold sm:text-4xl">{product.name}</h1>

          <div className="mt-3 flex items-center gap-4">
            <p className="tabular text-2xl font-bold">
              {formatMoney(product.price, product.currency)}
            </p>
            {product.ratingCount > 0 && (
              <span className="tabular flex items-center gap-1 text-sm text-ink-soft">
                <Star className="size-4 fill-warning text-warning" />
                {product.ratingAverage.toFixed(1)}
                <span className="text-ink-faint">({product.ratingCount})</span>
              </span>
            )}
          </div>

          {product.description && (
            <p className="mt-5 leading-relaxed text-ink-soft">{product.description}</p>
          )}

          <div className="mt-7">
            <AddToCartPanel productId={product.id} />
          </div>

          {attrs.length > 0 && (
            <div className="mt-9">
              <h2 className="eyebrow mb-3">Specifications</h2>
              <dl className="overflow-hidden rounded-xl border border-line">
                {attrs.map(([k, v], i) => (
                  <div
                    key={k}
                    className={`flex justify-between gap-4 px-4 py-3 text-sm ${
                      i % 2 ? "bg-surface" : "bg-paper-2/40"
                    }`}
                  >
                    <dt className="text-ink-soft">{titleCase(k)}</dt>
                    <dd className="tabular text-right font-medium text-ink">{String(v)}</dd>
                  </div>
                ))}
              </dl>
            </div>
          )}

          {product.variants.length > 0 && (
            <div className="mt-9">
              <h2 className="eyebrow mb-3">Variants</h2>
              <ul className="space-y-2">
                {product.variants.map((v) => (
                  <li
                    key={v.sku}
                    className="flex items-center justify-between rounded-lg border border-line bg-surface px-4 py-3 text-sm"
                  >
                    <div>
                      <p className="tabular font-medium text-ink">{v.sku}</p>
                      <p className="text-xs text-ink-faint">
                        {Object.entries(v.attributes ?? {})
                          .map(([k, val]) => `${titleCase(k)}: ${val}`)
                          .join(" · ") || "Standard"}
                      </p>
                    </div>
                    <div className="text-right">
                      {v.priceOverride != null && (
                        <p className="tabular font-semibold">{formatMoney(v.priceOverride, product.currency)}</p>
                      )}
                      {v.stock != null && (
                        <p className="tabular text-xs text-ink-faint">{v.stock} in stock</p>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      </div>

      {related.length > 0 && (
        <section className="mt-20">
          <h2 className="mb-6 text-2xl font-extrabold">More from {store?.name ?? "this store"}</h2>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
            {related.map((p) => (
              <ProductCard
                key={p.id}
                product={p}
                storeType={storeMap.get(p.storeId)?.storeType}
                storeName={storeMap.get(p.storeId)?.name}
              />
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
