package com.nexuscommerce.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

/** Base for entities that track a last-modified timestamp (e.g. users, stores). */
@Getter
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
