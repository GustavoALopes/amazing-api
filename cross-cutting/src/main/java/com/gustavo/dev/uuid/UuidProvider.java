package com.gustavo.dev.uuid;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public final class UuidProvider {

    public static UUID getV7() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
