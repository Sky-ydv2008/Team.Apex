package com.apexinnovators.service;

import com.apexinnovators.dto.StatsDto;
import com.apexinnovators.entity.ProjectStatus;
import com.apexinnovators.entity.UserStatus;
import com.apexinnovators.repository.AchievementRepository;
import com.apexinnovators.repository.HackathonRepository;
import com.apexinnovators.repository.ProjectRepository;
import com.apexinnovators.repository.TechnologyRepository;
import com.apexinnovators.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatsService {

    private static final List<ProjectStatus> VISIBLE =
            List.of(ProjectStatus.APPROVED, ProjectStatus.PUBLISHED);

    private final ProjectRepository projectRepository;
    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final TechnologyRepository technologyRepository;
    private final AchievementRepository achievementRepository;

    /** Public counters: published projects, total hackathons, active members, technologies, achievements. */
    @Transactional(readOnly = true)
    public StatsDto stats() {
        return new StatsDto(
                projectRepository.countByStatusIn(VISIBLE),
                hackathonRepository.count(),
                userRepository.countByStatus(UserStatus.ACTIVE),
                technologyRepository.count(),
                achievementRepository.count());
    }
}
