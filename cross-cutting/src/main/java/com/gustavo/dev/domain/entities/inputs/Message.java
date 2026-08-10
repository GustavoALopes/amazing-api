package com.gustavo.dev.domain.entities.inputs;

import java.util.Objects;

public record Message(Type type, String text) {

    public Message {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(text, "text must not be null");
    }

    public enum Type {
        INFORMATION,
        SUCCESS,
        ERROR
    }
}
