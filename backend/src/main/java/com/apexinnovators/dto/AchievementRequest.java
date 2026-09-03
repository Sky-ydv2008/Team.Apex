package com.apexinnovators.dto;

import com.apexinnovators.entity.AchievementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Admin achievement create/update body (POST/PUT /api/admin/achievements). */
public record AchievementRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "Title is required")
        @Size(max = 190, message = "Title must be at most 190 characters")
        String title,

        AchievementType type,

        @Size(max = 190, message = "Issuer must be at most 190 characters")
        String issuer,

        LocalDate awardDate,

        String description,

        @Size(max = 500, message = "Verify URL must be at most 500 characters")
        String verifyUrl) {
}
