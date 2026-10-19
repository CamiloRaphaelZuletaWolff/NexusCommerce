import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

/** Merge conditional class names, resolving Tailwind conflicts. */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/** Format a money amount with its ISO currency (falls back to USD). */
export function formatMoney(
  amount: number | string | null | undefined,
  currency = "USD",
) {
  const value = typeof amount === "string" ? Number(amount) : amount ?? 0;
  try {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: currency || "USD",
      minimumFractionDigits: 2,
    }).format(value);
  } catch {
    return `$${value.toFixed(2)}`;
  }
}

/** Compact integer formatting (e.g. 1.2k). */
export function formatCount(n: number) {
  return new Intl.NumberFormat("en-US", { notation: "compact" }).format(n);
}

/** Human date, e.g. "Jun 15, 2026". */
export function formatDate(iso: string | null | undefined) {
  if (!iso) return "—";
  try {
    return new Intl.DateTimeFormat("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}

/** Stable 0..(buckets-1) hash for deterministic generated art. */
export function hashToIndex(input: string, buckets: number) {
  let h = 0;
  for (let i = 0; i < input.length; i++) {
    h = (h << 5) - h + input.charCodeAt(i);
    h |= 0;
  }
  return Math.abs(h) % buckets;
}

/** Title-case a slug/enum like FASHION or "home-decor". */
export function titleCase(s: string) {
  return s
    .replace(/[_-]+/g, " ")
    .toLowerCase()
    .replace(/\b\w/g, (c) => c.toUpperCase());
}

/**
 * Sanitize a post-login `?redirect=` target. Only same-origin, path-absolute URLs
 * are allowed ("/orders/1?x=y"); anything else ("https://evil.com", "//evil.com",
 * "/\evil.com", "javascript:...") falls back to `fallback`, so the login page cannot
 * be abused as an open redirect.
 */
export function safeRedirect(target: string | null | undefined, fallback = "/") {
  if (!target || !target.startsWith("/") || target.startsWith("//") || target.startsWith("/\\")) {
    return fallback;
  }
  try {
    const base = "http://same-origin.invalid";
    const url = new URL(target, base);
    if (url.origin !== base) return fallback;
    return `${url.pathname}${url.search}${url.hash}`;
  } catch {
    return fallback;
  }
}
