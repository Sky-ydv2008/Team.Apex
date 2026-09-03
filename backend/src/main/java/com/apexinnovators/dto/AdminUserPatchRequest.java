package com.apexinnovators.dto;

import com.apexinnovators.entity.Role;
import com.apexinnovators.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** PATCH /api/admin/users/{id} body — any subset of name/email/password/role/status. */
public record AdminUserPatchRequest(
        @Size(max = 120, message = "Name must be at most 120 characters")
        String name,

        @Email(message = "A valid email is required")
        @Size(max = 190, message = "Email must be at most 190 characters")
        String email,

        @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
        String password,

        Role role,
        UserStatus status) {
}
