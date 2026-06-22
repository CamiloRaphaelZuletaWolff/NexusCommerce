"use client";

import { AlertTriangle, RotateCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

/**
 * A failure panel that says what happened and offers the next action — used when a
 * request errors (server down, network drop). Distinct from EmptyState, which is for
 * a successful-but-empty response.
 */
export function ErrorState({
  title = "Couldn’t load this",
  description = "We couldn’t reach the server. Check your connection and try again.",
  onRetry,
  className,
}: {
  title?: string;
  description?: string;
  onRetry?: () => void;
  className?: string;
}) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center rounded-2xl border border-line bg-surface py-16 text-center",
        className,
      )}
    >
      <div className="grid size-16 place-items-center rounded-full bg-danger/10">
        <AlertTriangle className="size-7 text-danger" />
      </div>
      <h2 className="mt-5 text-xl font-bold">{title}</h2>
      <p className="mt-1 max-w-sm text-ink-soft">{description}</p>
      {onRetry && (
        <Button variant="outline" className="mt-6" onClick={onRetry}>
          <RotateCw className="size-4" />
          Try again
        </Button>
      )}
    </div>
  );
}
