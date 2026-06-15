import Link from "next/link";
import { ArrowUpRight } from "lucide-react";
import { accentVars, hueFor, type StoreType } from "@/lib/store-theme";

const TAGLINES: Record<StoreType, string> = {
  ELECTRONICS: "Devices, audio & smart gear",
  FASHION: "Apparel, footwear & accessories",
  FURNITURE: "Seating, tables & storage",
  KITCHENWARE: "Cookware, tools & dining",
  HOME_DECOR: "Lighting, textiles & objects",
};

export function StoreWorldCard({
  type,
  count,
}: {
  type: StoreType;
  count?: number;
}) {
  const hue = hueFor(type);
  return (
    <Link
      href={`/catalog?type=${type}`}
      style={accentVars(type)}
      className="group relative flex flex-col justify-between overflow-hidden rounded-2xl border border-line bg-surface p-5 transition-all duration-200 hover:-translate-y-1 hover:shadow-md"
    >
      <div
        className="absolute -right-8 -top-8 size-28 rounded-full opacity-15 transition-transform duration-300 group-hover:scale-125"
        style={{ background: hue.accent }}
        aria-hidden
      />
      <div className="relative flex items-start justify-between">
        <span className="size-3 rounded-full" style={{ background: hue.accent }} />
        <ArrowUpRight className="size-4 text-ink-faint transition-colors group-hover:text-accent" />
      </div>
      <div className="relative mt-10">
        <h3 className="text-lg font-bold text-ink">{hue.label}</h3>
        <p className="mt-1 text-sm text-ink-soft">{TAGLINES[type]}</p>
        {count !== undefined && (
          <p className="tabular mt-3 text-xs text-ink-faint">
            {count} {count === 1 ? "store" : "stores"}
          </p>
        )}
      </div>
    </Link>
  );
}
