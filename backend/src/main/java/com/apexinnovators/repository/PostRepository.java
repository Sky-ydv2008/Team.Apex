package com.apexinnovators.repository;

import com.apexinnovators.entity.Post;
import com.apexinnovators.entity.PostType;
import com.apexinnovators.entity.ProjectStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByStatusInOrderByCreatedAtDesc(List<ProjectStatus> statuses, Pageable pageable);

    Page<Post> findByStatusInAndTypeOrderByCreatedAtDesc(List<ProjectStatus> statuses,
                                                         PostType type,
                                                         Pageable pageable);

    long countByStatus(ProjectStatus status);

    long countByStatusIn(List<ProjectStatus> statuses);

    @Query("""
            select p from Post p
            where p.status in :statuses
              and (:q = '' or lower(p.title) like lower(concat('%', :q, '%'))
                   or (p.body is not null and lower(p.body) like lower(concat('%', :q, '%'))))
            order by p.createdAt desc, p.id desc
            """)
    List<Post> searchTop(@Param("statuses") List<ProjectStatus> statuses,
                         @Param("q") String q,
                         Pageable pageable);
}
