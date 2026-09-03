package com.apexinnovators.audit;

import com.apexinnovators.entity.AuditLog;
import com.apexinnovators.repository.AuditLogRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Append-only writer for audit_logs. Every admin mutating action records
 * (actor_id, action, entity, entity_id, detail) through this service.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(Long actorId, String action, String entity, Long entityId, String detail) {
        AuditLog logEntry = new AuditLog();
        logEntry.setActorId(actorId);
        logEntry.setAction(action);
        logEntry.setEntity(entity);
        logEntry.setEntityId(entityId);
        logEntry.setDetail(detail);
        logEntry.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(logEntry);
    }
}
