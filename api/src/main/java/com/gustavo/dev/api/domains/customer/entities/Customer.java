package com.gustavo.dev.api.domains.customer.entities;

import com.gustavo.dev.api.domains.customer.entities.valueobjects.BirthDate;
import com.gustavo.dev.domain.entities.BaseEntity;
import com.gustavo.dev.domain.entities.inputs.ExecutionContext;
import com.gustavo.dev.domain.entities.interfaces.IAggregateRoot;
import com.gustavo.dev.uuid.UuidProvider;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "Customers")
public final class Customer extends BaseEntity<UUID> implements IAggregateRoot {

    public static final class FirstNameRule {
        public static final int MAX_LENGTH_EXCLUSIVE = 255;

        private FirstNameRule() {
        }
    }

    public static final class LastNameRule {
        public static final int MAX_LENGTH_EXCLUSIVE = 255;

        private LastNameRule() {
        }
    }

    @Column(name = "first_name", nullable = false, length = FirstNameRule.MAX_LENGTH_EXCLUSIVE - 1)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = LastNameRule.MAX_LENGTH_EXCLUSIVE - 1)
    private String lastName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "birth_date", nullable = false))
    private BirthDate birthDate;

    /** Infrastructure-only constructor for Hibernate. */
    protected Customer() {
        super();
    }

    private Customer(final String firstName, final String lastName, final BirthDate birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    public static Customer createNew(
            final ExecutionContext executionContext,
            final String firstName,
            final String lastName,
            final BirthDate birthDate
    ) throws Exception {
        if (executionContext == null || !isValid(firstName, lastName, birthDate)) {
            return null;
        }

        final var customer = new Customer(firstName, lastName, birthDate);
        customer.baseCreateNew(executionContext, UuidProvider::getV7);
        return customer;
    }

    public Customer changeFirstName(final String newFirstName) {
        return isValidFirstName(newFirstName) ? copyOf(newFirstName, lastName, birthDate) : null;
    }

    public Customer changeLastName(final String newLastName) {
        return isValidLastName(newLastName) ? copyOf(firstName, newLastName, birthDate) : null;
    }

    public Customer changeBirthDate(final BirthDate newBirthDate) {
        return newBirthDate != null ? copyOf(firstName, lastName, newBirthDate) : null;
    }

    public UUID id() {
        return id;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public BirthDate birthDate() {
        return birthDate;
    }

    private Customer copyOf(
            final String newFirstName,
            final String newLastName,
            final BirthDate newBirthDate
    ) {
        final var copy = new Customer(newFirstName, newLastName, newBirthDate);
        copy.id = id;
        copy.auditInfo = auditInfo;
        copy.correlationId = correlationId;
        return copy;
    }

    private static boolean isValid(
            final String firstName,
            final String lastName,
            final BirthDate birthDate
    ) {
        return isValidFirstName(firstName) && isValidLastName(lastName) && birthDate != null;
    }

    private static boolean isValidFirstName(final String firstName) {
        return firstName != null && firstName.length() < FirstNameRule.MAX_LENGTH_EXCLUSIVE;
    }

    private static boolean isValidLastName(final String lastName) {
        return lastName != null && lastName.length() < LastNameRule.MAX_LENGTH_EXCLUSIVE;
    }
}
