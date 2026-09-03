package com.apexinnovators.dto;

import com.apexinnovators.entity.PostType;
import com.apexinnovators.entity.ProjectStatus;
import java.time.LocalDateTime;

/**
 * Post payload (contract field names):
 * {id,authorId,authorName,type,title,body,status,createdAt,commentCount,likeCount,likedByMe}.
 */
public record PostDto(
        Long id,
        Long authorId,
        String authorName,
        PostType type,
        String title,
        String body,
        ProjectStatus status,
        LocalDateTime createdAt,
        long commentCount,
        long likeCount,
        boolean likedByMe) {
}
