package com.gustavo.dev.templates.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable value object representing a person's birth date.
 *
 * <p>The public factories return {@code null} when validation fails, as required
 * by DE-003. The protected no-argument constructor exists only for Hibernate.</p>
 */
@Embeddable
public final class BirthDate {

    @Column(name = "birth_date", nullable = false)
    private LocalDate value;

    /**
     * Infrastructure-only constructor for Hibernate. Do not call from domain code.
     */
    protected BirthDate() {
    }

    private BirthDate(final LocalDate value) {
        this.value = value;
    }

    public static BirthDate of(final LocalDate value) {
        return of(value, Clock.systemDefaultZone());
    }

    /**
     * Clock-aware overload keeps date validation deterministic in tests.
     */
    public static BirthDate of(final LocalDate value, final Clock clock) {
        if (value == null || clock == null) {
            return null;
        }

        if (value.isAfter(LocalDate.now(clock))) {
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
        if (!(other instanceof BirthDate that)) {
            return false;
        }
        return Objects.equals(value, that.value);
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
