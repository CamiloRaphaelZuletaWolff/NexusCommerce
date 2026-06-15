import type { Metadata } from "next";
import { PackageOpen } from "lucide-react";
import { ProductCard } from "@/components/catalog/product-card";
import { CatalogFilters, type CatalogQueryState } from "@/components/catalog/catalog-filters";
import { Pagination } from "@/components/catalog/pagination";
import { serverApi } from "@/lib/server-api";
import { accentVars, hueFor } from "@/lib/store-theme";
import type { StoreResponse } from "@/types/api";

export const metadata: Metadata = { title: "Catalog" };

type SP = Record<string, string | string[] | undefined>;

const arr = (v: string | string[] | undefined): string[] =>
  v === undefined ? [] : Array.isArray(v) ? v : [v];
const one = (v: string | string[] | undefined): string | undefined =>
  Array.isArray(v) ? v[0] : v;

const PAGE_SIZE = 12;

export default async function CatalogPage({
  searchParams,
}: {
  searchParams: Promise<SP>;
}) {
  const sp = await searchParams;

  const state: CatalogQueryState = {
    q: one(sp.q),
    categories: arr(sp.category),
    brands: arr(sp.brand),
    store: one(sp.store),
    type: one(sp.type),
    minPrice: one(sp.minPrice),
    maxPrice: one(sp.maxPrice),
    sort: one(sp.sort),
  };
  const page = Math.max(0, Number(one(sp.page) ?? 0) || 0);

  const [categories, brands, storesPage] = await Promise.all([
    serverApi.categories(),
    serverApi.brands(),
    serverApi.stores({ size: 100 }),
  ]);
  const stores = storesPage.content;
  const storeMap = new Map<string, StoreResponse>(stores.map((s) => [s.id, s]));

  // Resolve a "world" (storeType) to a concrete store filter when possible.
  let storeId = state.store;
  let headerType = state.store ? storeMap.get(state.store)?.storeType : undefined;
  if (!storeId && state.type) {
    const ofType = stores.filter((s) => s.storeType === state.type);
    if (ofType.length >= 1) storeId = ofType[0].id;
    headerType = state.type as StoreResponse["storeType"];
  }

  const result = await serverApi.products({
    q: state.q,
    categories: state.categories.length ? state.categories : undefined,
    brands: state.brands.length ? state.brands : undefined,
    storeId,
    minPrice: state.minPrice,
    maxPrice: state.maxPrice,
    status: "ACTIVE",
    sort: state.sort ?? "createdAt,desc",
    page: String(page),
    size: String(PAGE_SIZE),
  });

  const hue = hueFor(headerType);
  const title = headerType
    ? hue.label
    : state.q
      ? `Results for “${state.q}”`
      : "Full catalog";

  return (
    <div style={accentVars(headerType)} className="container-page py-10">
      <header className="mb-8 border-b border-line pb-6">
        <p className="eyebrow">{headerType ? "Store-world" : "Catalog"}</p>
        <h1 className="mt-2 text-3xl font-extrabold sm:text-4xl">{title}</h1>
        <p className="tabular mt-2 text-sm text-ink-soft">
          {result.totalElements} {result.totalElements === 1 ? "product" : "products"}
        </p>
      </header>

      <div className="grid gap-8 lg:grid-cols-[260px_1fr]">
        <CatalogFilters current={state} categories={categories} brands={brands} stores={stores} />

        <div>
          {result.content.length === 0 ? (
            <EmptyCatalog />
          ) : (
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
              {result.content.map((p) => {
                const store = storeMap.get(p.storeId);
                return (
                  <ProductCard
                    key={p.id}
                    product={p}
                    storeType={store?.storeType}
                    storeName={store?.name}
                  />
                );
              })}
            </div>
          )}

          <Pagination
            page={page}
            totalPages={result.totalPages}
            params={{
              q: state.q,
              category: state.categories,
              brand: state.brands,
              store: state.store,
              type: state.type,
              minPrice: state.minPrice,
              maxPrice: state.maxPrice,
              sort: state.sort,
            }}
          />
        </div>
      </div>
    </div>
  );
}

function EmptyCatalog() {
  return (
    <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-line-strong bg-surface py-20 text-center">
      <div className="grid size-14 place-items-center rounded-full bg-paper-2">
        <PackageOpen className="size-6 text-ink-faint" />
      </div>
      <h3 className="mt-4 text-lg font-bold">No products match</h3>
      <p className="mt-1 max-w-sm text-sm text-ink-soft">
        Try widening your price range or clearing a filter.
      </p>
    </div>
  );
}
