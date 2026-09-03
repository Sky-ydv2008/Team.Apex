package com.apexinnovators.dto;

import com.apexinnovators.entity.Role;
import com.apexinnovators.entity.UserStatus;
import java.time.LocalDateTime;

/** Admin user detail: user + profile. */
public record AdminUserDetailDto(
        Long id,
        String name,
        String email,
        Role role,
        UserStatus status,
        LocalDateTime createdAt,
        ProfileDto profile) {
}
