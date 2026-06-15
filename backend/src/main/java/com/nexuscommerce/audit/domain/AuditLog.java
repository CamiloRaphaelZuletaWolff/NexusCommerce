package com.nexuscommerce.audit.domain;

import com.nexuscommerce.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Immutable record of a sensitive action. */
@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor
public class AuditLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private AuditEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuditOutcome outcome;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;

    public AuditLog(AuditEventType eventType, AuditOutcome outcome, UUID actorUserId,
                    String ipAddress, String userAgent, Map<String, Object> details) {
        this.eventType = eventType;
        this.outcome = outcome;
        this.actorUserId = actorUserId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.details = details;
    }
}
