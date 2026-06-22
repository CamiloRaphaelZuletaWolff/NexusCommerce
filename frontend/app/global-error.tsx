"use client";

import "./globals.css";

/**
 * Root error boundary — only renders if the root layout itself throws, so it must
 * provide its own <html>/<body>. Kept deliberately minimal and dependency-free.
 */
export default function GlobalError({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <html lang="en">
      <body className="antialiased">
        <div className="grid min-h-dvh place-items-center bg-paper px-6 text-center">
          <div>
            <p className="eyebrow text-danger">Something broke</p>
            <h1 className="mt-2 text-4xl font-extrabold">The nexus hit an error</h1>
            <p className="mx-auto mt-3 max-w-md text-ink-soft">
              An unexpected error stopped this page from loading. You can try again.
            </p>
            <button
              type="button"
              onClick={() => reset()}
              className="mt-7 inline-flex h-11 items-center rounded-md bg-ink px-5 text-sm font-semibold text-paper transition-colors hover:bg-ink/90"
            >
              Try again
            </button>
          </div>
        </div>
      </body>
    </html>
  );
}
