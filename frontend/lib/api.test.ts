import { describe, it, expect, vi, beforeEach } from "vitest";
import { api } from "@/lib/api";
import { getAccessToken, setAccessToken } from "@/lib/token-store";

describe("api request: silent refresh on 401", () => {
  beforeEach(() => {
    setAccessToken("expired-token");
    vi.unstubAllGlobals();
  });

  it("refreshes exactly once on a 401, then retries and returns the data", async () => {
    const calls: string[] = [];
    const fetchMock = vi.fn(async (url: string) => {
      calls.push(url);
      const orderCalls = calls.filter((c) => c.includes("/orders")).length;
      if (url.includes("/orders") && orderCalls === 1) {
        return new Response("unauthorized", { status: 401 });
      }
      if (url.includes("/auth/refresh")) {
        return new Response(
          JSON.stringify({ accessToken: "fresh-token", tokenType: "Bearer", expiresInSeconds: 900, user: {} }),
          { status: 200 },
        );
      }
      return new Response(JSON.stringify([{ id: "o1" }]), { status: 200 });
    });
    vi.stubGlobal("fetch", fetchMock);

    const orders = await api.orders.list();

    expect(orders).toEqual([{ id: "o1" }]);
    expect(calls.filter((c) => c.includes("/auth/refresh"))).toHaveLength(1);
    expect(getAccessToken()).toBe("fresh-token");
  });

  it("encodes array query params as repeated keys", async () => {
    const urls: string[] = [];
    const fetchMock = vi.fn(async (url: string) => {
      urls.push(url);
      return new Response(JSON.stringify({ content: [] }), { status: 200 });
    });
    vi.stubGlobal("fetch", fetchMock);

    await api.catalog.products({ minPrice: 100, tags: ["a", "b"] });

    expect(urls[0]).toContain("minPrice=100");
    expect(urls[0]).toContain("tags=a");
    expect(urls[0]).toContain("tags=b");
  });
});
