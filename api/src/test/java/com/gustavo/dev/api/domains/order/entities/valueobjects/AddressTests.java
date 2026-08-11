package com.gustavo.dev.api.domains.order.entities.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AddressTests {

    @Test
    void createsAValidAddressIncludingANullNumber() {
        final var address = Address.of("Portugal", "Lisbon", "Lisbon", "Alfama", "Main Street", null, "1000-001");

        assertNotNull(address);
        assertEquals("Portugal", address.country());
        assertNull(address.number());
    }

    @Test
    void rejectsNullRequiredFieldsAndValuesOverTheLengthBoundary() {
        assertNull(Address.of(null, "state", "city", "neighborhood", "street", "1", "zip"));
        assertNull(Address.of("country", null, "city", "neighborhood", "street", "1", "zip"));
        assertNull(Address.of("country", "state", null, "neighborhood", "street", "1", "zip"));
        assertNull(Address.of("country", "state", "city", null, "street", "1", "zip"));
        assertNull(Address.of("country", "state", "city", "neighborhood", null, "1", "zip"));
        assertNull(Address.of("country", "state", "city", "neighborhood", "street", "1", null));
        assertNotNull(Address.of("a".repeat(255), "state", "city", "neighborhood", "street", "1", "zip"));
        assertNull(Address.of("a".repeat(256), "state", "city", "neighborhood", "street", "1", "zip"));
        assertNull(Address.of("country", "state", "city", "neighborhood", "street", "1".repeat(256), "zip"));
    }

    @Test
    void implementsValueEquality() {
        final var first = Address.of("country", "state", "city", "neighborhood", "street", "1", "zip");
        final var second = Address.of("country", "state", "city", "neighborhood", "street", "1", "zip");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
