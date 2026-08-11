package com.gustavo.dev.api.domains.customer.entities.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/** A customer's birth date, which cannot be in the future. */
@Embeddable
public final class BirthDate {

    @Column(name = "birth_date", nullable = false)
    private LocalDate value;

    /** Infrastructure-only constructor for Hibernate. */
    protected BirthDate() {
    }

    private BirthDate(final LocalDate value) {
        this.value = value;
    }

    public static BirthDate of(final LocalDate value) {
        return of(value, Clock.systemDefaultZone());
    }

    public static BirthDate of(final LocalDate value, final Clock clock) {
        if (value == null || clock == null || value.isAfter(LocalDate.now(clock))) {
            return null;
        }

        return new BirthDate(value);
    }

    public LocalDate value() {
        return value;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof BirthDate that && Objects.equals(value, that.value);
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
