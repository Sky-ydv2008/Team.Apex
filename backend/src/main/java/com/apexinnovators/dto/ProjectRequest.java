package com.apexinnovators.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Member-facing project create/update body (POST/PUT /api/projects).
 * Optional text columns stay nullable; member/tech/slug management is admin-side.
 */
public record ProjectRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 190, message = "Title must be at most 190 characters")
        String title,

        @Size(max = 300, message = "Tagline must be at most 300 characters")
        String tagline,

        String description,
        String problem,
        String solution,

        @Size(max = 500, message = "GitHub URL must be at most 500 characters")
        String githubUrl,

        @Size(max = 500, message = "Demo URL must be at most 500 characters")
        String demoUrl,

        @Size(max = 500, message = "Docs URL must be at most 500 characters")
        String docsUrl,

        @Min(value = 1990, message = "Year must be >= 1990")
        @Max(value = 2100, message = "Year must be <= 2100")
        Integer year) {
}
