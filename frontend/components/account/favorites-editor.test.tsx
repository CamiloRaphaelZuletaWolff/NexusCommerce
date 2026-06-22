import { describe, it, expect, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";
import { FavoritesEditor } from "@/components/account/favorites-editor";
import type { CustomerProfileResponse } from "@/types/api";

vi.mock("@/lib/api", () => ({
  ApiError: class ApiError extends Error {},
  api: {
    catalog: {
      categories: vi.fn(async () => [
        { id: "1", slug: "electronics", name: "Electronics", parentSlug: null, attributes: [], createdAt: "" },
      ]),
      brands: vi.fn(async () => [
        { id: "1", slug: "nexies", name: "Nexies", description: null, createdAt: "" },
      ]),
      updateProfile: vi.fn(async (data: { favoriteCategories?: string[]; favoriteBrands?: string[] }) => ({
        id: "u", email: "e", fullName: null,
        favoriteCategories: data.favoriteCategories ?? [],
        favoriteBrands: data.favoriteBrands ?? [],
        recentlyViewedProductIds: [], createdAt: "", updatedAt: "",
      })),
    },
  },
}));

import { api } from "@/lib/api";

const profile: CustomerProfileResponse = {
  id: "u", email: "e", fullName: null,
  favoriteCategories: [], favoriteBrands: [],
  recentlyViewedProductIds: [], createdAt: "", updatedAt: "",
};

function renderWithClient(ui: ReactNode) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(<QueryClientProvider client={client}>{ui}</QueryClientProvider>);
}

describe("FavoritesEditor", () => {
  it("enables Save only after a change, and persists the selection", async () => {
    const user = userEvent.setup();
    renderWithClient(<FavoritesEditor profile={profile} />);

    // Save is disabled until something changes.
    expect(screen.getByRole("button", { name: /saved/i })).toBeDisabled();

    // Toggle a category once the options have loaded.
    const chip = await screen.findByRole("button", { name: "Electronics" });
    await user.click(chip);

    const save = screen.getByRole("button", { name: /save preferences/i });
    expect(save).toBeEnabled();

    await user.click(save);
    await waitFor(() =>
      expect(api.catalog.updateProfile).toHaveBeenCalledWith({
        favoriteCategories: ["electronics"],
        favoriteBrands: [],
      }),
    );
  });
});
