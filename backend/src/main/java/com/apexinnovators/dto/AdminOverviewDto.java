package com.apexinnovators.dto;

import java.util.List;

/** GET /api/admin/overview response. */
public record AdminOverviewDto(
        long totalProjects,
        long totalHackathons,
        long totalMembers,
        long publishedPosts,
        long pendingProjects,
        long pendingPosts,
        long unreadMessages,
        List<AdminActivityDto> recentActivity) {
}
