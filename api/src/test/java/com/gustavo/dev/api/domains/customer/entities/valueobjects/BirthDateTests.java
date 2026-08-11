package com.gustavo.dev.api.domains.customer.entities.valueobjects;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class BirthDateTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-11T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void acceptsTodayAndEarlierDates() {
        assertNotNull(BirthDate.of(LocalDate.of(2026, 8, 11), CLOCK));
        assertNotNull(BirthDate.of(LocalDate.of(2000, 1, 1), CLOCK));
    }

    @Test
    void rejectsNullAndFutureDates() {
        assertNull(BirthDate.of(null, CLOCK));
        assertNull(BirthDate.of(LocalDate.of(2026, 8, 12), CLOCK));
        assertNull(BirthDate.of(LocalDate.of(2000, 1, 1), null));
    }

    @Test
    void hasValueEqualityAndConsistentHashCode() {
        final var first = BirthDate.of(LocalDate.of(2000, 1, 1), CLOCK);
        final var second = BirthDate.of(LocalDate.of(2000, 1, 1), CLOCK);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
