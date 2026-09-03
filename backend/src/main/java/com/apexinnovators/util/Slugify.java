package com.apexinnovators.util;

import java.util.Locale;

/** Slug generation for projects and hackathons (ASCII-safe). */
public final class Slugify {

    private Slugify() {
    }

    /**
     * Lowercases the input and collapses runs of non-alphanumeric characters
     * into single dashes. Never returns an empty string.
     */
    public static String slugify(String input) {
        if (input == null) {
            return "item";
        }
        String slug = input.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isEmpty() ? "item" : slug;
    }
}
