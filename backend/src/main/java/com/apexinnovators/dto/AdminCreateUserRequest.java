package com.apexinnovators.dto;

import com.apexinnovators.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** ADMIN creates a member account with optional profile fields. */
public record AdminCreateUserRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must be at most 120 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "A valid email is required")
        @Size(max = 190, message = "Email must be at most 190 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
        String password,

        Role role,

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
