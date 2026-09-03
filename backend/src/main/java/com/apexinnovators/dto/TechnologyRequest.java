package com.apexinnovators.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Admin technology create/update body (POST/PUT /api/admin/technologies). */
public record TechnologyRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must be at most 120 characters")
        String name,

        @Size(max = 120, message = "Category must be at most 120 characters")
        String category,

        @Size(max = 500, message = "Icon must be at most 500 characters")
        String icon) {
}
