package com.apexinnovators.repository;

import com.apexinnovators.entity.Hackathon;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HackathonRepository extends JpaRepository<Hackathon, Long> {

    Optional<Hackathon> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<Hackathon> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            select h from Hackathon h
            where (:q = '' or lower(h.name) like lower(concat('%', :q, '%'))
                     or (h.description is not null and lower(h.description) like lower(concat('%', :q, '%')))
                     or (h.organizer is not null and lower(h.organizer) like lower(concat('%', :q, '%'))))
            order by h.createdAt desc, h.id desc
            """)
    List<Hackathon> search(@Param("q") String q, Pageable pageable);
}
