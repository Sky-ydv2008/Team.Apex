package com.apexinnovators.repository;

import com.apexinnovators.entity.ProjectMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectIdOrderByIdAsc(Long projectId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
