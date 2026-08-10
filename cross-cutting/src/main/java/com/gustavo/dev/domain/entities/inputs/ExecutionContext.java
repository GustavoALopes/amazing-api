package com.gustavo.dev.domain.entities.inputs;

import com.gustavo.dev.tenant.inputs.TenantInfo;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ExecutionContext {

    private final OffsetDateTime timestamp;
    private final UUID correlationId;
    private final TenantInfo tenantInfo;
    private final String executionUser;
    private final ConcurrentMap<UUID, Message> messages;
    private final Set<Exception> exceptions;

    public ExecutionContext(
            final OffsetDateTime timestamp,
            final UUID correlationId,
            final TenantInfo tenantInfo,
            final String executionUser
    ) {
        this(
                timestamp,
                correlationId,
                tenantInfo,
                executionUser,
                Map.of(),
                Set.of()
        );
    }

    public ExecutionContext(
            final OffsetDateTime timestamp,
            final UUID correlationId,
            final TenantInfo tenantInfo,
            final String executionUser,
            final Map<UUID, Message> messages,
            final Set<Exception> exceptions
    ) {
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.correlationId = Objects.requireNonNull(correlationId, "correlationId must not be null");
        this.tenantInfo = Objects.requireNonNull(tenantInfo, "tenantInfo must not be null");
        this.executionUser = Objects.requireNonNull(executionUser, "executionUser must not be null");

        this.messages = new ConcurrentHashMap<>(
                Objects.requireNonNull(messages, "messages must not be null")
        );
        this.exceptions = Set.copyOf(
                Objects.requireNonNull(exceptions, "exceptions must not be null")
        );
    }

    public static ExecutionContext Create(
            final UUID correlationId,
            final TenantInfo tenantInfo,
            final String executionUser
    ) {

        return new ExecutionContext(
                OffsetDateTime.now(),
                correlationId,
                tenantInfo,
                executionUser,
                Map.of(),
                Set.of()
        );
    }

    public OffsetDateTime timestamp() {
        return timestamp;
    }

    public UUID correlationId() {
        return correlationId;
    }

    public TenantInfo tenantInfo() {
        return tenantInfo;
    }

    public String executionUser() {
        return executionUser;
    }

    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    public boolean hasErrorMessages() {
        return hasMessages() && messages.values().stream()
                .anyMatch(message -> message.type() == Message.Type.ERROR);
    }

    public boolean hasException() {
        return !exceptions.isEmpty();
    }

    public boolean IsSuccessful() {
        return !hasErrorMessages() && !hasException();
    }

    public boolean IsPartiallySuccessful() {
        return messages.values().stream()
                .anyMatch(message -> message.type() == Message.Type.SUCCESS);
    }

    public Map<UUID, Message> getMessages() {
        return Map.copyOf(messages);
    }

    public Set<Exception> getException() {
        return Set.copyOf(exceptions);
    }
}
