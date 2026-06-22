import { Skeleton } from "@/components/ui/skeleton";

/** Catalog streaming fallback — mirrors the filters sidebar + product grid. */
export default function CatalogLoading() {
  return (
    <div className="container-page py-8">
      <Skeleton className="h-8 w-48 rounded-lg" />
      <div className="mt-6 grid gap-8 lg:grid-cols-[16rem_1fr]">
        <div className="hidden space-y-3 lg:block">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-10 w-full rounded-lg" />
          ))}
        </div>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
          {Array.from({ length: 9 }).map((_, i) => (
            <Skeleton key={i} className="aspect-[3/4] w-full rounded-xl" />
          ))}
        </div>
      </div>
    </div>
  );
}
