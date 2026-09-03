package com.apexinnovators.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** Members update their own display profile; null fields are left unchanged. */
public record ProfileUpdateRequest(
        @Size(max = 120, message = "Name must be at most 120 characters")
        String name,

        @Size(max = 190, message = "Headline must be at most 190 characters")
        String headline,

        @Size(max = 5000, message = "Bio is too long")
        String bio,

        @Size(max = 190, message = "GitHub handle is too long")
        String github,

        @Size(max = 190, message = "LinkedIn URL is too long")
        String linkedin,

        @Size(max = 500, message = "Photo URL is too long")
        String photoUrl) {
}
