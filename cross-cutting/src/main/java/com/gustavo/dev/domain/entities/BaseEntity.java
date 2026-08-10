package com.gustavo.dev.domain.entities;

import com.gustavo.dev.domain.entities.inputs.ExecutionContext;
import com.gustavo.dev.uuid.UuidProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.util.UUID;
import java.util.concurrent.Callable;


@MappedSuperclass
public abstract class BaseEntity<T> {

    @Id
    protected T id;

    protected AuditInfo auditInfo;

    @Column(name = "correlation_id")
    protected UUID correlationId;

    protected BaseEntity() {
    }


    protected void baseCreateNew(
            final ExecutionContext executionContext,
            final Callable<T> getId
    ) throws Exception {
        this.id = getId.call();
        this.auditInfo = AuditInfo.createNew(executionContext.executionUser());
        this.correlationId = executionContext.correlationId();
    }
}
