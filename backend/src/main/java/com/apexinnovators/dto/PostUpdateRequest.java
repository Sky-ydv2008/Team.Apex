package com.apexinnovators.dto;

import com.apexinnovators.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * PUT /api/posts/{id} body. A null type keeps the post's current type; title
 * and body replace the stored values.
 */
public record PostUpdateRequest(
        PostType type,

        @NotBlank(message = "Title is required")
        @Size(max = 190, message = "Title must be at most 190 characters")
        String title,

        String body) {
}
