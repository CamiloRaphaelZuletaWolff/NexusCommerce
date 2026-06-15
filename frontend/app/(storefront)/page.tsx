import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { Hero } from "@/components/home/hero";
import { StoreWorldCard } from "@/components/home/store-world-card";
import { ProductCard } from "@/components/catalog/product-card";
import { Button } from "@/components/ui/button";
import { serverApi } from "@/lib/server-api";
import { STORE_TYPES, type StoreType } from "@/lib/store-theme";
import type { StoreResponse } from "@/types/api";

// Always reflect current stores/products (managers add them from the dashboard).
export const dynamic = "force-dynamic";

export default async function HomePage() {
  const [storesPage, featured] = await Promise.all([
    serverApi.stores({ size: 100 }),
    serverApi.products({ size: 8, status: "ACTIVE", sort: "createdAt,desc" }),
  ]);

  const stores = storesPage.content;
  const storeMap = new Map<string, StoreResponse>(stores.map((s) => [s.id, s]));
  const countByType = STORE_TYPES.reduce<Record<string, number>>((acc, t) => {
    acc[t] = stores.filter((s) => s.storeType === t).length;
    return acc;
  }, {});

  return (
    <>
      <Hero />

      {/* Store-worlds directory */}
      <section className="container-page py-16">
        <SectionHeader
          eyebrow="Shop by world"
          title="Each store-world, its own identity"
          action={{ href: "/stores", label: "All stores" }}
        />
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-5">
          {STORE_TYPES.map((t) => (
            <StoreWorldCard key={t} type={t as StoreType} count={countByType[t]} />
          ))}
        </div>
      </section>

      {/* Featured products */}
      {featured.content.length > 0 && (
        <section className="container-page py-8">
          <SectionHeader
            eyebrow="Fresh arrivals"
            title="Newest across every store"
            action={{ href: "/catalog", label: "Browse catalog" }}
          />
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
            {featured.content.map((p) => {
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
        </section>
      )}

      {/* Engineering band */}
      <section className="container-page py-16">
        <div className="overflow-hidden rounded-3xl border border-line bg-nexus-deep px-8 py-12 text-paper sm:px-12">
          <div className="grid gap-8 md:grid-cols-[1.3fr_1fr] md:items-center">
            <div>
              <p className="eyebrow text-paper/60">Built to be inspected</p>
              <h2 className="mt-3 text-3xl font-extrabold text-white sm:text-4xl">
                The right database for each job, bridged by UUID
              </h2>
              <p className="mt-4 max-w-lg text-paper/70">
                Orders, payments, and users live in PostgreSQL with ACID checkout and
                column-level encryption. Flexible, category-specific catalogs live in
                MongoDB. One shared key keeps them in sync.
              </p>
            </div>
            <div className="grid gap-3">
              {[
                ["PostgreSQL", "Users · orders · encrypted payments"],
                ["MongoDB", "Catalog · dynamic attributes · search"],
                ["UUID bridge", "products ↔ stores ↔ customers"],
              ].map(([k, v]) => (
                <div
                  key={k}
                  className="flex items-center justify-between rounded-xl bg-white/5 px-4 py-3 ring-1 ring-white/10"
                >
                  <span className="font-semibold text-white">{k}</span>
                  <span className="text-sm text-paper/60">{v}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>
    </>
  );
}

function SectionHeader({
  eyebrow,
  title,
  action,
}: {
  eyebrow: string;
  title: string;
  action?: { href: string; label: string };
}) {
  return (
    <div className="mb-7 flex items-end justify-between gap-4">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h2 className="mt-2 text-2xl font-extrabold sm:text-3xl">{title}</h2>
      </div>
      {action && (
        <Button asChild variant="ghost" size="sm" className="shrink-0">
          <Link href={action.href}>
            {action.label}
            <ArrowRight className="size-4" />
          </Link>
        </Button>
      )}
    </div>
  );
}
