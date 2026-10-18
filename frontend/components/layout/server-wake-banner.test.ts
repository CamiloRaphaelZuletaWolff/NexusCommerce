import { describe, it, expect } from "vitest";
import { healthUrl } from "@/components/layout/server-wake-banner";

describe("healthUrl", () => {
  it("uses the same-origin proxy path for a relative API base", () => {
    expect(healthUrl("/api")).toBe("/api/actuator/health");
    expect(healthUrl("/api/")).toBe("/api/actuator/health");
  });

  it("targets the backend's own actuator for an absolute API base", () => {
    expect(healthUrl("http://localhost:8080/api")).toBe("http://localhost:8080/actuator/health");
  });
});
