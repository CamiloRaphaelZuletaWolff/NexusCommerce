"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { BarChart3, LayoutGrid, Package, Store, ArrowLeft } from "lucide-react";
import { NexusLogo } from "@/components/nexus-mark";
import { UserMenu } from "@/components/layout/user-menu";
import { cn } from "@/lib/utils";

const NAV = [
  { href: "/dashboard", label: "Overview", icon: LayoutGrid, exact: true },
  { href: "/dashboard/analytics", label: "Analytics", icon: BarChart3 },
  { href: "/dashboard/products", label: "Products", icon: Package },
  { href: "/dashboard/stores", label: "Stores", icon: Store },
];

export function DashboardShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isActive = (href: string, exact?: boolean) =>
    exact ? pathname === href : pathname.startsWith(href);

  return (
    <div className="min-h-dvh lg:grid lg:grid-cols-[240px_1fr]">
      {/* Sidebar (desktop) */}
      <aside className="sticky top-0 hidden h-dvh flex-col border-r border-line bg-surface p-4 lg:flex">
        <Link href="/" className="flex items-center gap-2 px-2 py-2 text-ink">
          <NexusLogo className="size-7 text-nexus" />
          <span className="font-display text-base font-extrabold">NexusCommerce</span>
        </Link>
        <p className="eyebrow mt-6 px-2">Manage</p>
        <nav className="mt-2 flex flex-col gap-0.5">
          {NAV.map((n) => (
            <Link
              key={n.href}
              href={n.href}
              className={cn(
                "flex items-center gap-2.5 rounded-md px-3 py-2 text-sm font-semibold transition-colors",
                isActive(n.href, n.exact)
                  ? "bg-nexus-tint text-nexus"
                  : "text-ink-soft hover:bg-paper-2 hover:text-ink",
              )}
            >
              <n.icon className="size-4" />
              {n.label}
            </Link>
          ))}
        </nav>
        <Link
          href="/"
          className="mt-auto flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium text-ink-faint transition-colors hover:bg-paper-2 hover:text-ink"
        >
          <ArrowLeft className="size-4" />
          Back to store
        </Link>
      </aside>

      <div className="flex min-w-0 flex-col">
        {/* Topbar */}
        <header className="sticky top-0 z-30 flex h-16 items-center gap-3 border-b border-line bg-paper/80 px-5 backdrop-blur-md">
          <Link href="/" className="flex items-center gap-2 text-ink lg:hidden">
            <NexusLogo className="size-6 text-nexus" />
            <span className="font-display font-extrabold">Dashboard</span>
          </Link>
          <span className="ml-auto" />
          <UserMenu />
        </header>

        {/* Mobile tab bar */}
        <nav className="flex gap-1 overflow-x-auto border-b border-line bg-surface px-3 py-2 lg:hidden">
          {NAV.map((n) => (
            <Link
              key={n.href}
              href={n.href}
              className={cn(
                "flex shrink-0 items-center gap-1.5 rounded-md px-3 py-1.5 text-sm font-semibold transition-colors",
                isActive(n.href, n.exact) ? "bg-nexus-tint text-nexus" : "text-ink-soft",
              )}
            >
              <n.icon className="size-4" />
              {n.label}
            </Link>
          ))}
        </nav>

        <main className="flex-1 p-5 sm:p-8">{children}</main>
      </div>
    </div>
  );
}
