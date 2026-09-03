package com.apexinnovators.dto;

import com.apexinnovators.entity.Role;

/** Public team member (users JOIN profiles): {id,name,role,headline,bio,photoUrl,github,linkedin}. */
public record MemberDto(
        Long id,
        String name,
        Role role,
        String headline,
        String bio,
        String photoUrl,
        String github,
        String linkedin) {
}
