import { Skeleton } from "@/components/ui/skeleton";

/** Streaming fallback for storefront routes without a more specific one. */
export default function Loading() {
  return (
    <div className="container-page py-10">
      <Skeleton className="h-9 w-64 rounded-lg" />
      <Skeleton className="mt-3 h-5 w-96 max-w-full rounded" />
      <div className="mt-8 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
        {Array.from({ length: 8 }).map((_, i) => (
          <Skeleton key={i} className="aspect-[3/4] w-full rounded-xl" />
        ))}
      </div>
    </div>
  );
}
