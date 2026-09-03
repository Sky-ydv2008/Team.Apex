package com.apexinnovators.service;

import com.apexinnovators.dto.HackathonDto;
import com.apexinnovators.dto.PostDto;
import com.apexinnovators.dto.ProjectDto;
import com.apexinnovators.dto.SearchResponse;
import com.apexinnovators.entity.ProjectStatus;
import com.apexinnovators.repository.HackathonRepository;
import com.apexinnovators.repository.ProjectRepository;
import com.apexinnovators.util.PageUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final List<ProjectStatus> VISIBLE =
            List.of(ProjectStatus.APPROVED, ProjectStatus.PUBLISHED);

    private final ProjectRepository projectRepository;
    private final HackathonRepository hackathonRepository;
    private final ProjectAssembler projectAssembler;
    private final HackathonAssembler hackathonAssembler;
    private final PostService postService;

    /** Cross-entity search over published content only; up to 10 hits per bucket. */
    @Transactional(readOnly = true)
    public SearchResponse search(String q) {
        if (q == null || q.isBlank()) {
            return SearchResponse.empty();
        }
        String query = q.trim();

        List<ProjectDto> projects = projectRepository
                .search(VISIBLE, query, "", null, null, PageUtil.of(0, 10))
                .getContent().stream()
                .map(projectAssembler::toDto)
                .toList();

        List<HackathonDto> hackathons = hackathonRepository
                .search(query, PageUtil.of(0, 10)).stream()
                .map(hackathonAssembler::toDto)
                .toList();

        List<PostDto> posts = postService.searchTopPublished(query);

        return new SearchResponse(projects, hackathons, posts);
    }
}
