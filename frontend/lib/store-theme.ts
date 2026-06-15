/**
 * Each store-world carries its own hue — the platform shell stays neutral so the
 * store identity leads. These map the backend `StoreType` enum to a hue, a soft
 * tint, and a deep ink, applied at runtime via the `--accent*` CSS variables.
 */
import type { CSSProperties } from "react";

export type StoreType =
  | "ELECTRONICS"
  | "FASHION"
  | "FURNITURE"
  | "KITCHENWARE"
  | "HOME_DECOR";

export interface StoreHue {
  accent: string;
  soft: string;
  ink: string;
  label: string;
}

export const STORE_HUES: Record<StoreType, StoreHue> = {
  ELECTRONICS: { accent: "#1e7fff", soft: "#e4f0ff", ink: "#0b3a78", label: "Electronics" },
  FASHION: { accent: "#e14b86", soft: "#fde7f0", ink: "#7a1f47", label: "Fashion" },
  FURNITURE: { accent: "#b07636", soft: "#f6ecdf", ink: "#5e3b15", label: "Furniture" },
  KITCHENWARE: { accent: "#2fa56b", soft: "#e2f5ec", ink: "#11502f", label: "Kitchenware" },
  HOME_DECOR: { accent: "#c75a3f", soft: "#fbe9e3", ink: "#6e2a18", label: "Home Decor" },
};

const NEXUS_HUE: StoreHue = {
  accent: "#3f31d6",
  soft: "#ece9fd",
  ink: "#1d1452",
  label: "Nexus",
};

export function hueFor(type: string | null | undefined): StoreHue {
  if (!type) return NEXUS_HUE;
  return STORE_HUES[type as StoreType] ?? NEXUS_HUE;
}

/** Inline style that sets `--accent`, `--accent-soft`, `--accent-ink` for a subtree. */
export function accentVars(type: string | null | undefined): CSSProperties {
  const h = hueFor(type);
  return {
    ["--accent" as string]: h.accent,
    ["--accent-soft" as string]: h.soft,
    ["--accent-ink" as string]: h.ink,
  };
}

export const STORE_TYPES: StoreType[] = [
  "ELECTRONICS",
  "FASHION",
  "FURNITURE",
  "KITCHENWARE",
  "HOME_DECOR",
];
