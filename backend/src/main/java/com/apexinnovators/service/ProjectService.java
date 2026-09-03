package com.apexinnovators.service;

import com.apexinnovators.audit.AuditService;
import com.apexinnovators.dto.AdminProjectRequest;
import com.apexinnovators.dto.PageResponse;
import com.apexinnovators.dto.ProjectDto;
import com.apexinnovators.dto.ProjectRequest;
import com.apexinnovators.dto.ProjectStatusRequest;
import com.apexinnovators.entity.Project;
import com.apexinnovators.entity.ProjectMember;
import com.apexinnovators.entity.ProjectStatus;
import com.apexinnovators.entity.ProjectTechnology;
import com.apexinnovators.entity.Role;
import com.apexinnovators.entity.Technology;
import com.apexinnovators.entity.User;
import com.apexinnovators.exception.ApiException;
import com.apexinnovators.repository.BookmarkRepository;
import com.apexinnovators.repository.HackathonProjectRepository;
import com.apexinnovators.repository.ProjectImageRepository;
import com.apexinnovators.repository.ProjectMemberRepository;
import com.apexinnovators.repository.ProjectRepository;
import com.apexinnovators.repository.ProjectTechnologyRepository;
import com.apexinnovators.repository.TechnologyRepository;
import com.apexinnovators.repository.UserRepository;
import com.apexinnovators.security.UserPrincipal;
import com.apexinnovators.util.PageUtil;
import com.apexinnovators.util.Slugify;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final List<ProjectStatus> VISIBLE =
            List.of(ProjectStatus.APPROVED, ProjectStatus.PUBLISHED);
    private static final List<ProjectStatus> ALL_STATUSES =
            List.of(ProjectStatus.values());

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectTechnologyRepository projectTechnologyRepository;
    private final HackathonProjectRepository hackathonProjectRepository;
    private final ProjectImageRepository projectImageRepository;
    private final BookmarkRepository bookmarkRepository;
    private final TechnologyRepository technologyRepository;
    private final UserRepository userRepository;
    private final ProjectAssembler projectAssembler;
    private final AuditService auditService;

    // ------------------------------------------------------------------
    // Public reads
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<ProjectDto> listPublished(String q, String tech, Integer year,
                                                  Boolean featured, Integer page, Integer size) {
        return list(VISIBLE, q, tech, year, featured, page, size);
    }

    /** Resolves either a numeric id or a slug; published projects are public, drafts are member/admin-only. */
    @Transactional(readOnly = true)
    public ProjectDto getByIdOrSlug(String identifier, UserPrincipal viewer) {
        Project project = findByIdentifier(identifier);
        boolean visible = VISIBLE.contains(project.getStatus());
        if (!visible && !canViewUnpublished(project, viewer)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Project not found");
        }
        return projectAssembler.toDto(project);
    }

    // ------------------------------------------------------------------
    // Member actions
    // ------------------------------------------------------------------

    /** Creates a DRAFT project and registers the acting user as its first project_member. */
    @Transactional
    public ProjectDto create(UserPrincipal actor, ProjectRequest request) {
        Project project = new Project();
        applyContent(project, request);
        project.setStatus(ProjectStatus.DRAFT);
        project.setFeatured(false);
        project.setSlug(uniqueSlug(Slugify.slugify(project.getTitle()), null));
        projectRepository.save(project);

        ProjectMember member = new ProjectMember();
        member.setProjectId(project.getId());
        member.setUserId(actor.getId());
        projectMemberRepository.save(member);
        auditService.record(actor.getId(), "CREATE", "Project", project.getId(),
                "Created project '" + project.getTitle() + "' as draft (user #" + actor.getId() + ")");
        return projectAssembler.toDto(project);
    }

    @Transactional
    public ProjectDto update(UserPrincipal actor, Long id, ProjectRequest request) {
        Project project = requireProject(id);
        requireManager(project, actor);
        applyContent(project, request);
        projectRepository.save(project);
        auditService.record(actor.getId(), "UPDATE", "Project", project.getId(),
                "Edited project '" + project.getTitle() + "' (user #" + actor.getId() + ")");
        return projectAssembler.toDto(project);
    }

    /** Submits a DRAFT/REJECTED project for review (status -> PENDING_REVIEW). */
    @Transactional
    public ProjectDto submit(UserPrincipal actor, Long id) {
        Project project = requireProject(id);
        requireManager(project, actor);
        if (project.getStatus() != ProjectStatus.DRAFT && project.getStatus() != ProjectStatus.REJECTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Only draft or rejected projects can be submitted for review");
        }
        project.setStatus(ProjectStatus.PENDING_REVIEW);
        projectRepository.save(project);
        auditService.record(actor.getId(), "SUBMIT", "Project", project.getId(),
                "Submitted project '" + project.getTitle() + "' for review (user #" + actor.getId() + ")");
        return projectAssembler.toDto(project);
    }

    // ------------------------------------------------------------------
    // Admin actions (each mutating action appends an audit_logs row)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<ProjectDto> listAdmin(String q, ProjectStatus status, Integer page, Integer size) {
        List<ProjectStatus> statuses = status == null ? ALL_STATUSES : List.of(status);
        return list(statuses, q, null, null, null, page, size);
    }

    @Transactional
    public ProjectDto createByAdmin(UserPrincipal actor, AdminProjectRequest request) {
        Project project = new Project();
        applyAdminContent(project, request);
        project.setStatus(request.status() == null ? ProjectStatus.DRAFT : request.status());
        project.setFeatured(Boolean.TRUE.equals(request.featured()));
        project.setSlug(resolveSlug(request, project.getTitle(), null));
        projectRepository.save(project);
        replaceTechnologies(project.getId(), request.technologyIds());
        replaceMembers(project.getId(), request.memberIds());
        auditService.record(actor.getId(), "CREATE", "Project", project.getId(),
                "Created project '" + project.getTitle() + "'");
        return projectAssembler.toDto(project);
    }

    @Transactional
    public ProjectDto updateByAdmin(UserPrincipal actor, Long id, AdminProjectRequest request) {
        Project project = requireProject(id);
        applyAdminContent(project, request);
        if (request.status() != null) {
            project.setStatus(request.status());
        }
        if (request.featured() != null) {
            project.setFeatured(request.featured());
        }
        project.setSlug(resolveSlug(request, project.getTitle(), id));
        projectRepository.save(project);
        replaceTechnologies(project.getId(), request.technologyIds());
        replaceMembers(project.getId(), request.memberIds());
        auditService.record(actor.getId(), "UPDATE", "Project", project.getId(),
                "Updated project '" + project.getTitle() + "'");
        return projectAssembler.toDto(project);
    }

    @Transactional
    public ProjectDto patchStatus(UserPrincipal actor, Long id, ProjectStatusRequest request) {
        Project project = requireProject(id);
        project.setStatus(request.status());
        projectRepository.save(project);
        auditService.record(actor.getId(), "STATUS_CHANGE", "Project", project.getId(),
                "Status " + project.getStatus() + " for project '" + project.getTitle() + "'");
        return projectAssembler.toDto(project);
    }

    /** Admin/core moderation: delete any project. */
    @Transactional
    public void deleteByAdmin(UserPrincipal actor, Long id) {
        requireProject(id);
        deleteInternal(actor, id);
        auditService.record(actor.getId(), "DELETE", "Project", id,
                "Deleted project (moderation) by user #" + actor.getId());
    }

    /** Members delete their own non-published project; core/admin may delete any project. */
    @Transactional
    public void deleteOwn(UserPrincipal actor, Long id) {
        Project project = requireProject(id);
        boolean privileged = actor.getRole() == Role.ADMIN || actor.getRole() == Role.CORE_MEMBER;
        if (!privileged) {
            if (project.getStatus() == ProjectStatus.PUBLISHED || project.getStatus() == ProjectStatus.APPROVED) {
                throw new ApiException(HttpStatus.FORBIDDEN,
                        "Published projects cannot be deleted by their authors — ask a core member or admin");
            }
            if (!projectMemberRepository.existsByProjectIdAndUserId(project.getId(), actor.getId())) {
                throw new ApiException(HttpStatus.FORBIDDEN,
                        "You can only delete projects you are a member of");
            }
        }
        String title = project.getTitle();
        deleteInternal(actor, id);
        auditService.record(actor.getId(), "DELETE", "Project", id,
                "Deleted project '" + title + "' by user #" + actor.getId());
    }

    /** Removes every row that references the project so MySQL and H2 behave identically. */
    private void deleteInternal(UserPrincipal actor, Long id) {
        projectTechnologyRepository.deleteByProjectId(id);
        projectMemberRepository.deleteByProjectId(id);
        hackathonProjectRepository.deleteByProjectId(id);
        projectImageRepository.deleteByProjectId(id);
        bookmarkRepository.deleteByProjectId(id);
        projectRepository.deleteById(id);
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private PageResponse<ProjectDto> list(List<ProjectStatus> statuses, String q, String tech,
                                          Integer year, Boolean featured, Integer page, Integer size) {
        Pageable pageable = PageUtil.of(page, size);
        Page<Project> projects = projectRepository.search(statuses,
                q == null ? "" : q.trim(),
                tech == null ? "" : tech.trim(),
                year, featured, pageable);
        List<ProjectDto> content = projects.getContent().stream()
                .map(projectAssembler::toDto)
                .toList();
        return new PageResponse<>(content, projects.getNumber(), projects.getSize(),
                projects.getTotalElements(), projects.getTotalPages());
    }

    private boolean canViewUnpublished(Project project, UserPrincipal viewer) {
        if (viewer == null) {
            return false;
        }
        if (viewer.getRole() == Role.ADMIN) {
            return true;
        }
        return projectMemberRepository.existsByProjectIdAndUserId(project.getId(), viewer.getId());
    }

    private void requireManager(Project project, UserPrincipal actor) {
        if (actor.getRole() == Role.ADMIN || actor.getRole() == Role.CORE_MEMBER) {
            return;
        }
        if (!projectMemberRepository.existsByProjectIdAndUserId(project.getId(), actor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "You can only manage projects you are a member of");
        }
    }

    private void applyContent(Project project, ProjectRequest request) {
        project.setTitle(request.title().trim());
        project.setTagline(trimToNull(request.tagline()));
        project.setDescription(request.description());
        project.setProblem(request.problem());
        project.setSolution(request.solution());
        project.setGithubUrl(trimToNull(request.githubUrl()));
        project.setDemoUrl(trimToNull(request.demoUrl()));
        project.setDocsUrl(trimToNull(request.docsUrl()));
        project.setYear(request.year());
    }

    private void applyAdminContent(Project project, AdminProjectRequest request) {
        project.setTitle(request.title().trim());
        project.setTagline(trimToNull(request.tagline()));
        project.setDescription(request.description());
        project.setProblem(request.problem());
        project.setSolution(request.solution());
        project.setGithubUrl(trimToNull(request.githubUrl()));
        project.setDemoUrl(trimToNull(request.demoUrl()));
        project.setDocsUrl(trimToNull(request.docsUrl()));
        project.setYear(request.year());
    }

    /** Admin-supplied slug (else slugified title) made unique. */
    private String resolveSlug(AdminProjectRequest request, String title, Long id) {
        String requested = request.slug();
        String base = (requested == null || requested.isBlank())
                ? Slugify.slugify(title)
                : Slugify.slugify(requested);
        return uniqueSlug(base, id);
    }

    private String uniqueSlug(String base, Long id) {
        String slug = base;
        int counter = 2;
        if (id == null) {
            while (projectRepository.existsBySlug(slug)) {
                slug = base + "-" + counter++;
            }
        } else {
            while (projectRepository.existsBySlugAndIdNot(slug, id)) {
                slug = base + "-" + counter++;
            }
        }
        return slug;
    }

    /** Replaces the project's technology links. null keeps them, a list (even empty) replaces. */
    private void replaceTechnologies(Long projectId, List<Long> technologyIds) {
        if (technologyIds == null) {
            return;
        }
        List<Long> ids = sanitizeIds(technologyIds);
        if (!ids.isEmpty()) {
            List<Long> found = technologyRepository.findAllById(ids).stream()
                    .map(Technology::getId)
                    .toList();
            for (Long id : ids) {
                if (!found.contains(id)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown technology id " + id);
                }
            }
        }
        List<ProjectTechnology> existing = projectTechnologyRepository.findByProjectId(projectId);
        if (!existing.isEmpty()) {
            projectTechnologyRepository.deleteAll(existing);
        }
        for (Long technologyId : ids) {
            projectTechnologyRepository.save(new ProjectTechnology(projectId, technologyId));
        }
    }

    /** Replaces the project's members. null keeps them, a list (even empty) replaces. */
    private void replaceMembers(Long projectId, List<Long> memberIds) {
        if (memberIds == null) {
            return;
        }
        List<Long> ids = sanitizeIds(memberIds);
        if (!ids.isEmpty()) {
            List<Long> found = userRepository.findAllById(ids).stream()
                    .map(User::getId)
                    .toList();
            for (Long id : ids) {
                if (!found.contains(id)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown user id " + id);
                }
            }
        }
        List<ProjectMember> existing = projectMemberRepository.findByProjectIdOrderByIdAsc(projectId);
        if (!existing.isEmpty()) {
            projectMemberRepository.deleteAll(existing);
        }
        for (Long userId : ids) {
            ProjectMember member = new ProjectMember();
            member.setProjectId(projectId);
            member.setUserId(userId);
            projectMemberRepository.save(member);
        }
    }

    private List<Long> sanitizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
    }

    private Project requireProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private Project findByIdentifier(String identifier) {
        try {
            return requireProject(Long.parseLong(identifier));
        } catch (NumberFormatException ex) {
            return projectRepository.findBySlug(identifier)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
