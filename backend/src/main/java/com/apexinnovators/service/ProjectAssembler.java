package com.apexinnovators.service;

import com.apexinnovators.dto.ProjectDto;
import com.apexinnovators.dto.ProjectHackathonDto;
import com.apexinnovators.dto.ProjectMemberDto;
import com.apexinnovators.dto.TechDto;
import com.apexinnovators.entity.Hackathon;
import com.apexinnovators.entity.HackathonProject;
import com.apexinnovators.entity.Project;
import com.apexinnovators.entity.ProjectMember;
import com.apexinnovators.entity.ProjectTechnology;
import com.apexinnovators.entity.Technology;
import com.apexinnovators.entity.User;
import com.apexinnovators.repository.HackathonProjectRepository;
import com.apexinnovators.repository.HackathonRepository;
import com.apexinnovators.repository.ProjectMemberRepository;
import com.apexinnovators.repository.ProjectTechnologyRepository;
import com.apexinnovators.repository.TechnologyRepository;
import com.apexinnovators.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Assembles the full ProjectDto (technologies/members/hackathons) for one project. */
@Component
@RequiredArgsConstructor
public class ProjectAssembler {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectTechnologyRepository projectTechnologyRepository;
    private final TechnologyRepository technologyRepository;
    private final HackathonProjectRepository hackathonProjectRepository;
    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;

    public ProjectDto toDto(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getTitle(),
                project.getSlug(),
                project.getTagline(),
                project.getDescription(),
                project.getProblem(),
                project.getSolution(),
                project.getStatus(),
                Boolean.TRUE.equals(project.getFeatured()),
                project.getGithubUrl(),
                project.getDemoUrl(),
                project.getDocsUrl(),
                project.getYear(),
                technologiesOf(project.getId()),
                membersOf(project.getId()),
                hackathonsOf(project.getId()),
                project.getCreatedAt());
    }

    private List<TechDto> technologiesOf(Long projectId) {
        List<Long> ids = projectTechnologyRepository.findByProjectId(projectId).stream()
                .map(ProjectTechnology::getTechnologyId)
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Technology> byId = new HashMap<>();
        technologyRepository.findAllById(ids).forEach(t -> byId.put(t.getId(), t));
        return ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(t -> new TechDto(t.getId(), t.getName(), t.getCategory(), t.getIcon()))
                .toList();
    }

    private List<ProjectMemberDto> membersOf(Long projectId) {
        List<ProjectMember> rows = projectMemberRepository.findByProjectIdOrderByIdAsc(projectId);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = rows.stream().map(ProjectMember::getUserId).distinct().toList();
        Map<Long, User> usersById = new HashMap<>();
        userRepository.findAllById(userIds).forEach(u -> usersById.put(u.getId(), u));
        return rows.stream()
                .map(pm -> {
                    User user = usersById.get(pm.getUserId());
                    return new ProjectMemberDto(pm.getUserId(), user == null ? null : user.getName(),
                            pm.getRole(), pm.getContribution());
                })
                .toList();
    }

    private List<ProjectHackathonDto> hackathonsOf(Long projectId) {
        List<Long> ids = hackathonProjectRepository.findByProjectId(projectId).stream()
                .map(HackathonProject::getHackathonId)
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Hackathon> byId = new HashMap<>();
        hackathonRepository.findAllById(ids).forEach(h -> byId.put(h.getId(), h));
        return ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(h -> new ProjectHackathonDto(h.getId(), h.getName()))
                .toList();
    }
}
