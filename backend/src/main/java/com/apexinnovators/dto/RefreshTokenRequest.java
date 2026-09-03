package com.apexinnovators.dto;

import jakarta.validation.constraints.NotBlank;

/** POST /api/auth/refresh body. */
public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken is required")
        String refreshToken) {
}
