package com.apexinnovators.service;

import com.apexinnovators.dto.HackathonDto;
import com.apexinnovators.dto.HackathonMemberDto;
import com.apexinnovators.dto.HackathonProjectLinkDto;
import com.apexinnovators.entity.Hackathon;
import com.apexinnovators.entity.HackathonMember;
import com.apexinnovators.entity.HackathonProject;
import com.apexinnovators.entity.Project;
import com.apexinnovators.repository.HackathonMemberRepository;
import com.apexinnovators.repository.HackathonProjectRepository;
import com.apexinnovators.repository.ProjectRepository;
import com.apexinnovators.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Assembles the full HackathonDto (members/projects) for one hackathon. */
@Component
@RequiredArgsConstructor
public class HackathonAssembler {

    private final HackathonMemberRepository hackathonMemberRepository;
    private final HackathonProjectRepository hackathonProjectRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public HackathonDto toDto(Hackathon hackathon) {
        return new HackathonDto(
                hackathon.getId(),
                hackathon.getName(),
                hackathon.getSlug(),
                hackathon.getOrganizer(),
                hackathon.getDate(),
                hackathon.getDescription(),
                hackathon.getChallenge(),
                hackathon.getResult(),
                hackathon.getCertificateUrl(),
                hackathon.getPresentationUrl(),
                membersOf(hackathon.getId()),
                projectsOf(hackathon.getId()),
                hackathon.getCreatedAt());
    }

    private List<HackathonMemberDto> membersOf(Long hackathonId) {
        List<HackathonMember> rows = hackathonMemberRepository.findByHackathonIdOrderByIdAsc(hackathonId);
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, String> namesById = new HashMap<>();
        userRepository.findAllById(rows.stream().map(HackathonMember::getUserId).distinct().toList())
                .forEach(u -> namesById.put(u.getId(), u.getName()));
        return rows.stream()
                .map(hm -> new HackathonMemberDto(hm.getUserId(), namesById.get(hm.getUserId())))
                .toList();
    }

    private List<HackathonProjectLinkDto> projectsOf(Long hackathonId) {
        List<HackathonProject> rows = hackathonProjectRepository.findByHackathonId(hackathonId);
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, Project> projectsById = new HashMap<>();
        projectRepository.findAllById(rows.stream().map(HackathonProject::getProjectId).distinct().toList())
                .forEach(p -> projectsById.put(p.getId(), p));
        return rows.stream()
                .map(hp -> projectsById.get(hp.getProjectId()))
                .filter(Objects::nonNull)
                .map(p -> new HackathonProjectLinkDto(p.getId(), p.getTitle(), p.getSlug()))
                .toList();
    }
}
