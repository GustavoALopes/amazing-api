package com.gustavo.dev.tenant.inputs;

import java.util.UUID;

import jakarta.annotation.Nullable;

public record TenantInfo(
    UUID id,
    @Nullable String name
) {
}
