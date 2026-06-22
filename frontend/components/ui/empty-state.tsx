import type { ComponentType, ReactNode } from "react";
import { cn } from "@/lib/utils";

/**
 * A neutral "nothing here yet" panel — an invitation to act, not an apology.
 * Use it where a successful response simply has no data.
 */
export function EmptyState({
  icon: Icon,
  title,
  description,
  action,
  className,
}: {
  icon: ComponentType<{ className?: string }>;
  title: string;
  description?: string;
  action?: ReactNode;
  className?: string;
}) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center rounded-2xl border border-dashed border-line-strong bg-surface py-16 text-center",
        className,
      )}
    >
      <div className="grid size-16 place-items-center rounded-full bg-nexus-tint">
        <Icon className="size-7 text-nexus" />
      </div>
      <h2 className="mt-5 text-xl font-bold">{title}</h2>
      {description && <p className="mt-1 max-w-sm text-ink-soft">{description}</p>}
      {action && <div className="mt-6">{action}</div>}
    </div>
  );
}
