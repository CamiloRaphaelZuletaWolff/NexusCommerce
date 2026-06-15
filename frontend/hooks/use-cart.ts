"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/components/auth-provider";
import { useUiStore } from "@/stores/ui";
import { toast } from "@/stores/toast";
import type { CartResponse } from "@/types/api";

const CART_KEY = ["cart"] as const;

/** The current user's cart (only fetched when authenticated). */
export function useCart() {
  const { status } = useAuth();
  return useQuery({
    queryKey: CART_KEY,
    queryFn: api.cart.get,
    enabled: status === "authenticated",
  });
}

export function useAddToCart() {
  const queryClient = useQueryClient();
  const { status } = useAuth();
  const openCart = useUiStore((s) => s.openCart);

  return useMutation({
    mutationFn: ({ productId, quantity = 1 }: { productId: string; quantity?: number }) => {
      if (status !== "authenticated") {
        return Promise.reject(new ApiError(401, "Sign in to add items to your cart"));
      }
      return api.cart.addItem({ productId, quantity });
    },
    onSuccess: (cart: CartResponse) => {
      queryClient.setQueryData(CART_KEY, cart);
      openCart();
      toast.success("Added to cart", `${cart.itemCount} item${cart.itemCount === 1 ? "" : "s"} in your cart`);
    },
    onError: (err) => {
      const msg = err instanceof ApiError ? err.message : "Something went wrong";
      toast.error("Couldn't add to cart", msg);
    },
  });
}

export function useUpdateCartItem() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ itemId, quantity }: { itemId: string; quantity: number }) =>
      api.cart.updateItem(itemId, quantity),
    onSuccess: (cart) => queryClient.setQueryData(CART_KEY, cart),
    onError: () => toast.error("Couldn't update item"),
  });
}

export function useRemoveCartItem() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (itemId: string) => api.cart.removeItem(itemId),
    onSuccess: (cart) => queryClient.setQueryData(CART_KEY, cart),
    onError: () => toast.error("Couldn't remove item"),
  });
}

export function useClearCart() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.cart.clear(),
    onSuccess: (cart) => queryClient.setQueryData(CART_KEY, cart),
  });
}
