package com.nexuscommerce.audit.service;

import com.nexuscommerce.audit.domain.AuditEventType;
import com.nexuscommerce.audit.domain.AuditLog;
import com.nexuscommerce.audit.domain.AuditOutcome;
import com.nexuscommerce.audit.repository.AuditLogRepository;
import com.nexuscommerce.shared.web.RequestMetadata;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records audit events durably.
 *
 * <p>Writes run in a {@code REQUIRES_NEW} transaction so the audit trail survives
 * even when the caller's transaction rolls back (e.g. a failed login attempt).
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditEventType eventType, AuditOutcome outcome, UUID actorUserId,
                       RequestMetadata metadata, Map<String, ?> details) {
        try {
            String ip = metadata != null ? metadata.ipAddress() : null;
            String userAgent = metadata != null ? metadata.userAgent() : null;
            Map<String, Object> payload = details == null ? null : new LinkedHashMap<>(details);
            auditLogRepository.save(new AuditLog(eventType, outcome, actorUserId, ip, userAgent, payload));
        } catch (RuntimeException ex) {
            // Auditing must never break the primary flow; log and move on.
            log.warn("Failed to write audit log for event {} ({})", eventType, outcome, ex);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(AuditEventType eventType, UUID actorUserId, RequestMetadata metadata,
                        Map<String, ?> details) {
        record(eventType, AuditOutcome.SUCCESS, actorUserId, metadata, details);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failure(AuditEventType eventType, UUID actorUserId, RequestMetadata metadata,
                        Map<String, ?> details) {
        record(eventType, AuditOutcome.FAILURE, actorUserId, metadata, details);
    }
}
