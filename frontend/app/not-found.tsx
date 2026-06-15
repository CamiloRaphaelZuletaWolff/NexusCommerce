import Link from "next/link";
import { NexusLogo } from "@/components/nexus-mark";

export default function NotFound() {
  return (
    <div className="grid min-h-dvh place-items-center bg-paper px-6 text-center">
      <div>
        <NexusLogo className="mx-auto size-12 text-nexus" />
        <p className="eyebrow mt-6">Error 404</p>
        <h1 className="mt-2 text-4xl font-extrabold">This page isn’t in the nexus</h1>
        <p className="mx-auto mt-3 max-w-md text-ink-soft">
          The page you’re looking for doesn’t exist or may have moved.
        </p>
        <div className="mt-7 flex justify-center gap-3">
          <Link
            href="/"
            className="inline-flex h-11 items-center rounded-md bg-ink px-5 text-sm font-semibold text-paper transition-colors hover:bg-ink/90"
          >
            Back to home
          </Link>
          <Link
            href="/catalog"
            className="inline-flex h-11 items-center rounded-md border border-line-strong bg-surface px-5 text-sm font-semibold transition-colors hover:bg-paper"
          >
            Browse catalog
          </Link>
        </div>
      </div>
    </div>
  );
}
