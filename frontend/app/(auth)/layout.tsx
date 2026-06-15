import Link from "next/link";
import { NexusMark, NexusLogo } from "@/components/nexus-mark";

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="grid min-h-dvh lg:grid-cols-2">
      {/* Brand panel */}
      <aside className="relative hidden flex-col justify-between overflow-hidden bg-nexus-deep p-12 text-paper lg:flex">
        <div className="nexus-grid absolute inset-0 opacity-30" aria-hidden />
        <Link href="/" className="relative flex items-center gap-2 text-white">
          <NexusLogo className="size-7" />
          <span className="font-display text-lg font-extrabold">NexusCommerce</span>
        </Link>
        <div className="relative mx-auto w-full max-w-sm">
          <NexusMark className="h-auto w-full opacity-95" />
        </div>
        <div className="relative">
          <p className="font-display text-2xl font-bold leading-tight text-white">
            Five store-worlds.<br />One cart.
          </p>
          <p className="mt-2 max-w-xs text-sm text-paper/60">
            Sign in to shop across every store and track your orders in one place.
          </p>
        </div>
      </aside>

      {/* Form panel */}
      <main className="flex flex-col">
        <div className="p-6 lg:hidden">
          <Link href="/" className="flex items-center gap-2 text-ink">
            <NexusLogo className="size-7 text-nexus" />
            <span className="font-display text-lg font-extrabold">NexusCommerce</span>
          </Link>
        </div>
        <div className="flex flex-1 items-center justify-center px-6 pb-12">
          <div className="w-full max-w-sm">{children}</div>
        </div>
      </main>
    </div>
  );
}
