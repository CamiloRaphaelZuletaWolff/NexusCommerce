"use client";

import { create } from "zustand";

/** Ephemeral UI state only (the cart's data lives server-side via TanStack Query). */
interface UiState {
  cartOpen: boolean;
  openCart: () => void;
  closeCart: () => void;
  toggleCart: () => void;
  mobileNavOpen: boolean;
  setMobileNav: (open: boolean) => void;
}

export const useUiStore = create<UiState>((set) => ({
  cartOpen: false,
  openCart: () => set({ cartOpen: true }),
  closeCart: () => set({ cartOpen: false }),
  toggleCart: () => set((s) => ({ cartOpen: !s.cartOpen })),
  mobileNavOpen: false,
  setMobileNav: (open) => set({ mobileNavOpen: open }),
}));
