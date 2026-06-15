import Link from "next/link";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { cn } from "@/lib/utils";

type Params = Record<string, string | string[] | undefined>;

/** Server-rendered pager; `params` are the current filters minus `page`. */
export function Pagination({
  page,
  totalPages,
  params,
}: {
  page: number;
  totalPages: number;
  params: Params;
}) {
  if (totalPages <= 1) return null;

  const href = (p: number) => {
    const sp = new URLSearchParams();
    for (const [k, v] of Object.entries(params)) {
      if (!v) continue;
      if (Array.isArray(v)) v.forEach((x) => sp.append(k, x));
      else sp.set(k, v);
    }
    if (p > 0) sp.set("page", String(p));
    return `/catalog${sp.toString() ? `?${sp}` : ""}`;
  };

  return (
    <nav className="mt-10 flex items-center justify-center gap-3" aria-label="Pagination">
      <PagerLink href={href(page - 1)} disabled={page <= 0} aria-label="Previous page">
        <ChevronLeft className="size-4" />
        Prev
      </PagerLink>
      <span className="tabular text-sm text-ink-soft">
        Page {page + 1} of {totalPages}
      </span>
      <PagerLink href={href(page + 1)} disabled={page >= totalPages - 1} aria-label="Next page">
        Next
        <ChevronRight className="size-4" />
      </PagerLink>
    </nav>
  );
}

function PagerLink({
  href,
  disabled,
  children,
  ...rest
}: {
  href: string;
  disabled?: boolean;
  children: React.ReactNode;
} & React.AriaAttributes) {
  const cls =
    "inline-flex h-10 items-center gap-1 rounded-md border border-line-strong bg-surface px-4 text-sm font-semibold transition-colors";
  if (disabled) {
    return (
      <span className={cn(cls, "pointer-events-none opacity-40")} aria-disabled {...rest}>
        {children}
      </span>
    );
  }
  return (
    <Link href={href} className={cn(cls, "hover:border-ink/40 hover:bg-paper")} {...rest}>
      {children}
    </Link>
  );
}
