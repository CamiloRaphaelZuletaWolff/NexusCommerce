import { describe, it, expect } from "vitest";
import { cn, formatMoney, formatDate, hashToIndex, titleCase } from "@/lib/utils";

describe("formatMoney", () => {
  it("formats a USD amount", () => {
    expect(formatMoney(1299)).toBe("$1,299.00");
  });
  it("accepts string amounts", () => {
    expect(formatMoney("99.9")).toBe("$99.90");
  });
  it("falls back to zero for nullish input", () => {
    expect(formatMoney(null)).toBe("$0.00");
  });
});

describe("hashToIndex", () => {
  it("is deterministic and within range", () => {
    const first = hashToIndex("product-123", 5);
    expect(first).toBe(hashToIndex("product-123", 5));
    expect(first).toBeGreaterThanOrEqual(0);
    expect(first).toBeLessThan(5);
  });
  it("varies across different seeds", () => {
    expect(hashToIndex("a", 1000)).not.toBe(hashToIndex("b", 1000));
  });
});

describe("titleCase", () => {
  it("title-cases enum values and slugs", () => {
    expect(titleCase("HOME_DECOR")).toBe("Home Decor");
    expect(titleCase("home-decor")).toBe("Home Decor");
  });
});

describe("formatDate", () => {
  it("returns an em dash for nullish input", () => {
    expect(formatDate(null)).toBe("—");
  });
  it("formats an ISO date", () => {
    expect(formatDate("2026-06-21T12:00:00Z")).toMatch(/2026/);
  });
});

describe("cn", () => {
  it("resolves conflicting tailwind classes (last wins)", () => {
    expect(cn("p-2", "p-4")).toBe("p-4");
  });
});
