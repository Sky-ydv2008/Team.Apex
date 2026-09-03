package com.apexinnovators.dto;

import jakarta.validation.constraints.NotBlank;

/** POST /api/posts/{postId}/comments body. */
public record CommentRequest(
        @NotBlank(message = "body is required")
        String body) {
}
