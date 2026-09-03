package com.apexinnovators.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Admin hackathon create/update body (POST/PUT /api/admin/hackathons): all
 * hackathon content plus optional explicit slug and the member/project id
 * lists that replace the hackathon's current links.
 */
public record HackathonAdminRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 190, message = "Name must be at most 190 characters")
        String name,

        @Size(max = 210, message = "Slug must be at most 210 characters")
        String slug,

        @Size(max = 190, message = "Organizer must be at most 190 characters")
        String organizer,

        LocalDate date,

        String description,
        String challenge,

        @Size(max = 300, message = "Result must be at most 300 characters")
        String result,

        @Size(max = 500, message = "Certificate URL must be at most 500 characters")
        String certificateUrl,

        @Size(max = 500, message = "Presentation URL must be at most 500 characters")
        String presentationUrl,

        List<Long> memberIds,
        List<Long> projectIds) {
}
