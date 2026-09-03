package com.apexinnovators.dto;

import com.apexinnovators.entity.PostType;
import com.apexinnovators.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** ADMIN creates a post directly; status defaults to PUBLISHED when omitted. */
public record AdminCreatePostRequest(
        @NotNull(message = "type is required")
        PostType type,

        @NotBlank(message = "Title is required")
        @Size(max = 190, message = "Title must be at most 190 characters")
        String title,

        @Size(max = 20000, message = "Body is too long")
        String body,

        ProjectStatus status) {
}
