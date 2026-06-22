import { Skeleton } from "@/components/ui/skeleton";

/** Product-detail streaming fallback — mirrors the two-column art + details layout. */
export default function ProductLoading() {
  return (
    <div className="container-page py-8">
      <Skeleton className="h-4 w-72 max-w-full rounded" />
      <div className="mt-6 grid gap-10 lg:grid-cols-2">
        <Skeleton className="aspect-square w-full rounded-2xl" />
        <div className="space-y-4">
          <Skeleton className="h-6 w-32 rounded" />
          <Skeleton className="h-10 w-3/4 rounded-lg" />
          <Skeleton className="h-8 w-28 rounded" />
          <Skeleton className="h-24 w-full rounded-lg" />
          <Skeleton className="h-12 w-full rounded-lg" />
        </div>
      </div>
    </div>
  );
}
