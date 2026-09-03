package com.apexinnovators.dto;

/** Project contributor: {userId,name,role,contribution}. */
public record ProjectMemberDto(
        Long userId,
        String name,
        String role,
        String contribution) {
}
