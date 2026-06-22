"use client";

import { useEffect } from "react";
import { ErrorState } from "@/components/ui/error-state";

/** Catches render/data errors within the dashboard, keeping the dashboard shell. */
export default function DashboardError({
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
    <div className="py-10">
      <ErrorState
        title="This view didn’t load"
        description="We couldn’t load the dashboard data. Try again in a moment."
        onRetry={() => reset()}
      />
    </div>
  );
}
