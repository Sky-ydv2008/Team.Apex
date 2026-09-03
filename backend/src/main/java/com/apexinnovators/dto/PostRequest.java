package com.apexinnovators.dto;

import com.apexinnovators.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** POST /api/posts body (creates a DRAFT). */
public record PostRequest(
        @NotNull(message = "type is required")
        PostType type,

        @NotBlank(message = "Title is required")
        @Size(max = 190, message = "Title must be at most 190 characters")
        String title,

        String body) {
}
