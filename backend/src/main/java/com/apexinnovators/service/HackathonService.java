package com.apexinnovators.service;

import com.apexinnovators.audit.AuditService;
import com.apexinnovators.dto.HackathonAdminRequest;
import com.apexinnovators.dto.HackathonDto;
import com.apexinnovators.dto.PageResponse;
import com.apexinnovators.entity.Hackathon;
import com.apexinnovators.entity.HackathonMember;
import com.apexinnovators.entity.HackathonProject;
import com.apexinnovators.entity.Project;
import com.apexinnovators.entity.User;
import com.apexinnovators.exception.ApiException;
import com.apexinnovators.repository.HackathonMemberRepository;
import com.apexinnovators.repository.HackathonProjectRepository;
import com.apexinnovators.repository.HackathonRepository;
import com.apexinnovators.repository.ProjectRepository;
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
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final HackathonMemberRepository hackathonMemberRepository;
    private final HackathonProjectRepository hackathonProjectRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final HackathonAssembler hackathonAssembler;
    private final AuditService auditService;

    // ------------------------------------------------------------------
    // Public reads (hackathons carry no status column; all rows are public)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<HackathonDto> list(Integer page, Integer size) {
        Pageable pageable = PageUtil.of(page, size);
        Page<Hackathon> hackathons = hackathonRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<HackathonDto> content = hackathons.getContent().stream()
                .map(hackathonAssembler::toDto)
                .toList();
        return new PageResponse<>(content, hackathons.getNumber(), hackathons.getSize(),
                hackathons.getTotalElements(), hackathons.getTotalPages());
    }

    @Transactional(readOnly = true)
    public HackathonDto getByIdOrSlug(String identifier) {
        Hackathon hackathon = findByIdentifier(identifier);
        return hackathonAssembler.toDto(hackathon);
    }

    // ------------------------------------------------------------------
    // Admin CRUD (each mutating action appends an audit_logs row)
    // ------------------------------------------------------------------

    @Transactional
    public HackathonDto createByAdmin(UserPrincipal actor, HackathonAdminRequest request) {
        Hackathon hackathon = new Hackathon();
        applyContent(hackathon, request);
        hackathon.setSlug(resolveSlug(request, hackathon.getName(), null));
        hackathonRepository.save(hackathon);
        replaceMembers(hackathon.getId(), request.memberIds());
        replaceProjects(hackathon.getId(), request.projectIds());
        auditService.record(actor.getId(), "CREATE", "Hackathon", hackathon.getId(),
                "Created hackathon '" + hackathon.getName() + "'");
        return hackathonAssembler.toDto(hackathon);
    }

    @Transactional
    public HackathonDto updateByAdmin(UserPrincipal actor, Long id, HackathonAdminRequest request) {
        Hackathon hackathon = requireHackathon(id);
        applyContent(hackathon, request);
        hackathon.setSlug(resolveSlug(request, hackathon.getName(), id));
        hackathonRepository.save(hackathon);
        replaceMembers(hackathon.getId(), request.memberIds());
        replaceProjects(hackathon.getId(), request.projectIds());
        auditService.record(actor.getId(), "UPDATE", "Hackathon", hackathon.getId(),
                "Updated hackathon '" + hackathon.getName() + "'");
        return hackathonAssembler.toDto(hackathon);
    }

    /** Deleting relies on schema FKs (ON DELETE CASCADE) for member/project link rows. */
    @Transactional
    public void deleteByAdmin(UserPrincipal actor, Long id) {
        Hackathon hackathon = requireHackathon(id);
        String name = hackathon.getName();
        hackathonRepository.delete(hackathon);
        auditService.record(actor.getId(), "DELETE", "Hackathon", id,
                "Deleted hackathon '" + name + "'");
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private void applyContent(Hackathon hackathon, HackathonAdminRequest request) {
        hackathon.setName(request.name().trim());
        hackathon.setOrganizer(trimToNull(request.organizer()));
        hackathon.setDate(request.date());
        hackathon.setDescription(request.description());
        hackathon.setChallenge(request.challenge());
        hackathon.setResult(trimToNull(request.result()));
        hackathon.setCertificateUrl(trimToNull(request.certificateUrl()));
        hackathon.setPresentationUrl(trimToNull(request.presentationUrl()));
    }

    private String resolveSlug(HackathonAdminRequest request, String name, Long id) {
        String requested = request.slug();
        String base = (requested == null || requested.isBlank())
                ? Slugify.slugify(name)
                : Slugify.slugify(requested);
        String slug = base;
        int counter = 2;
        if (id == null) {
            while (hackathonRepository.existsBySlug(slug)) {
                slug = base + "-" + counter++;
            }
        } else {
            while (hackathonRepository.existsBySlugAndIdNot(slug, id)) {
                slug = base + "-" + counter++;
            }
        }
        return slug;
    }

    /** Replaces member links. null keeps them, a list (even empty) replaces. */
    private void replaceMembers(Long hackathonId, List<Long> memberIds) {
        if (memberIds == null) {
            return;
        }
        List<Long> ids = sanitizeIds(memberIds);
        if (!ids.isEmpty()) {
            List<Long> found = userRepository.findAllById(ids).stream()
                    .map(User::getId)
                    .toList();
            for (Long userId : ids) {
                if (!found.contains(userId)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown user id " + userId);
                }
            }
        }
        List<HackathonMember> existing = hackathonMemberRepository.findByHackathonIdOrderByIdAsc(hackathonId);
        if (!existing.isEmpty()) {
            hackathonMemberRepository.deleteAll(existing);
        }
        for (Long userId : ids) {
            hackathonMemberRepository.save(new HackathonMember(hackathonId, userId));
        }
    }

    /** Replaces project links. null keeps them, a list (even empty) replaces. */
    private void replaceProjects(Long hackathonId, List<Long> projectIds) {
        if (projectIds == null) {
            return;
        }
        List<Long> ids = sanitizeIds(projectIds);
        if (!ids.isEmpty()) {
            List<Long> found = projectRepository.findAllById(ids).stream()
                    .map(Project::getId)
                    .toList();
            for (Long projectId : ids) {
                if (!found.contains(projectId)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown project id " + projectId);
                }
            }
        }
        List<HackathonProject> existing = hackathonProjectRepository.findByHackathonId(hackathonId);
        if (!existing.isEmpty()) {
            hackathonProjectRepository.deleteAll(existing);
        }
        for (Long projectId : ids) {
            hackathonProjectRepository.save(new HackathonProject(hackathonId, projectId));
        }
    }

    private List<Long> sanitizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
    }

    private Hackathon requireHackathon(Long id) {
        return hackathonRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hackathon not found"));
    }

    private Hackathon findByIdentifier(String identifier) {
        try {
            return requireHackathon(Long.parseLong(identifier));
        } catch (NumberFormatException ex) {
            return hackathonRepository.findBySlug(identifier)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hackathon not found"));
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
