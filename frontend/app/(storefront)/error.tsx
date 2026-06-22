"use client";

import { useEffect } from "react";
import { ErrorState } from "@/components/ui/error-state";

/** Catches render/data errors within the storefront; the navbar and footer stay put. */
export default function StorefrontError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="container-page py-16">
      <ErrorState
        title="This page didn’t load"
        description="Something went wrong reaching the store. Try again, or head back to browsing."
        onRetry={() => reset()}
      />
    </div>
  );
}
