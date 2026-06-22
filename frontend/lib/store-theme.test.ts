import { describe, it, expect } from "vitest";
import { accentVars, hueFor, STORE_HUES, STORE_TYPES } from "@/lib/store-theme";

describe("hueFor", () => {
  it("returns the matching hue for a known store type", () => {
    expect(hueFor("ELECTRONICS")).toBe(STORE_HUES.ELECTRONICS);
  });
  it("falls back to the nexus hue for null or unknown types", () => {
    expect(hueFor(null).label).toBe("Nexus");
    expect(hueFor("NOT_A_TYPE").label).toBe("Nexus");
  });
});

describe("accentVars", () => {
  it("exposes the accent CSS custom properties for a store type", () => {
    const vars = accentVars("FASHION") as Record<string, string>;
    expect(vars["--accent"]).toBe(STORE_HUES.FASHION.accent);
    expect(vars["--accent-soft"]).toBe(STORE_HUES.FASHION.soft);
    expect(vars["--accent-ink"]).toBe(STORE_HUES.FASHION.ink);
  });
});

describe("STORE_TYPES", () => {
  it("covers all five store-worlds and each has a hue", () => {
    expect(STORE_TYPES).toHaveLength(5);
    STORE_TYPES.forEach((type) => expect(STORE_HUES[type]).toBeDefined());
  });
});
