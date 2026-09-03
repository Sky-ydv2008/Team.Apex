package com.apexinnovators.dto;

import com.apexinnovators.entity.MessageStatus;
import jakarta.validation.constraints.NotNull;

/** PATCH /api/admin/messages/{id} body. */
public record MessageStatusRequest(
        @NotNull(message = "status is required")
        MessageStatus status) {
}
