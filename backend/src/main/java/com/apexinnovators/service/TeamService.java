package com.apexinnovators.service;

import com.apexinnovators.dto.MemberDto;
import com.apexinnovators.entity.Profile;
import com.apexinnovators.entity.Role;
import com.apexinnovators.entity.User;
import com.apexinnovators.entity.UserStatus;
import com.apexinnovators.repository.ProfileRepository;
import com.apexinnovators.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final List<Role> TEAM_ROLES = List.of(Role.ADMIN, Role.CORE_MEMBER);

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    /**
     * Public team listing: ACTIVE accounts with role ADMIN or CORE_MEMBER
     * (users JOIN profiles). ADMIN first, then CORE_MEMBER, oldest first.
     */
    @Transactional(readOnly = true)
    public List<MemberDto> listPublishedMembers() {
        return userRepository.findAllByStatusOrderByCreatedAtAsc(UserStatus.ACTIVE).stream()
                .filter(user -> TEAM_ROLES.contains(user.getRole()))
                .sorted(Comparator
                        .comparingInt((User u) -> u.getRole() == Role.ADMIN ? 0 : 1)
                        .thenComparing(User::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toMemberDto)
                .toList();
    }

    private MemberDto toMemberDto(User user) {
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        return new MemberDto(
                user.getId(),
                user.getName(),
                user.getRole(),
                profile == null ? null : profile.getHeadline(),
                profile == null ? null : profile.getBio(),
                profile == null ? null : profile.getPhotoUrl(),
                profile == null ? null : profile.getGithub(),
                profile == null ? null : profile.getLinkedin());
    }
}
