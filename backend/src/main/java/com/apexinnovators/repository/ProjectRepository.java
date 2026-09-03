package com.apexinnovators.repository;

import com.apexinnovators.entity.Project;
import com.apexinnovators.entity.ProjectStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    long countByStatus(ProjectStatus status);

    long countByStatusIn(List<ProjectStatus> statuses);

    /**
     * Combined public/admin listing with optional q, technology name, year and
     * featured filters. A project matches the tech filter when at least one of
     * its technologies has the given (case-insensitive) name.
     */
    @Query(value = """
            select p from Project p
            where p.status in :statuses
              and (:q = '' or lower(p.title) like lower(concat('%', :q, '%'))
                   or (p.tagline is not null and lower(p.tagline) like lower(concat('%', :q, '%')))
                   or (p.description is not null and lower(p.description) like lower(concat('%', :q, '%'))))
              and (:tech = '' or exists (select 1 from ProjectTechnology pt
                        where pt.projectId = p.id
                          and exists (select 1 from Technology t
                                      where t.id = pt.technologyId
                                        and lower(t.name) = lower(:tech))))
              and (:year is null or p.year = :year)
              and (:featured is null or p.featured = true)
            order by p.createdAt desc, p.id desc
            """,
            countQuery = """
            select count(p) from Project p
            where p.status in :statuses
              and (:q = '' or lower(p.title) like lower(concat('%', :q, '%'))
                   or (p.tagline is not null and lower(p.tagline) like lower(concat('%', :q, '%')))
                   or (p.description is not null and lower(p.description) like lower(concat('%', :q, '%'))))
              and (:tech = '' or exists (select 1 from ProjectTechnology pt
                        where pt.projectId = p.id
                          and exists (select 1 from Technology t
                                      where t.id = pt.technologyId
                                        and lower(t.name) = lower(:tech))))
              and (:year is null or p.year = :year)
              and (:featured is null or p.featured = true)
            """)
    Page<Project> search(@Param("statuses") List<ProjectStatus> statuses,
                         @Param("q") String q,
                         @Param("tech") String tech,
                         @Param("year") Integer year,
                         @Param("featured") Boolean featured,
                         Pageable pageable);
}
