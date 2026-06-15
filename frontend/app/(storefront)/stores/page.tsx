import type { Metadata } from "next";
import { Store } from "lucide-react";
import { StoreCard } from "@/components/stores/store-card";
import { serverApi } from "@/lib/server-api";
import { STORE_HUES, STORE_TYPES } from "@/lib/store-theme";

export const metadata: Metadata = {
  title: "Stores",
  description: "Browse every store-world on NexusCommerce.",
};

// Always reflect current stores (created from the dashboard).
export const dynamic = "force-dynamic";

export default async function StoresPage() {
  const { content: stores } = await serverApi.stores({ size: 100 });

  return (
    <div className="container-page py-10">
      <header className="mb-8 border-b border-line pb-6">
        <p className="eyebrow">The directory</p>
        <h1 className="mt-2 text-3xl font-extrabold sm:text-4xl">Every store-world</h1>
        <p className="mt-2 max-w-xl text-ink-soft">
          Five categories, each with its own identity and catalog — all shoppable from a
          single cart.
        </p>
      </header>

      {stores.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-line-strong bg-surface py-20 text-center">
          <div className="grid size-14 place-items-center rounded-full bg-paper-2">
            <Store className="size-6 text-ink-faint" />
          </div>
          <h3 className="mt-4 text-lg font-bold">No stores yet</h3>
          <p className="mt-1 text-sm text-ink-soft">Check back soon as store-worlds open.</p>
        </div>
      ) : (
        <div className="space-y-12">
          {STORE_TYPES.filter((t) => stores.some((s) => s.storeType === t)).map((type) => {
            const group = stores.filter((s) => s.storeType === type);
            return (
              <section key={type}>
                <div className="mb-4 flex items-center gap-2.5">
                  <span className="size-2.5 rounded-full" style={{ background: STORE_HUES[type].accent }} />
                  <h2 className="text-xl font-bold">{STORE_HUES[type].label}</h2>
                  <span className="tabular text-sm text-ink-faint">{group.length}</span>
                </div>
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                  {group.map((s) => (
                    <StoreCard key={s.id} store={s} />
                  ))}
                </div>
              </section>
            );
          })}
        </div>
      )}
    </div>
  );
}
