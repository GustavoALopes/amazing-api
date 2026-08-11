package com.gustavo.dev.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;

@Embeddable
public class AuditInfo {

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    /** Infrastructure-only constructor for Hibernate. */
    protected AuditInfo() {
    }

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
