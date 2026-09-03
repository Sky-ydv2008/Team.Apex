package com.apexinnovators.dto;

import java.time.LocalDateTime;

/** Comment payload: {id,authorId,authorName,body,createdAt}. */
public record CommentDto(
        Long id,
        Long authorId,
        String authorName,
        String body,
        LocalDateTime createdAt) {
}
