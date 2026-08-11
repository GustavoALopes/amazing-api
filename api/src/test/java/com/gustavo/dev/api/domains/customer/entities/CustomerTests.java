package com.gustavo.dev.api.domains.customer.entities;

import com.gustavo.dev.api.domains.customer.entities.valueobjects.BirthDate;
import com.gustavo.dev.domain.entities.inputs.ExecutionContext;
import com.gustavo.dev.domain.entities.interfaces.IAggregateRoot;
import com.gustavo.dev.tenant.inputs.TenantInfo;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-11T12:00:00Z"), ZoneOffset.UTC);
    private static final BirthDate BIRTH_DATE = BirthDate.of(LocalDate.of(2000, 1, 1), CLOCK);

    @Test
    void createsAValidCustomer() throws Exception {
        final var customer = Customer.createNew(context(), "Ada", "Lovelace", BIRTH_DATE);

        assertNotNull(customer);
        assertInstanceOf(IAggregateRoot.class, customer);
        assertNotNull(customer.id());
        assertEquals("Ada", customer.firstName());
        assertEquals("Lovelace", customer.lastName());
        assertEquals(BIRTH_DATE, customer.birthDate());
    }

    @Test
    void rejectsInvalidCreationInputsAndLengthBoundary() throws Exception {
        assertNull(Customer.createNew(null, "Ada", "Lovelace", BIRTH_DATE));
        assertNull(Customer.createNew(context(), null, "Lovelace", BIRTH_DATE));
        assertNull(Customer.createNew(context(), "Ada", null, BIRTH_DATE));
        assertNull(Customer.createNew(context(), "Ada", "Lovelace", null));
        assertNotNull(Customer.createNew(context(), "a".repeat(254), "b".repeat(254), BIRTH_DATE));
        assertNull(Customer.createNew(context(), "a".repeat(255), "Lovelace", BIRTH_DATE));
        assertNull(Customer.createNew(context(), "Ada", "b".repeat(255), BIRTH_DATE));
    }

    @Test
    void modificationsReturnNewInstancesAndPreserveIdentityAndOriginal() throws Exception {
        final var original = Customer.createNew(context(), "Ada", "Lovelace", BIRTH_DATE);
        final var changedBirthDate = BirthDate.of(LocalDate.of(2001, 2, 3), CLOCK);

        final var renamed = original.changeFirstName("Grace");
        final var relastNamed = original.changeLastName("Byron");
        final var reborn = original.changeBirthDate(changedBirthDate);

        assertNotSame(original, renamed);
        assertEquals(original.id(), renamed.id());
        assertEquals("Grace", renamed.firstName());
        assertEquals("Byron", relastNamed.lastName());
        assertEquals(changedBirthDate, reborn.birthDate());
        assertEquals("Ada", original.firstName());
        assertEquals("Lovelace", original.lastName());
        assertEquals(BIRTH_DATE, original.birthDate());
    }

    @Test
    void invalidModificationsReturnNullAndLeaveOriginalUnchanged() throws Exception {
        final var original = Customer.createNew(context(), "Ada", "Lovelace", BIRTH_DATE);

        assertNull(original.changeFirstName(null));
        assertNull(original.changeFirstName("a".repeat(255)));
        assertNull(original.changeLastName(null));
        assertNull(original.changeLastName("b".repeat(255)));
        assertNull(original.changeBirthDate(null));
        assertEquals("Ada", original.firstName());
        assertEquals("Lovelace", original.lastName());
        assertEquals(BIRTH_DATE, original.birthDate());
    }

    private static ExecutionContext context() {
        return new ExecutionContext(
                OffsetDateTime.now(CLOCK),
                UUID.randomUUID(),
                new TenantInfo(UUID.randomUUID(), "test"),
                "test-user"
        );
    }
}
