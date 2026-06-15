"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Menu, Search, ShoppingBag, X } from "lucide-react";
import { NexusLogo } from "@/components/nexus-mark";
import { UserMenu } from "@/components/layout/user-menu";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/components/auth-provider";
import { useUiStore } from "@/stores/ui";
import { useCart } from "@/hooks/use-cart";
import { cn } from "@/lib/utils";

const LINKS = [
  { href: "/stores", label: "Stores" },
  { href: "/catalog", label: "Catalog" },
];

export function Navbar() {
  const pathname = usePathname();
  const router = useRouter();
  const { status, hasRole } = useAuth();
  const { openCart, mobileNavOpen, setMobileNav } = useUiStore();
  const { data: cart } = useCart();
  const [query, setQuery] = useState("");

  const count = cart?.itemCount ?? 0;

  const onSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const q = query.trim();
    router.push(q ? `/catalog?q=${encodeURIComponent(q)}` : "/catalog");
    setMobileNav(false);
  };

  return (
    <header className="sticky top-0 z-40 border-b border-line bg-paper/80 backdrop-blur-md">
      <div className="container-page flex h-16 items-center gap-4">
        <Link href="/" className="flex shrink-0 items-center gap-2 text-ink">
          <NexusLogo className="size-7 text-nexus" />
          <span className="font-display text-lg font-extrabold tracking-tight">
            Nexus<span className="text-nexus">Commerce</span>
          </span>
        </Link>

        <nav className="ml-2 hidden items-center gap-1 md:flex">
          {LINKS.map((l) => (
            <Link
              key={l.href}
              href={l.href}
              className={cn(
                "rounded-md px-3 py-2 text-sm font-semibold transition-colors",
                pathname.startsWith(l.href)
                  ? "text-nexus"
                  : "text-ink-soft hover:text-ink",
              )}
            >
              {l.label}
            </Link>
          ))}
        </nav>

        <form onSubmit={onSearch} className="ml-auto hidden max-w-xs flex-1 sm:block">
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-ink-faint" />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search products…"
              aria-label="Search products"
              className="h-10 w-full rounded-full border border-line-strong bg-surface pl-9 pr-4 text-sm outline-none transition-colors focus:border-nexus focus:ring-4 focus:ring-nexus/12"
            />
          </div>
        </form>

        <div className="ml-auto flex items-center gap-1.5 sm:ml-0">
          <button
            onClick={openCart}
            aria-label={`Open cart, ${count} items`}
            className="relative grid size-10 place-items-center rounded-full text-ink transition-colors hover:bg-paper-2"
          >
            <ShoppingBag className="size-5" />
            {count > 0 && (
              <span className="tabular absolute -right-0.5 -top-0.5 grid min-w-5 place-items-center rounded-full bg-nexus px-1 text-[10px] font-bold text-white">
                {count}
              </span>
            )}
          </button>

          {status === "authenticated" ? (
            <UserMenu />
          ) : status === "unauthenticated" ? (
            <div className="hidden items-center gap-1.5 sm:flex">
              <Button asChild variant="ghost" size="sm">
                <Link href="/login">Sign in</Link>
              </Button>
              <Button asChild size="sm">
                <Link href="/register">Sign up</Link>
              </Button>
            </div>
          ) : (
            <div className="size-10" />
          )}

          <button
            onClick={() => setMobileNav(!mobileNavOpen)}
            aria-label="Toggle menu"
            className="grid size-10 place-items-center rounded-full text-ink transition-colors hover:bg-paper-2 md:hidden"
          >
            {mobileNavOpen ? <X className="size-5" /> : <Menu className="size-5" />}
          </button>
        </div>
      </div>

      {mobileNavOpen && (
        <div className="border-t border-line bg-paper px-4 py-3 md:hidden">
          <form onSubmit={onSearch} className="mb-3 sm:hidden">
            <div className="relative">
              <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-ink-faint" />
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search products…"
                className="h-10 w-full rounded-full border border-line-strong bg-surface pl-9 pr-4 text-sm outline-none focus:border-nexus"
              />
            </div>
          </form>
          <nav className="flex flex-col">
            {LINKS.map((l) => (
              <Link
                key={l.href}
                href={l.href}
                onClick={() => setMobileNav(false)}
                className="rounded-md px-3 py-2.5 text-sm font-semibold text-ink hover:bg-paper-2"
              >
                {l.label}
              </Link>
            ))}
            {hasRole("STORE_MANAGER", "ADMIN") && (
              <Link
                href="/dashboard"
                onClick={() => setMobileNav(false)}
                className="rounded-md px-3 py-2.5 text-sm font-semibold text-ink hover:bg-paper-2"
              >
                Dashboard
              </Link>
            )}
            {status === "unauthenticated" && (
              <div className="mt-2 flex gap-2">
                <Button asChild variant="outline" className="flex-1" onClick={() => setMobileNav(false)}>
                  <Link href="/login">Sign in</Link>
                </Button>
                <Button asChild className="flex-1" onClick={() => setMobileNav(false)}>
                  <Link href="/register">Sign up</Link>
                </Button>
              </div>
            )}
          </nav>
        </div>
      )}
    </header>
  );
}
