package com.apexinnovators.repository;

import com.apexinnovators.entity.ProjectTechnology;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTechnologyRepository extends JpaRepository<ProjectTechnology, Long> {

    List<ProjectTechnology> findByProjectId(Long projectId);
}
