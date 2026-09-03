package com.apexinnovators.dto;

import com.apexinnovators.entity.ProjectStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Project payload (contract field names):
 * {id,title,slug,tagline,description,problem,solution,status,featured,
 *  githubUrl,demoUrl,docsUrl,year,technologies,members,hackathons,createdAt}.
 */
public record ProjectDto(
        Long id,
        String title,
        String slug,
        String tagline,
        String description,
        String problem,
        String solution,
        ProjectStatus status,
        boolean featured,
        String githubUrl,
        String demoUrl,
        String docsUrl,
        Integer year,
        List<TechDto> technologies,
        List<ProjectMemberDto> members,
        List<ProjectHackathonDto> hackathons,
        LocalDateTime createdAt) {
}
