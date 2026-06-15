"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Search, SlidersHorizontal, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/input";
import { STORE_HUES } from "@/lib/store-theme";
import { cn } from "@/lib/utils";
import type { BrandResponse, CategoryResponse, StoreResponse } from "@/types/api";

export interface CatalogQueryState {
  q?: string;
  categories: string[];
  brands: string[];
  store?: string;
  type?: string;
  minPrice?: string;
  maxPrice?: string;
  sort?: string;
}

const SORTS = [
  { value: "createdAt,desc", label: "Newest" },
  { value: "price,asc", label: "Price: low to high" },
  { value: "price,desc", label: "Price: high to low" },
  { value: "name,asc", label: "Name: A–Z" },
];

export function CatalogFilters({
  current,
  categories,
  brands,
  stores,
}: {
  current: CatalogQueryState;
  categories: CategoryResponse[];
  brands: BrandResponse[];
  stores: StoreResponse[];
}) {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [minPrice, setMinPrice] = useState(current.minPrice ?? "");
  const [maxPrice, setMaxPrice] = useState(current.maxPrice ?? "");
  const [q, setQ] = useState(current.q ?? "");

  function commit(next: CatalogQueryState) {
    const sp = new URLSearchParams();
    if (next.q) sp.set("q", next.q);
    next.categories.forEach((c) => sp.append("category", c));
    next.brands.forEach((b) => sp.append("brand", b));
    if (next.store) sp.set("store", next.store);
    if (next.type) sp.set("type", next.type);
    if (next.minPrice) sp.set("minPrice", next.minPrice);
    if (next.maxPrice) sp.set("maxPrice", next.maxPrice);
    if (next.sort) sp.set("sort", next.sort);
    router.push(`/catalog${sp.toString() ? `?${sp}` : ""}`, { scroll: false });
  }

  const toggle = (key: "categories" | "brands", value: string) => {
    const set = new Set(current[key]);
    if (set.has(value)) set.delete(value);
    else set.add(value);
    commit({ ...current, [key]: [...set] });
  };

  const activeCount =
    current.categories.length +
    current.brands.length +
    (current.store ? 1 : 0) +
    (current.type ? 1 : 0) +
    (current.minPrice ? 1 : 0) +
    (current.maxPrice ? 1 : 0) +
    (current.q ? 1 : 0);

  const panel = (
    <div className="space-y-7">
      <div>
        <FilterHeading>Search</FilterHeading>
        <form
          onSubmit={(e) => {
            e.preventDefault();
            commit({ ...current, q: q.trim() || undefined });
          }}
          className="relative"
        >
          <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-ink-faint" />
          <input
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Search products…"
            className="h-10 w-full rounded-md border border-line-strong bg-surface pl-9 pr-3 text-sm outline-none focus:border-nexus focus:ring-4 focus:ring-nexus/12"
          />
        </form>
      </div>

      <div>
        <FilterHeading>Sort by</FilterHeading>
        <Select
          value={current.sort ?? "createdAt,desc"}
          onChange={(e) => commit({ ...current, sort: e.target.value })}
        >
          {SORTS.map((s) => (
            <option key={s.value} value={s.value}>
              {s.label}
            </option>
          ))}
        </Select>
      </div>

      <div>
        <FilterHeading>Price range</FilterHeading>
        <div className="flex items-center gap-2">
          <input
            type="number"
            inputMode="decimal"
            min={0}
            placeholder="Min"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
            onBlur={() => commit({ ...current, minPrice: minPrice || undefined })}
            className="tabular h-10 w-full rounded-md border border-line-strong bg-surface px-3 text-sm outline-none focus:border-nexus"
          />
          <span className="text-ink-faint">—</span>
          <input
            type="number"
            inputMode="decimal"
            min={0}
            placeholder="Max"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
            onBlur={() => commit({ ...current, maxPrice: maxPrice || undefined })}
            className="tabular h-10 w-full rounded-md border border-line-strong bg-surface px-3 text-sm outline-none focus:border-nexus"
          />
        </div>
      </div>

      {stores.length > 0 && (
        <div>
          <FilterHeading>Store</FilterHeading>
          <Select
            value={current.store ?? ""}
            onChange={(e) => commit({ ...current, store: e.target.value || undefined, type: undefined })}
          >
            <option value="">All stores</option>
            {stores.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </Select>
        </div>
      )}

      {categories.length > 0 && (
        <div>
          <FilterHeading>Category</FilterHeading>
          <CheckList
            options={categories.map((c) => ({ value: c.slug, label: c.name }))}
            selected={current.categories}
            onToggle={(v) => toggle("categories", v)}
          />
        </div>
      )}

      {brands.length > 0 && (
        <div>
          <FilterHeading>Brand</FilterHeading>
          <CheckList
            options={brands.map((b) => ({ value: b.name, label: b.name }))}
            selected={current.brands}
            onToggle={(v) => toggle("brands", v)}
          />
        </div>
      )}

      {activeCount > 0 && (
        <Button variant="ghost" size="sm" className="w-full" onClick={() => router.push("/catalog", { scroll: false })}>
          <X className="size-4" />
          Clear all filters
        </Button>
      )}
    </div>
  );

  return (
    <>
      {/* Mobile toggle */}
      <div className="mb-4 lg:hidden">
        <Button variant="outline" className="w-full" onClick={() => setOpen((o) => !o)}>
          <SlidersHorizontal className="size-4" />
          Filters{activeCount > 0 ? ` (${activeCount})` : ""}
        </Button>
        {open && (
          <div className="mt-4 rounded-xl border border-line bg-surface p-5">{panel}</div>
        )}
      </div>

      {/* Desktop sidebar */}
      <aside className="hidden lg:block">
        <div className="sticky top-20 rounded-xl border border-line bg-surface p-5">{panel}</div>
      </aside>
    </>
  );

  function FilterHeading({ children }: { children: React.ReactNode }) {
    return <p className="eyebrow mb-2.5">{children}</p>;
  }
}

function CheckList({
  options,
  selected,
  onToggle,
}: {
  options: { value: string; label: string }[];
  selected: string[];
  onToggle: (v: string) => void;
}) {
  return (
    <div className="max-h-52 space-y-0.5 overflow-y-auto pr-1">
      {options.map((o) => {
        const checked = selected.includes(o.value);
        const hue = STORE_HUES[o.value.toUpperCase().replace(/-/g, "_") as keyof typeof STORE_HUES];
        return (
          <label
            key={o.value}
            className="flex cursor-pointer items-center gap-2.5 rounded-md px-1.5 py-1.5 text-sm transition-colors hover:bg-paper-2"
          >
            <span
              className={cn(
                "grid size-4 shrink-0 place-items-center rounded border transition-colors",
                checked ? "border-nexus bg-nexus" : "border-line-strong bg-surface",
              )}
            >
              {checked && (
                <svg viewBox="0 0 16 16" className="size-3 text-white" fill="none">
                  <path d="M3 8l3.5 3.5L13 5" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              )}
            </span>
            <input type="checkbox" checked={checked} onChange={() => onToggle(o.value)} className="sr-only" />
            {hue && <span className="size-2 rounded-full" style={{ background: hue.accent }} />}
            <span className="truncate text-ink">{o.label}</span>
          </label>
        );
      })}
    </div>
  );
}
