package com.apexinnovators.controller;

import com.apexinnovators.dto.AdminActivityDto;
import com.apexinnovators.dto.AdminCreatePostRequest;
import com.apexinnovators.dto.AdminCreateUserRequest;
import com.apexinnovators.dto.AdminMessageDto;
import com.apexinnovators.dto.AdminOverviewDto;
import com.apexinnovators.dto.AdminProjectRequest;
import com.apexinnovators.dto.AdminUserDetailDto;
import com.apexinnovators.dto.AdminUserPatchRequest;
import com.apexinnovators.dto.AchievementDto;
import com.apexinnovators.dto.AchievementRequest;
import com.apexinnovators.dto.HackathonAdminRequest;
import com.apexinnovators.dto.HackathonDto;
import com.apexinnovators.dto.MessageStatusRequest;
import com.apexinnovators.dto.PageResponse;
import com.apexinnovators.dto.PostDto;
import com.apexinnovators.dto.PostStatusRequest;
import com.apexinnovators.dto.ProjectDto;
import com.apexinnovators.dto.ProjectStatusRequest;
import com.apexinnovators.dto.TechDto;
import com.apexinnovators.dto.TechnologyRequest;
import com.apexinnovators.dto.UserDto;
import com.apexinnovators.entity.MessageStatus;
import com.apexinnovators.entity.ProjectStatus;
import com.apexinnovators.security.UserPrincipal;
import com.apexinnovators.service.AchievementService;
import com.apexinnovators.service.AdminService;
import com.apexinnovators.service.HackathonService;
import com.apexinnovators.service.PostService;
import com.apexinnovators.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** ADMIN-only back office surface. Every mutating action appends an audit_logs row. */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "ADMIN-only back office: overview, content CRUD, moderation, users, messages, technologies")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ProjectService projectService;
    private final HackathonService hackathonService;
    private final PostService postService;
    private final AchievementService achievementService;

    // ------------------------------------------------------------------
    // Overview
    // ------------------------------------------------------------------

    @GetMapping("/overview")
    @Operation(summary = "Dashboard counters plus the 10 most recent audit entries")
    public AdminOverviewDto overview() {
        return adminService.overview();
    }

    // ------------------------------------------------------------------
    // Projects
    // ------------------------------------------------------------------

    @GetMapping("/projects")
    @Operation(summary = "Page of all projects (any status), optional q/status filters")
    public PageResponse<ProjectDto> listProjects(@RequestParam(required = false) String q,
                                                 @RequestParam(required = false) ProjectStatus status,
                                                 @RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer size) {
        return projectService.listAdmin(q, status, page, size);
    }

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a project with any status, featured flag, technologies and members")
    public ProjectDto createProject(@AuthenticationPrincipal UserPrincipal principal,
                                    @Valid @RequestBody AdminProjectRequest request) {
        return projectService.createByAdmin(principal, request);
    }

    @PutMapping("/projects/{id}")
    @Operation(summary = "Replace project content, moderation fields and member/technology links")
    public ProjectDto updateProject(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable Long id,
                                    @Valid @RequestBody AdminProjectRequest request) {
        return projectService.updateByAdmin(principal, id, request);
    }

    @PatchMapping("/projects/{id}/status")
    @Operation(summary = "Set a project status directly (approve/reject/publish)")
    public ProjectDto patchProjectStatus(@AuthenticationPrincipal UserPrincipal principal,
                                         @PathVariable Long id,
                                         @Valid @RequestBody ProjectStatusRequest request) {
        return projectService.patchStatus(principal, id, request);
    }

    @DeleteMapping("/projects/{id}")
    @Operation(summary = "Delete a project (child rows removed by schema FKs)")
    public ResponseEntity<Void> deleteProject(@AuthenticationPrincipal UserPrincipal principal,
                                              @PathVariable Long id) {
        projectService.deleteByAdmin(principal, id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Hackathons
    // ------------------------------------------------------------------

    @GetMapping("/hackathons")
    @Operation(summary = "Page of hackathons, newest first")
    public PageResponse<HackathonDto> listHackathons(@RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer size) {
        return hackathonService.list(page, size);
    }

    @PostMapping("/hackathons")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a hackathon with members and linked projects")
    public HackathonDto createHackathon(@AuthenticationPrincipal UserPrincipal principal,
                                        @Valid @RequestBody HackathonAdminRequest request) {
        return hackathonService.createByAdmin(principal, request);
    }

    @PutMapping("/hackathons/{id}")
    @Operation(summary = "Replace hackathon content and member/project links")
    public HackathonDto updateHackathon(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id,
                                        @Valid @RequestBody HackathonAdminRequest request) {
        return hackathonService.updateByAdmin(principal, id, request);
    }

    @DeleteMapping("/hackathons/{id}")
    @Operation(summary = "Delete a hackathon (links removed by schema FKs)")
    public ResponseEntity<Void> deleteHackathon(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long id) {
        hackathonService.deleteByAdmin(principal, id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Users
    // ------------------------------------------------------------------

    @GetMapping("/users")
    @Operation(summary = "Page of users with name/email search (q)")
    public PageResponse<UserDto> listUsers(@RequestParam(required = false) String q,
                                           @RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size) {
        return adminService.listUsers(q, page, size);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a member account with optional profile fields")
    public UserDto createUser(@AuthenticationPrincipal UserPrincipal principal,
                              @Valid @RequestBody AdminCreateUserRequest request) {
        return adminService.createUser(principal, request);
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "User detail including profile fields")
    public AdminUserDetailDto getUser(@PathVariable Long id) {
        return adminService.getUser(id);
    }

    @GetMapping("/users/{id}/audit")
    @Operation(summary = "Change history for a user: actions they performed plus actions targeting them")
    public PageResponse<AdminActivityDto> getUserAudit(@PathVariable Long id,
                                                       @RequestParam(required = false) Integer page,
                                                       @RequestParam(required = false) Integer size) {
        return adminService.userAudit(id, page, size);
    }

    @PatchMapping("/users/{id}")
    @Operation(summary = "Update name/email/password/role/status of a user (any subset)")
    public UserDto patchUser(@AuthenticationPrincipal UserPrincipal principal,
                             @PathVariable Long id,
                             @Valid @RequestBody AdminUserPatchRequest request) {
        return adminService.patchUser(principal, id, request);
    }

    // ------------------------------------------------------------------
    // Posts (moderation)
    // ------------------------------------------------------------------

    @GetMapping("/posts")
    @Operation(summary = "Page of all posts (any status), optional status filter")
    public PageResponse<PostDto> listPosts(@RequestParam(required = false) ProjectStatus status,
                                           @RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size) {
        return postService.pageAdmin(status, page, size);
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a post as the acting admin (status defaults to PUBLISHED)")
    public PostDto createPost(@AuthenticationPrincipal UserPrincipal principal,
                              @Valid @RequestBody AdminCreatePostRequest request) {
        return postService.createAdmin(principal, request);
    }

    @PatchMapping("/posts/{id}/status")
    @Operation(summary = "Approve, reject or publish a post")
    public PostDto patchPostStatus(@AuthenticationPrincipal UserPrincipal principal,
                                   @PathVariable Long id,
                                   @Valid @RequestBody PostStatusRequest request) {
        return postService.patchStatus(principal, id, request);
    }

    // ------------------------------------------------------------------
    // Achievements
    // ------------------------------------------------------------------

    @GetMapping("/achievements")
    @Operation(summary = "Page of achievements, newest first")
    public PageResponse<AchievementDto> listAchievements(@RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer size) {
        return achievementService.page(page, size);
    }

    @PostMapping("/achievements")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an achievement for a member")
    public AchievementDto createAchievement(@AuthenticationPrincipal UserPrincipal principal,
                                            @Valid @RequestBody AchievementRequest request) {
        return achievementService.createByAdmin(principal, request);
    }

    @PutMapping("/achievements/{id}")
    @Operation(summary = "Replace an achievement")
    public AchievementDto updateAchievement(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable Long id,
                                            @Valid @RequestBody AchievementRequest request) {
        return achievementService.updateByAdmin(principal, id, request);
    }

    @DeleteMapping("/achievements/{id}")
    @Operation(summary = "Delete an achievement")
    public ResponseEntity<Void> deleteAchievement(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable Long id) {
        achievementService.deleteByAdmin(principal, id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Contact messages
    // ------------------------------------------------------------------

    @GetMapping("/messages")
    @Operation(summary = "Page of contact messages, optional status filter")
    public PageResponse<AdminMessageDto> listMessages(@RequestParam(required = false) MessageStatus status,
                                                      @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer size) {
        return adminService.listMessages(status, page, size);
    }

    @PatchMapping("/messages/{id}")
    @Operation(summary = "Mark a contact message READ or REPLIED")
    public AdminMessageDto patchMessage(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id,
                                        @Valid @RequestBody MessageStatusRequest request) {
        return adminService.patchMessage(principal, id, request);
    }

    // ------------------------------------------------------------------
    // Technologies
    // ------------------------------------------------------------------

    @GetMapping("/technologies")
    @Operation(summary = "All technologies, alphabetical")
    public List<TechDto> listTechnologies() {
        return adminService.listTechnologies();
    }

    @PostMapping("/technologies")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a technology")
    public TechDto createTechnology(@AuthenticationPrincipal UserPrincipal principal,
                                    @Valid @RequestBody TechnologyRequest request) {
        return adminService.createTechnology(principal, request);
    }

    @PutMapping("/technologies/{id}")
    @Operation(summary = "Replace a technology")
    public TechDto updateTechnology(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable Long id,
                                    @Valid @RequestBody TechnologyRequest request) {
        return adminService.updateTechnology(principal, id, request);
    }

    @DeleteMapping("/technologies/{id}")
    @Operation(summary = "Delete a technology (project links removed by schema FKs)")
    public ResponseEntity<Void> deleteTechnology(@AuthenticationPrincipal UserPrincipal principal,
                                                 @PathVariable Long id) {
        adminService.deleteTechnology(principal, id);
        return ResponseEntity.noContent().build();
    }
}
