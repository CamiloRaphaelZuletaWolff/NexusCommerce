import Link from "next/link";
import { NexusLogo } from "@/components/nexus-mark";
import { STORE_HUES, STORE_TYPES } from "@/lib/store-theme";

export function Footer() {
  return (
    <footer className="mt-24 border-t border-line bg-surface">
      <div className="container-page grid gap-10 py-14 md:grid-cols-[1.4fr_1fr_1fr]">
        <div>
          <Link href="/" className="flex items-center gap-2 text-ink">
            <NexusLogo className="size-7 text-nexus" />
            <span className="font-display text-lg font-extrabold">
              Nexus<span className="text-nexus">Commerce</span>
            </span>
          </Link>
          <p className="mt-3 max-w-sm text-sm text-ink-soft">
            Five store-worlds, one system. A multi-store marketplace built on a hybrid
            PostgreSQL + MongoDB architecture.
          </p>
        </div>

        <div>
          <p className="eyebrow mb-3">Store-worlds</p>
          <ul className="space-y-2">
            {STORE_TYPES.map((t) => (
              <li key={t}>
                <Link
                  href={`/catalog?type=${t}`}
                  className="group flex items-center gap-2 text-sm text-ink-soft transition-colors hover:text-ink"
                >
                  <span
                    className="size-2 rounded-full"
                    style={{ background: STORE_HUES[t].accent }}
                  />
                  {STORE_HUES[t].label}
                </Link>
              </li>
            ))}
          </ul>
        </div>

        <div>
          <p className="eyebrow mb-3">Platform</p>
          <ul className="space-y-2 text-sm text-ink-soft">
            <li><Link href="/stores" className="transition-colors hover:text-ink">Browse stores</Link></li>
            <li><Link href="/catalog" className="transition-colors hover:text-ink">Full catalog</Link></li>
            <li><Link href="/orders" className="transition-colors hover:text-ink">Order history</Link></li>
            <li><Link href="/dashboard" className="transition-colors hover:text-ink">Dashboard</Link></li>
          </ul>
        </div>
      </div>
      <div className="border-t border-line">
        <div className="container-page flex flex-col items-center justify-between gap-2 py-5 text-xs text-ink-faint sm:flex-row">
          <p>© {2026} NexusCommerce — portfolio engineering artifact.</p>
          <p className="tabular">Spring Boot · Next.js · PostgreSQL · MongoDB</p>
        </div>
      </div>
    </footer>
  );
}
