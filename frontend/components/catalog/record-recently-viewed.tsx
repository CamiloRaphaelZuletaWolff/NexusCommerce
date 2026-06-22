"use client";

import { useEffect, useRef } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/components/auth-provider";
import { api } from "@/lib/api";

/**
 * Fire-and-forget: records a product view to the customer's MongoDB profile when a
 * signed-in user opens a product. Renders nothing; ignored for guests. This is the
 * live write into the cross-database bridge that populates "Recently viewed".
 */
export function RecordRecentlyViewed({ productId }: { productId: string }) {
  const { status } = useAuth();
  const queryClient = useQueryClient();
  const recorded = useRef<string | null>(null);

  useEffect(() => {
    if (status !== "authenticated" || recorded.current === productId) return;
    recorded.current = productId;
    api.catalog
      .recordRecentlyViewed(productId)
      .then((updated) => queryClient.setQueryData(["myProfile"], updated))
      .catch(() => {
        recorded.current = null; // allow a later visit to retry
      });
  }, [status, productId, queryClient]);

  return null;
}
