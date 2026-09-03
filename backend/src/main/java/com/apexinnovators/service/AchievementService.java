package com.apexinnovators.service;

import com.apexinnovators.audit.AuditService;
import com.apexinnovators.dto.AchievementDto;
import com.apexinnovators.dto.AchievementRequest;
import com.apexinnovators.dto.PageResponse;
import com.apexinnovators.entity.Achievement;
import com.apexinnovators.entity.AchievementType;
import com.apexinnovators.entity.User;
import com.apexinnovators.exception.ApiException;
import com.apexinnovators.repository.AchievementRepository;
import com.apexinnovators.repository.UserRepository;
import com.apexinnovators.security.UserPrincipal;
import com.apexinnovators.util.PageUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    /** Public (and admin list) page of achievements, newest first, with member names. */
    @Transactional(readOnly = true)
    public PageResponse<AchievementDto> page(Integer page, Integer size) {
        Pageable pageable = PageUtil.of(page, size);
        Page<Achievement> achievements = achievementRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<AchievementDto> content = achievements.getContent().stream()
                .map(this::toDto)
                .toList();
        return new PageResponse<>(content, achievements.getNumber(), achievements.getSize(),
                achievements.getTotalElements(), achievements.getTotalPages());
    }

    @Transactional
    public AchievementDto createByAdmin(UserPrincipal actor, AchievementRequest request) {
        requireUser(request.userId());
        Achievement achievement = new Achievement();
        apply(achievement, request);
        achievementRepository.save(achievement);
        auditService.record(actor.getId(), "CREATE", "Achievement", achievement.getId(),
                "Created achievement '" + achievement.getTitle() + "'");
        return toDto(achievement);
    }

    @Transactional
    public AchievementDto updateByAdmin(UserPrincipal actor, Long id, AchievementRequest request) {
        Achievement achievement = requireAchievement(id);
        requireUser(request.userId());
        apply(achievement, request);
        achievementRepository.save(achievement);
        auditService.record(actor.getId(), "UPDATE", "Achievement", achievement.getId(),
                "Updated achievement '" + achievement.getTitle() + "'");
        return toDto(achievement);
    }

    @Transactional
    public void deleteByAdmin(UserPrincipal actor, Long id) {
        Achievement achievement = requireAchievement(id);
        String title = achievement.getTitle();
        achievementRepository.delete(achievement);
        auditService.record(actor.getId(), "DELETE", "Achievement", id,
                "Deleted achievement '" + title + "'");
    }

    private void apply(Achievement achievement, AchievementRequest request) {
        achievement.setUserId(request.userId());
        achievement.setTitle(request.title().trim());
        achievement.setType(request.type() == null ? AchievementType.CERTIFICATE : request.type());
        achievement.setIssuer(trimToNull(request.issuer()));
        achievement.setAwardDate(request.awardDate());
        achievement.setDescription(request.description());
        achievement.setVerifyUrl(trimToNull(request.verifyUrl()));
    }

    private AchievementDto toDto(Achievement achievement) {
        User user = userRepository.findById(achievement.getUserId()).orElse(null);
        return new AchievementDto(
                achievement.getId(),
                achievement.getUserId(),
                user == null ? null : user.getName(),
                achievement.getTitle(),
                achievement.getType(),
                achievement.getIssuer(),
                achievement.getAwardDate(),
                achievement.getDescription(),
                achievement.getVerifyUrl());
    }

    private Achievement requireAchievement(Long id) {
        return achievementRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Achievement not found"));
    }

    private void requireUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown user id " + userId);
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
