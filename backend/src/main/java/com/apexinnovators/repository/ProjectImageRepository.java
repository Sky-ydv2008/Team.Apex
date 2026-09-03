package com.apexinnovators.repository;

import com.apexinnovators.entity.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {

    void deleteByProjectId(Long projectId);
}
