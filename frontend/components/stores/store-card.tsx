import Link from "next/link";
import { ArrowUpRight } from "lucide-react";
import { accentVars, hueFor } from "@/lib/store-theme";
import { titleCase } from "@/lib/utils";
import type { StoreResponse } from "@/types/api";

export function StoreCard({ store }: { store: StoreResponse }) {
  const hue = hueFor(store.storeType);
  return (
    <Link
      href={`/stores/${store.slug}`}
      style={accentVars(store.storeType)}
      className="group relative flex flex-col overflow-hidden rounded-2xl border border-line bg-surface transition-all duration-200 hover:-translate-y-1 hover:shadow-md"
    >
      <div className="relative h-28 overflow-hidden" style={{ background: hue.soft }}>
        <div
          className="absolute -right-6 -top-10 size-36 rounded-full opacity-40 blur-xl transition-transform duration-300 group-hover:scale-110"
          style={{ background: hue.accent }}
          aria-hidden
        />
        <span
          className="absolute left-4 top-4 rounded-full px-2.5 py-1 text-xs font-semibold text-white"
          style={{ background: hue.accent }}
        >
          {hue.label}
        </span>
      </div>
      <div className="flex flex-1 flex-col p-5">
        <div className="flex items-start justify-between gap-2">
          <h3 className="text-lg font-bold text-ink">{store.name}</h3>
          <ArrowUpRight className="size-4 shrink-0 text-ink-faint transition-colors group-hover:text-accent" />
        </div>
        <p className="mt-1.5 line-clamp-2 text-sm text-ink-soft">
          {store.description || `A ${titleCase(store.storeType)} store on NexusCommerce.`}
        </p>
      </div>
    </Link>
  );
}
