package com.apexinnovators.repository;

import com.apexinnovators.entity.Achievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    Page<Achievement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    void deleteByUserId(Long userId);
}
