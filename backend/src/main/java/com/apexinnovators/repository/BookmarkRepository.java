package com.apexinnovators.repository;

import com.apexinnovators.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    void deleteByUserId(Long userId);
}
