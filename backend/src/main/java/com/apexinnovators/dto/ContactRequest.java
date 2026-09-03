package com.apexinnovators.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** POST /api/contact request and echo body: {name,email,subject,message}. */
public record ContactRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must be at most 120 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "A valid email is required")
        @Size(max = 190, message = "Email must be at most 190 characters")
        String email,

        @Size(max = 190, message = "Subject must be at most 190 characters")
        String subject,

        @NotBlank(message = "Message is required")
        String message) {
}
