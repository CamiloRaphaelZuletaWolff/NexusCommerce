"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { api, ApiError } from "@/lib/api";
import { cn } from "@/lib/utils";
import { toast } from "@/stores/toast";
import type { CustomerProfileResponse } from "@/types/api";

/**
 * Edits the customer's favorite categories and brands and writes them back to the
 * MongoDB profile via PATCH — the write half of the cross-database bridge. Toggling
 * is local; "Save preferences" persists the full sets.
 */
export function FavoritesEditor({ profile }: { profile: CustomerProfileResponse }) {
  const queryClient = useQueryClient();
  const { data: categories } = useQuery({ queryKey: ["categories"], queryFn: api.catalog.categories });
  const { data: brands } = useQuery({ queryKey: ["brands"], queryFn: api.catalog.brands });

  const [cats, setCats] = useState<string[]>(profile.favoriteCategories);
  const [brs, setBrs] = useState<string[]>(profile.favoriteBrands);

  const dirty = useMemo(
    () => !sameSet(cats, profile.favoriteCategories) || !sameSet(brs, profile.favoriteBrands),
    [cats, brs, profile.favoriteCategories, profile.favoriteBrands],
  );

  const save = useMutation({
    mutationFn: () => api.catalog.updateProfile({ favoriteCategories: cats, favoriteBrands: brs }),
    onSuccess: (updated) => {
      queryClient.setQueryData(["myProfile"], updated);
      toast.success("Preferences saved", "Written to your MongoDB profile.");
    },
    onError: (e) => toast.error("Couldn’t save", e instanceof ApiError ? e.message : "Try again."),
  });

  return (
    <section className="rounded-2xl border border-line bg-surface p-6 sm:p-8">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="eyebrow">Preferences</p>
          <h2 className="mt-1 text-xl font-extrabold">Favorite categories &amp; brands</h2>
          <p className="mt-1 max-w-prose text-sm text-ink-soft">
            Pick what you care about. Saving writes the selection straight to MongoDB.
          </p>
        </div>
        <Button
          variant="accent"
          loading={save.isPending}
          disabled={!dirty || save.isPending}
          onClick={() => save.mutate()}
        >
          {dirty ? "Save preferences" : "Saved"}
        </Button>
      </div>

      <div className="mt-6 space-y-6">
        <ChipGroup
          label="Categories"
          options={(categories ?? []).map((c) => ({ value: c.slug, label: c.name }))}
          selected={cats}
          onToggle={(v) => setCats((s) => toggle(s, v))}
        />
        <ChipGroup
          label="Brands"
          options={(brands ?? []).map((b) => ({ value: b.name, label: b.name }))}
          selected={brs}
          onToggle={(v) => setBrs((s) => toggle(s, v))}
        />
      </div>
    </section>
  );
}

function ChipGroup({
  label,
  options,
  selected,
  onToggle,
}: {
  label: string;
  options: { value: string; label: string }[];
  selected: string[];
  onToggle: (value: string) => void;
}) {
  return (
    <div>
      <h3 className="eyebrow mb-2.5">{label}</h3>
      {options.length === 0 ? (
        <p className="text-sm text-ink-faint">None available.</p>
      ) : (
        <div className="flex flex-wrap gap-2">
          {options.map((o) => {
            const on = selected.includes(o.value);
            return (
              <button
                key={o.value}
                type="button"
                aria-pressed={on}
                onClick={() => onToggle(o.value)}
                className={cn(
                  "inline-flex items-center gap-1.5 rounded-full border px-3 py-1.5 text-sm font-medium transition-colors",
                  on
                    ? "border-nexus bg-nexus text-white"
                    : "border-line-strong text-ink-soft hover:border-ink/30 hover:text-ink",
                )}
              >
                {on && <Check className="size-3.5" />}
                {o.label}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}

function toggle(set: string[], value: string): string[] {
  return set.includes(value) ? set.filter((v) => v !== value) : [...set, value];
}

function sameSet(a: string[], b: string[]): boolean {
  if (a.length !== b.length) return false;
  const setB = new Set(b);
  return a.every((v) => setB.has(v));
}
