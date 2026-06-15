import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { ChevronRight, PackageOpen } from "lucide-react";
import { ProductCard } from "@/components/catalog/product-card";
import { serverApi } from "@/lib/server-api";
import { accentVars, hueFor } from "@/lib/store-theme";
import { titleCase } from "@/lib/utils";
import type { StoreResponse } from "@/types/api";

async function loadStore(slug: string): Promise<StoreResponse | null> {
  try {
    return await serverApi.store(slug);
  } catch {
    return null;
  }
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ slug: string }>;
}): Promise<Metadata> {
  const { slug } = await params;
  const store = await loadStore(slug);
  return { title: store?.name ?? "Store", description: store?.description ?? undefined };
}

export default async function StorePage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const store = await loadStore(slug);
  if (!store) notFound();

  const hue = hueFor(store.storeType);
  const products = (
    await serverApi.products({ storeId: store.id, status: "ACTIVE", size: 24 })
  ).content;

  return (
    <div style={accentVars(store.storeType)}>
      {/* Themed store banner */}
      <header className="relative overflow-hidden border-b border-line" style={{ background: hue.soft }}>
        <div
          className="absolute -left-20 -top-24 size-72 rounded-full opacity-40 blur-3xl"
          style={{ background: hue.accent }}
          aria-hidden
        />
        <div className="container-page relative py-12">
          <nav className="mb-5 flex items-center gap-1.5 text-sm" style={{ color: hue.ink }} aria-label="Breadcrumb">
            <Link href="/stores" className="opacity-70 transition-opacity hover:opacity-100">Stores</Link>
            <ChevronRight className="size-3.5" />
            <span>{store.name}</span>
          </nav>
          <span
            className="inline-flex rounded-full px-2.5 py-1 text-xs font-semibold text-white"
            style={{ background: hue.accent }}
          >
            {hue.label}
          </span>
          <h1 className="mt-3 text-4xl font-extrabold" style={{ color: hue.ink }}>
            {store.name}
          </h1>
          {store.description && (
            <p className="mt-3 max-w-2xl" style={{ color: hue.ink, opacity: 0.8 }}>
              {store.description}
            </p>
          )}
        </div>
      </header>

      <div className="container-page py-10">
        <div className="mb-6 flex items-center justify-between">
          <h2 className="text-xl font-bold">Products</h2>
          <span className="tabular text-sm text-ink-faint">
            {products.length} {products.length === 1 ? "item" : "items"}
          </span>
        </div>

        {products.length === 0 ? (
          <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-line-strong bg-surface py-20 text-center">
            <div className="grid size-14 place-items-center rounded-full bg-paper-2">
              <PackageOpen className="size-6 text-ink-faint" />
            </div>
            <h3 className="mt-4 text-lg font-bold">No products yet</h3>
            <p className="mt-1 text-sm text-ink-soft">
              This {titleCase(store.storeType)} store hasn’t listed anything yet.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
            {products.map((p) => (
              <ProductCard key={p.id} product={p} storeType={store.storeType} storeName={store.name} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
