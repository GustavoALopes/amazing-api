package com.gustavo.dev.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
public class AuditInfo {

    @Column(name = "created_at")
    private final Instant createdAt;

    @Column(name = "created_by")
    private final String createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    public AuditInfo(final Instant createdAt, final String createdBy) {
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public AuditInfo(
            final Instant createdAt,
            final String createdBy,
            final Instant updatedAt,
            final String updatedBy
    ) {
        this(createdAt, createdBy);
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    static AuditInfo createNew(final String createdBy) {
        return new AuditInfo(Instant.now(), createdBy);
    }
}
