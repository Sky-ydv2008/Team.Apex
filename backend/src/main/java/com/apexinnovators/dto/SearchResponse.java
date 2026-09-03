package com.apexinnovators.dto;

import java.util.List;

/** GET /api/search response: {projects:[],hackathons:[],posts:[]} — published content only. */
public record SearchResponse(
        List<ProjectDto> projects,
        List<HackathonDto> hackathons,
        List<PostDto> posts) {

    public static SearchResponse empty() {
        return new SearchResponse(List.of(), List.of(), List.of());
    }
}
