package com.apexinnovators.repository;

import com.apexinnovators.entity.AuditLog;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findTop10ByOrderByCreatedAtDesc();

    /** Everything touching a user id: changes they made (actor) or changes made to them. */
    @Query("select a from AuditLog a where a.actorId = :userId "
            + "or (a.entity = 'User' and a.entityId = :userId) "
            + "order by a.createdAt desc")
    List<AuditLog> findHistoryForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("select count(a) from AuditLog a where a.actorId = :userId "
            + "or (a.entity = 'User' and a.entityId = :userId)")
    long countHistoryForUser(@Param("userId") Long userId);
}
