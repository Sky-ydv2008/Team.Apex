package com.apexinnovators.dto;

import com.apexinnovators.entity.ProjectStatus;
import jakarta.validation.constraints.NotNull;

/** PATCH /api/admin/projects/{id}/status body. */
public record ProjectStatusRequest(
        @NotNull(message = "status is required")
        ProjectStatus status) {
}
