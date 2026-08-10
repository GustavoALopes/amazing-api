package com.gustavo.dev.tenant;

import java.util.Objects;

/**
 * Provides access to the tenant associated with the current request.
 */
public final class TenantContext {

    private static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();

    private TenantContext() {
    }

    /**
     * Returns the current request's tenant id, or {@code null} when no tenant id
     * is bound to the current execution context.
     */
    public static String getTenantId() {
        return TENANT_ID.orElse(null);
    }

    /**
     * Runs an operation with the supplied tenant id bound to the current
     * execution context. The binding is automatically removed afterwards.
     */
    public static <X extends Throwable> void runWithTenantId(
        String tenantId,
        ThrowingRunnable<X> operation
    ) throws X {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(operation, "operation must not be null");

        ScopedValue.where(TENANT_ID, tenantId).call(() -> {
            operation.run();
            return null;
        });
    }

    @FunctionalInterface
    public interface ThrowingRunnable<X extends Throwable> {
        void run() throws X;
    }
}
