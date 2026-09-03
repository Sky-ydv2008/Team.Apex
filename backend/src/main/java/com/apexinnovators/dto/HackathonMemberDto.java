package com.apexinnovators.dto;

/** Hackathon participant: {userId,name}. */
public record HackathonMemberDto(
        Long userId,
        String name) {
}
