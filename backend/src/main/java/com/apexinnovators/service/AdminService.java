package com.apexinnovators.service;

import com.apexinnovators.audit.AuditService;
import com.apexinnovators.dto.AdminActivityDto;
import com.apexinnovators.dto.AdminCreateUserRequest;
import com.apexinnovators.dto.AdminMessageDto;
import com.apexinnovators.dto.AdminOverviewDto;
import com.apexinnovators.dto.AdminUserDetailDto;
import com.apexinnovators.dto.AdminUserPatchRequest;
import com.apexinnovators.dto.MessageStatusRequest;
import com.apexinnovators.dto.PageResponse;
import com.apexinnovators.dto.ProfileDto;
import com.apexinnovators.dto.TechDto;
import com.apexinnovators.dto.TechnologyRequest;
import com.apexinnovators.dto.UserDto;
import com.apexinnovators.entity.AuditLog;
import com.apexinnovators.entity.ContactMessage;
import com.apexinnovators.entity.MessageStatus;
import com.apexinnovators.entity.Profile;
import com.apexinnovators.entity.ProjectStatus;
import com.apexinnovators.entity.Role;
import com.apexinnovators.entity.Technology;
import com.apexinnovators.entity.User;
import com.apexinnovators.entity.UserStatus;
import com.apexinnovators.exception.ApiException;
import com.apexinnovators.repository.AuditLogRepository;
import com.apexinnovators.repository.ContactMessageRepository;
import com.apexinnovators.repository.HackathonRepository;
import com.apexinnovators.repository.PostRepository;
import com.apexinnovators.repository.ProfileRepository;
import com.apexinnovators.repository.ProjectRepository;
import com.apexinnovators.repository.TechnologyRepository;
import com.apexinnovators.repository.UserRepository;
import com.apexinnovators.security.UserPrincipal;
import com.apexinnovators.util.PageUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin back office: dashboard overview, user management, contact-message
 * triage and the technologies catalog. Project/hackathon/post/achievement
 * admin actions live in their domain services; every mutating action here
 * appends an audit_logs row.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private static final List<ProjectStatus> VISIBLE =
            List.of(ProjectStatus.APPROVED, ProjectStatus.PUBLISHED);

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final HackathonRepository hackathonRepository;
    private final PostRepository postRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final TechnologyRepository technologyRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    // ------------------------------------------------------------------
    // Overview
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public AdminOverviewDto overview() {
        List<AuditLog> recent = auditLogRepository.findTop10ByOrderByCreatedAtDesc();
        List<Long> actorIds = recent.stream()
                .map(AuditLog::getActorId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> actorNames = new HashMap<>();
        if (!actorIds.isEmpty()) {
            userRepository.findAllById(actorIds)
                    .forEach(u -> actorNames.put(u.getId(), u.getName()));
        }
        List<AdminActivityDto> activity = recent.stream()
                .map(log -> new AdminActivityDto(
                        log.getActorId() == null ? "System" : actorNames.getOrDefault(log.getActorId(), "Unknown"),
                        log.getAction(),
                        log.getEntity(),
                        log.getDetail(),
                        log.getCreatedAt()))
                .toList();
        return new AdminOverviewDto(
                projectRepository.count(),
                hackathonRepository.count(),
                userRepository.countByStatus(UserStatus.ACTIVE),
                postRepository.countByStatusIn(VISIBLE),
                projectRepository.countByStatus(ProjectStatus.PENDING_REVIEW),
                postRepository.countByStatus(ProjectStatus.PENDING_REVIEW),
                contactMessageRepository.countByStatus(MessageStatus.NEW),
                activity);
    }

    // ------------------------------------------------------------------
    // Users
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<UserDto> listUsers(String q, Integer page, Integer size) {
        Pageable pageable = PageUtil.of(page, size);
        Page<User> users = userRepository.search(q == null ? "" : q.trim(), pageable);
        List<UserDto> content = users.getContent().stream()
                .map(u -> new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getStatus()))
                .toList();
        return new PageResponse<>(content, users.getNumber(), users.getSize(),
                users.getTotalElements(), users.getTotalPages());
    }

    /** Change history for a user: entries they caused plus entries targeting them. */
    @Transactional(readOnly = true)
    public PageResponse<AdminActivityDto> userAudit(Long id, Integer page, Integer size) {
        requireUser(id);
        Pageable pageable = PageUtil.of(page, size);
        List<AuditLog> logs = auditLogRepository.findHistoryForUser(id, pageable);
        List<Long> actorIds = logs.stream()
                .map(AuditLog::getActorId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> actorNames = new HashMap<>();
        if (!actorIds.isEmpty()) {
            userRepository.findAllById(actorIds)
                    .forEach(u -> actorNames.put(u.getId(), u.getName()));
        }
        List<AdminActivityDto> content = logs.stream()
                .map(log -> new AdminActivityDto(
                        log.getActorId() == null ? "System" : actorNames.getOrDefault(log.getActorId(), "Unknown"),
                        log.getAction(),
                        log.getEntity(),
                        log.getDetail(),
                        log.getCreatedAt()))
                .toList();
        Page<AuditLog> paged = new org.springframework.data.domain.PageImpl<>(logs, pageable,
                auditLogRepository.countHistoryForUser(id));
        return new PageResponse<>(content, paged.getNumber(), paged.getSize(),
                paged.getTotalElements(), paged.getTotalPages());
    }

    @Transactional(readOnly = true)
    public AdminUserDetailDto getUser(Long id) {
        User user = requireUser(id);
        Profile profile = profileRepository.findByUserId(id).orElse(null);
        return new AdminUserDetailDto(
                user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus(),
                user.getCreatedAt(),
                profile == null ? null
                        : new ProfileDto(profile.getHeadline(), profile.getBio(),
                                profile.getPhotoUrl(), profile.getGithub(), profile.getLinkedin()));
    }

    @Transactional
    public UserDto patchUser(UserPrincipal actor, Long id, AdminUserPatchRequest request) {
        User user = requireUser(id);
        List<String> changes = new ArrayList<>();

        String newEmail = request.email() == null ? null : request.email().trim().toLowerCase();
        if (request.name() != null && !request.name().trim().equalsIgnoreCase(user.getName())) {
            String old = user.getName();
            user.setName(request.name().trim());
            changes.add("name '" + old + "' -> '" + user.getName() + "'");
        }
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new ApiException(HttpStatus.CONFLICT, "An account with that email already exists");
            }
            String old = user.getEmail();
            user.setEmail(newEmail);
            changes.add("email '" + old + "' -> '" + newEmail + "'");
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
            changes.add("password reset");
        }
        if (request.role() != null && request.role() != user.getRole()) {
            changes.add("role " + user.getRole() + " -> " + request.role());
            user.setRole(request.role());
        }
        if (request.status() != null && request.status() != user.getStatus()) {
            changes.add("status " + user.getStatus() + " -> " + request.status());
            user.setStatus(request.status());
        }
        if (!changes.isEmpty()) {
            userRepository.save(user);
            auditService.record(actor.getId(), "UPDATE", "User", user.getId(),
                    "by user #" + actor.getId() + ": " + String.join(", ", changes)
                            + " for '" + user.getName() + "'");
        }
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus());
    }

    /** ADMIN creates a member account (role optional, default MEMBER) with an empty profile row. */
    @Transactional
    public UserDto createUser(UserPrincipal actor, AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with that email already exists");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role() == null ? Role.MEMBER : request.role());
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        Profile profile = new Profile();
        profile.setUserId(user.getId());
        profile.setHeadline(request.headline());
        profile.setBio(request.bio());
        profile.setGithub(request.github());
        profile.setLinkedin(request.linkedin());
        profile.setPhotoUrl(request.photoUrl());
        profileRepository.save(profile);

        auditService.record(actor.getId(), "CREATE", "User", user.getId(),
                "Created account '" + user.getEmail() + "' with role " + user.getRole());
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus());
    }

    // ------------------------------------------------------------------
    // Contact messages
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<AdminMessageDto> listMessages(MessageStatus status, Integer page, Integer size) {
        Pageable pageable = PageUtil.of(page, size);
        Page<ContactMessage> messages = (status == null)
                ? contactMessageRepository.findAllByOrderByCreatedAtDesc(pageable)
                : contactMessageRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        List<AdminMessageDto> content = messages.getContent().stream()
                .map(this::toMessageDto)
                .toList();
        return new PageResponse<>(content, messages.getNumber(), messages.getSize(),
                messages.getTotalElements(), messages.getTotalPages());
    }

    @Transactional
    public AdminMessageDto patchMessage(UserPrincipal actor, Long id, MessageStatusRequest request) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message not found"));
        message.setStatus(request.status());
        contactMessageRepository.save(message);
        auditService.record(actor.getId(), "UPDATE", "Message", message.getId(),
                "Message status -> " + message.getStatus() + " from '" + message.getEmail() + "'");
        return toMessageDto(message);
    }

    // ------------------------------------------------------------------
    // Technologies (admin-managed catalog)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<TechDto> listTechnologies() {
        return technologyRepository.findAllByOrderByNameAsc().stream()
                .map(t -> new TechDto(t.getId(), t.getName(), t.getCategory(), t.getIcon()))
                .toList();
    }

    @Transactional
    public TechDto createTechnology(UserPrincipal actor, TechnologyRequest request) {
        String name = request.name().trim();
        if (technologyRepository.existsByName(name)) {
            throw new ApiException(HttpStatus.CONFLICT, "Technology '" + name + "' already exists");
        }
        Technology technology = new Technology();
        applyTechnology(technology, request);
        technologyRepository.save(technology);
        auditService.record(actor.getId(), "CREATE", "Technology", technology.getId(),
                "Created technology '" + technology.getName() + "'");
        return toTechDto(technology);
    }

    @Transactional
    public TechDto updateTechnology(UserPrincipal actor, Long id, TechnologyRequest request) {
        Technology technology = technologyRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technology not found"));
        String newName = request.name().trim();
        if (!newName.equalsIgnoreCase(technology.getName())
                && technologyRepository.existsByName(newName)) {
            throw new ApiException(HttpStatus.CONFLICT, "Technology '" + newName + "' already exists");
        }
        applyTechnology(technology, request);
        technologyRepository.save(technology);
        auditService.record(actor.getId(), "UPDATE", "Technology", technology.getId(),
                "Updated technology '" + technology.getName() + "'");
        return toTechDto(technology);
    }

    @Transactional
    public void deleteTechnology(UserPrincipal actor, Long id) {
        Technology technology = technologyRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technology not found"));
        String name = technology.getName();
        technologyRepository.delete(technology);
        auditService.record(actor.getId(), "DELETE", "Technology", id,
                "Deleted technology '" + name + "'");
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private AdminMessageDto toMessageDto(ContactMessage message) {
        return new AdminMessageDto(message.getId(), message.getName(), message.getEmail(),
                message.getSubject(), message.getMessage(), message.getStatus(),
                message.getCreatedAt());
    }

    private void applyTechnology(Technology technology, TechnologyRequest request) {
        technology.setName(request.name().trim());
        technology.setCategory(request.category() == null ? null : request.category().trim());
        technology.setIcon(request.icon() == null ? null : request.icon().trim());
    }

    private TechDto toTechDto(Technology technology) {
        return new TechDto(technology.getId(), technology.getName(),
                technology.getCategory(), technology.getIcon());
    }
}
