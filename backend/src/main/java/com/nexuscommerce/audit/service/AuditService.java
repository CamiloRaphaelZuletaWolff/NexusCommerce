package com.nexuscommerce.audit.service;

import com.nexuscommerce.audit.domain.AuditEventType;
import com.nexuscommerce.audit.domain.AuditLog;
import com.nexuscommerce.audit.domain.AuditOutcome;
import com.nexuscommerce.audit.repository.AuditLogRepository;
import com.nexuscommerce.shared.web.RequestMetadata;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Records audit events durably.
 *
 * <p>Writes run in their own {@code REQUIRES_NEW} transaction so the audit trail
 * survives even when the caller's transaction rolls back (e.g. a failed checkout).
 * The transaction is driven programmatically so that a failure at <em>commit</em>
 * time (constraint violations are only detected on flush) is also caught — auditing
 * must never break the primary flow.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final TransactionTemplate requiresNew;

    public AuditService(AuditLogRepository auditLogRepository, PlatformTransactionManager transactionManager) {
        this.auditLogRepository = auditLogRepository;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void record(AuditEventType eventType, AuditOutcome outcome, UUID actorUserId,
                       RequestMetadata metadata, Map<String, ?> details) {
        try {
            String ip = metadata != null ? metadata.ipAddress() : null;
            String userAgent = metadata != null ? metadata.userAgent() : null;
            Map<String, Object> payload = details == null ? null : new LinkedHashMap<>(details);
            requiresNew.executeWithoutResult(status ->
                    auditLogRepository.save(new AuditLog(eventType, outcome, actorUserId, ip, userAgent, payload)));
        } catch (RuntimeException ex) {
            // Auditing must never break the primary flow; log and move on.
            log.warn("Failed to write audit log for event {} ({})", eventType, outcome, ex);
        }
    }

    public void success(AuditEventType eventType, UUID actorUserId, RequestMetadata metadata,
                        Map<String, ?> details) {
        record(eventType, AuditOutcome.SUCCESS, actorUserId, metadata, details);
    }

    public void failure(AuditEventType eventType, UUID actorUserId, RequestMetadata metadata,
                        Map<String, ?> details) {
        record(eventType, AuditOutcome.FAILURE, actorUserId, metadata, details);
    }
}
