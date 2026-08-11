package com.gustavo.dev.api.domains.products.entities.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/** A product's stock-keeping unit. */
@Embeddable
public final class SKU {

    public static final class ValueRule {
        public static final int MAX_LENGTH_EXCLUSIVE = 50;

        private ValueRule() {
        }
    }

    @Column(name = "sku", nullable = false, length = ValueRule.MAX_LENGTH_EXCLUSIVE - 1)
    private String value;

    /** Infrastructure-only constructor for Hibernate. */
    protected SKU() {
    }

    private SKU(final String value) {
        this.value = value;
    }

    public static SKU of(final String value) {
        if (value == null || value.length() >= ValueRule.MAX_LENGTH_EXCLUSIVE) {
            return null;
        }

        return new SKU(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof SKU that && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
