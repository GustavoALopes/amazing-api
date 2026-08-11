package com.gustavo.dev.api.domains.customer.entities.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public final class Document {
    @Column(name = "document_value", nullable = false, length = 64)
    private String value;

    @Column(name = "document_type", nullable = false, length = 32)
    private String type;

    protected Document() { }

    private Document(final String value, final String type) {
        this.value = value;
        this.type = type;
    }

    public static Document of(final String value, final String type) {
        if (value == null || value.isBlank() || value.length() > 64
                || type == null || type.isBlank() || type.length() > 32) {
            return null;
        }
        return new Document(value, type);
    }

    public String value() { return value; }
    public String type() { return type; }

    @Override public boolean equals(final Object other) {
        return this == other || other instanceof Document that
                && Objects.equals(value, that.value) && Objects.equals(type, that.type);
    }

    @Override public int hashCode() { return Objects.hash(value, type); }
}
