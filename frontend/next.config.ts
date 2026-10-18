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

const nextConfig: NextConfig = {
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
