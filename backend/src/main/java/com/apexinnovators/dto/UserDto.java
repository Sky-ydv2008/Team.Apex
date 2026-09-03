package com.apexinnovators.dto;

import com.apexinnovators.entity.Role;
import com.apexinnovators.entity.UserStatus;

/** Public user shape: {id,name,email,role,status}. */
public record UserDto(
        Long id,
        String name,
        String email,
        Role role,
        UserStatus status) {
}
