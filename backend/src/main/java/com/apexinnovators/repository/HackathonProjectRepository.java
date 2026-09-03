package com.apexinnovators.repository;

import com.apexinnovators.entity.HackathonProject;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonProjectRepository extends JpaRepository<HackathonProject, Long> {

    List<HackathonProject> findByHackathonId(Long hackathonId);

    List<HackathonProject> findByProjectId(Long projectId);
}
