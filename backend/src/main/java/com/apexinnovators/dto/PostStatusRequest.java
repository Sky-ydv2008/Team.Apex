package com.apexinnovators.dto;

import com.apexinnovators.entity.ProjectStatus;
import jakarta.validation.constraints.NotNull;

/** PATCH /api/admin/posts/{id}/status body (posts reuse the project status enum). */
public record PostStatusRequest(
        @NotNull(message = "status is required")
        ProjectStatus status) {
}
