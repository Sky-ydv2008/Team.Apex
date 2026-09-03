package com.apexinnovators.dto;

/** GET /api/public/stats response. */
public record StatsDto(
        long projects,
        long hackathons,
        long members,
        long technologies,
        long achievements) {
}
