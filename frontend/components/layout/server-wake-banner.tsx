"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { AnimatePresence, motion } from "framer-motion";
import { Loader2 } from "lucide-react";
import { API_BASE } from "@/lib/api";

/** Only surface the banner if the backend hasn't answered within this window. */
const SHOW_AFTER_MS = 2_000;
const ATTEMPT_TIMEOUT_MS = 20_000;
const RETRY_DELAY_MS = 3_000;
/** Give up (and hide) after this long rather than claim we're "waking" forever. */
const MAX_WAIT_MS = 5 * 60_000;

/**
 * Health probe URL for the configured API base.
 * - Relative base (production, "/api"): the proxied `/api/actuator/health` (see next.config.ts).
 * - Absolute base (local dev, "http://localhost:8080/api"): the backend's own `/actuator/health`.
 */
export function healthUrl(base: string = API_BASE): string {
  if (/^https?:\/\//i.test(base)) return `${new URL(base).origin}/actuator/health`;
  return `${base.replace(/\/+$/, "")}/actuator/health`;
}

/**
 * The demo backend runs on Render's free plan, which sleeps after 15 min idle and
 * takes 2–3 min to boot (JVM + Flyway on 0.1 vCPU). Ping the health probe once per page load; if it is slow,
 * show a small non-blocking notice, and once healthy refetch everything that may
 * have rendered empty while the server was asleep.
 */
export function ServerWakeBanner() {
  const [waking, setWaking] = useState(false);
  const queryClient = useQueryClient();
  const router = useRouter();

  useEffect(() => {
    let cancelled = false;
    let shown = false;
    const url = healthUrl();
    const deadline = Date.now() + MAX_WAIT_MS;
    const showTimer = setTimeout(() => {
      shown = true;
      setWaking(true);
    }, SHOW_AFTER_MS);

    async function isHealthy() {
      const controller = new AbortController();
      const abort = setTimeout(() => controller.abort(), ATTEMPT_TIMEOUT_MS);
      try {
        const res = await fetch(url, { cache: "no-store", signal: controller.signal });
        return res.ok;
      } catch {
        return false;
      } finally {
        clearTimeout(abort);
      }
    }

    (async () => {
      let healthy = await isHealthy();
      while (!healthy && !cancelled && Date.now() < deadline) {
        await new Promise((r) => setTimeout(r, RETRY_DELAY_MS));
        if (!cancelled) healthy = await isHealthy();
      }
      if (cancelled) return;
      clearTimeout(showTimer);
      setWaking(false);
      if (healthy && shown) {
        void queryClient.invalidateQueries();
        router.refresh();
      }
    })();

    return () => {
      cancelled = true;
      clearTimeout(showTimer);
    };
  }, [queryClient, router]);

  return (
    <AnimatePresence>
      {waking && (
        <motion.div
          role="status"
          aria-live="polite"
          initial={{ opacity: 0, y: -12 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -12, transition: { duration: 0.15 } }}
          transition={{ type: "spring", stiffness: 380, damping: 30 }}
          className="pointer-events-none fixed inset-x-0 top-3 z-[100] flex justify-center px-4"
        >
          <div className="pointer-events-auto flex items-center gap-2.5 rounded-full border border-line bg-surface/95 py-2 pl-2.5 pr-4 text-sm shadow-md backdrop-blur">
            <span className="grid size-6 shrink-0 place-items-center rounded-full bg-nexus-tint text-nexus">
              <Loader2 className="size-3.5 animate-spin" strokeWidth={2.5} />
            </span>
            <span className="text-ink-soft">
              Waking up the server (free hosting tier, this can take 2–3 minutes)…
            </span>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
