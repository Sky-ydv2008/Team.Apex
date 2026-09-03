package com.apexinnovators.dto;

import com.apexinnovators.entity.ProjectStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Admin project create/update body (POST/PUT /api/admin/projects): all project
 * content plus moderation fields (status, featured), optional explicit slug and
 * the member/technology id lists that replace the project's current links.
 */
public record AdminProjectRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 190, message = "Title must be at most 190 characters")
        String title,

        @Size(max = 210, message = "Slug must be at most 210 characters")
        String slug,

        @Size(max = 300, message = "Tagline must be at most 300 characters")
        String tagline,

        String description,
        String problem,
        String solution,

        ProjectStatus status,
        Boolean featured,

        @Size(max = 500, message = "GitHub URL must be at most 500 characters")
        String githubUrl,

        @Size(max = 500, message = "Demo URL must be at most 500 characters")
        String demoUrl,

        @Size(max = 500, message = "Docs URL must be at most 500 characters")
        String docsUrl,

        @Min(value = 1990, message = "Year must be >= 1990")
        @Max(value = 2100, message = "Year must be <= 2100")
        Integer year,

        List<Long> technologyIds,
        List<Long> memberIds) {
}
