package com.gustavo.dev.api.domains.products.entities.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SKUTests {

    @Test
    void acceptsValuesBelowTheExclusiveLengthLimit() {
        assertNotNull(SKU.of("A"));
        assertNotNull(SKU.of("a".repeat(49)));
    }

    @Test
    void rejectsNullAndTheExclusiveLengthBoundary() {
        assertNull(SKU.of(null));
        assertNull(SKU.of("a".repeat(50)));
    }

    @Test
    void hasValueEqualityAndConsistentHashCode() {
        final var first = SKU.of("SKU-123");
        final var second = SKU.of("SKU-123");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals("SKU-123", first.toString());
    }
}
