package com.apexinnovators.dto;

import com.apexinnovators.entity.Role;
import com.apexinnovators.entity.UserStatus;

/** PATCH /api/admin/users/{id} body — at least one of role/status must be present. */
public record AdminUserPatchRequest(
        Role role,
        UserStatus status) {
}
