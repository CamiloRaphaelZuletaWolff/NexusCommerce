package com.nexuscommerce.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Base for all JPA entities.
 *
 * <p>UUID generation strategy (ADR-0007): v4 UUIDs are generated application-side
 * by Hibernate's {@link UuidGenerator} at persist time. The database columns also
 * carry {@code DEFAULT gen_random_uuid()} as a safety net for out-of-band inserts.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
