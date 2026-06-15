package com.nexuscommerce.shared.util;

import java.util.Locale;

/** URL-safe slug generation shared across features. */
public final class Slugs {

    private Slugs() {
    }

    public static String slugify(String input) {
        if (input == null) {
            return "item";
        }
        String slug = input.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isEmpty() ? "item" : slug;
    }
}
