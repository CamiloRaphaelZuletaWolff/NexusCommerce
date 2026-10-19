import type { NextConfig } from "next";

/**
 * Same-origin API proxy for production.
 *
 * The frontend (Vercel, *.vercel.app) and the backend (Render, *.onrender.com)
 * are different *sites*, so a refresh cookie set directly by the backend would be
 * a third-party cookie — blocked by Safari, Brave and most incognito modes. When
 * BACKEND_ORIGIN is set (server-only, e.g. https://nexuscommerce-backend.onrender.com),
 * the browser talks to `/api/*` on the frontend's own origin and Next proxies it,
 * so the `nexus_refresh` cookie is first-party and SameSite=Lax just works.
 *
 * Unset (local dev): no rewrites; the browser calls NEXT_PUBLIC_API_BASE_URL
 * (http://localhost:8080/api) directly, exactly as before.
 */
const backendOrigin = process.env.BACKEND_ORIGIN?.replace(/\/+$/, "");

const isDev = process.env.NODE_ENV !== "production";

/** Origin the BROWSER calls for the API: none extra in proxy mode ("/api" is same-origin). */
function browserApiOrigin(): string | null {
  const base = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api";
  try {
    const url = new URL(base); // absolute URL -> its origin
    return url.protocol === "http:" || url.protocol === "https:" ? url.origin : null;
  } catch {
    return null; // relative ("/api") -> covered by 'self'
  }
}

/**
 * Content-Security-Policy. Next.js injects inline bootstrap scripts, so without a
 * per-request nonce (which needs middleware and forces dynamic rendering) script-src
 * must allow 'unsafe-inline'; the policy still blocks every third-party script, frame,
 * plugin and form target, and pins API calls to this origin (+ the API origin).
 * The app loads no external assets (next/font self-hosts the Google fonts at build).
 */
function contentSecurityPolicy(): string {
  const connect = ["'self'", browserApiOrigin(), isDev ? "ws:" : null].filter(Boolean).join(" ");
  return [
    "default-src 'self'",
    `script-src 'self' 'unsafe-inline'${isDev ? " 'unsafe-eval'" : ""}`,
    "style-src 'self' 'unsafe-inline'",
    "img-src 'self' data: blob:",
    "font-src 'self' data:",
    `connect-src ${connect}`,
    "object-src 'none'",
    "base-uri 'self'",
    "form-action 'self'",
    "frame-ancestors 'none'",
  ].join("; ");
}

const securityHeaders = [
  { key: "Content-Security-Policy", value: contentSecurityPolicy() },
  { key: "X-Content-Type-Options", value: "nosniff" },
  { key: "X-Frame-Options", value: "DENY" }, // legacy twin of frame-ancestors 'none'
  { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
  { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=(), payment=()" },
  // Ignored by browsers over plain http (local dev); Vercel serves HTTPS only.
  { key: "Strict-Transport-Security", value: "max-age=63072000; includeSubDomains" },
];

const nextConfig: NextConfig = {
  poweredByHeader: false,
  async headers() {
    return [{ source: "/:path*", headers: securityHeaders }];
  },
  async rewrites() {
    if (!backendOrigin) return [];
    return [
      // Actuator lives outside /api on the backend; expose only the health probe
      // (used by the cold-start banner). Must precede the catch-all below.
      { source: "/api/actuator/health", destination: `${backendOrigin}/actuator/health` },
      { source: "/api/:path*", destination: `${backendOrigin}/api/:path*` },
    ];
  },
};

export default nextConfig;
