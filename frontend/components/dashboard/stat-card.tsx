import { cn } from "@/lib/utils";

export function StatCard({
  label,
  value,
  sub,
  icon: Icon,
  accent,
}: {
  label: string;
  value: string;
  sub?: string;
  icon: React.ComponentType<{ className?: string; style?: React.CSSProperties }>;
  accent?: string;
}) {
  return (
    <div className="rounded-2xl border border-line bg-surface p-5">
      <div className="flex items-center justify-between">
        <p className="eyebrow">{label}</p>
        <span
          className="grid size-9 place-items-center rounded-lg"
          style={{ background: accent ? `${accent}1a` : "var(--color-nexus-tint)" }}
        >
          <Icon className="size-5" style={{ color: accent ?? "var(--color-nexus)" }} />
        </span>
      </div>
      <p className="tabular mt-3 text-2xl font-extrabold text-ink">{value}</p>
      {sub && <p className="mt-1 text-xs text-ink-faint">{sub}</p>}
    </div>
  );
}

export function Panel({
  title,
  action,
  children,
  className,
}: {
  title: string;
  action?: React.ReactNode;
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <section className={cn("rounded-2xl border border-line bg-surface", className)}>
      <header className="flex items-center justify-between border-b border-line px-5 py-4">
        <h2 className="font-bold">{title}</h2>
        {action}
      </header>
      <div className="p-5">{children}</div>
    </section>
  );
}
