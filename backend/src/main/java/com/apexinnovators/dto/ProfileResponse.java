package com.apexinnovators.dto;

import com.apexinnovators.entity.Role;
import com.apexinnovators.entity.UserStatus;

/** Self-view of the signed-in account plus profile: {id,name,email,role,status,headline,...}. */
public record ProfileResponse(
        Long id,
        String name,
        String email,
        Role role,
        UserStatus status,
        String headline,
        String bio,
        String github,
        String linkedin,
        String photoUrl) {
}
