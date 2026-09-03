package com.apexinnovators.controller;

import com.apexinnovators.dto.PageResponse;
import com.apexinnovators.dto.ProjectDto;
import com.apexinnovators.dto.ProjectRequest;
import com.apexinnovators.security.UserPrincipal;
import com.apexinnovators.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project showcase: public listing/detail plus member create, edit and submission")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "List published projects (page/size/q/tech/year/featured filters)")
    public PageResponse<ProjectDto> list(@RequestParam(required = false) String q,
                                         @RequestParam(required = false) String tech,
                                         @RequestParam(required = false) Integer year,
                                         @RequestParam(required = false) Boolean featured,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size) {
        return projectService.listPublished(q, tech, year, featured, page, size);
    }

    @GetMapping("/{identifier}")
    @Operation(summary = "Get a project by numeric id or slug; drafts are visible to members and admins only")
    public ProjectDto get(@PathVariable String identifier,
                          @AuthenticationPrincipal UserPrincipal viewer) {
        return projectService.getByIdOrSlug(identifier, viewer);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a project as DRAFT; the acting user becomes a project member")
    public ProjectDto create(@AuthenticationPrincipal UserPrincipal principal,
                             @Valid @RequestBody ProjectRequest request) {
        return projectService.create(principal, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a project: author may remove their own unpublished project, "
            + "CORE_MEMBER/ADMIN any project")
    public ResponseEntity<Void> deleteOwn(@AuthenticationPrincipal UserPrincipal principal,
                                          @PathVariable Long id) {
        projectService.deleteOwn(principal, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project content (project member, CORE_MEMBER or ADMIN)")
    public ProjectDto update(@AuthenticationPrincipal UserPrincipal principal,
                             @PathVariable Long id,
                             @Valid @RequestBody ProjectRequest request) {
        return projectService.update(principal, id, request);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit a draft or rejected project for review (status -> PENDING_REVIEW)")
    public ProjectDto submit(@AuthenticationPrincipal UserPrincipal principal,
                             @PathVariable Long id) {
        return projectService.submit(principal, id);
    }
}
