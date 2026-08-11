package com.gustavo.dev.templates.domain.entities;

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

/**
 * Sample aggregate root implementing the domain entity ADRs DE-001 through DE-005.
 */
@Entity
@Table(name = "simple_aggregate_roots")
public final class SimpleAggregateRoot extends BaseEntity<UUID> implements IAggregateRoot {

    /** Domain metadata for the first-name rule. */
    public static final class FirstNameRule {
        public static final int MAX_LENGTH = 255;

        private FirstNameRule() {
        }
    }

    /** Domain metadata for the last-name rule. */
    public static final class LastNameRule {
        public static final int MAX_LENGTH = 255;

        private LastNameRule() {
        }
    }

    @Column(name = "first_name", nullable = false, length = FirstNameRule.MAX_LENGTH)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = LastNameRule.MAX_LENGTH)
    private String lastName;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "birth_date", nullable = false)
    )
    private BirthDate birthDate;

    /**
     * Infrastructure-only constructor for Hibernate. Do not call from domain code.
     */
    protected SimpleAggregateRoot() {
        super();
    }

    private SimpleAggregateRoot(
            final String firstName,
            final String lastName,
            final BirthDate birthDate
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    /**
     * Creates a valid aggregate or returns {@code null} when any rule fails.
     */
    public static SimpleAggregateRoot createNew(
            final ExecutionContext executionContext,
            final String firstName,
            final String lastName,
            final BirthDate birthDate
    ) throws Exception {
        if (executionContext == null || !isValid(firstName, lastName, birthDate)) {
            return null;
        }

        final var entity = new SimpleAggregateRoot(firstName, lastName, birthDate);
        entity.baseCreateNew(executionContext, UuidProvider::getV7);
        return entity;
    }

    /**
     * Returns a new valid instance with the same entity identity.
     */
    public SimpleAggregateRoot changeFirstName(final String newFirstName) {
        if (!isValidFirstName(newFirstName)) {
            return null;
        }

        return copyOf(newFirstName, lastName, birthDate);
    }

    /**
     * Returns a new valid instance with the same entity identity.
     */
    public SimpleAggregateRoot changeLastName(final String newLastName) {
        if (!isValidLastName(newLastName)) {
            return null;
        }

        return copyOf(firstName, newLastName, birthDate);
    }

    /**
     * Returns a new valid instance with the same entity identity.
     */
    public SimpleAggregateRoot changeBirthDate(final BirthDate newBirthDate) {
        if (newBirthDate == null) {
            return null;
        }

        return copyOf(firstName, lastName, newBirthDate);
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

    private SimpleAggregateRoot copyOf(
            final String newFirstName,
            final String newLastName,
            final BirthDate newBirthDate
    ) {
        final var copy = new SimpleAggregateRoot(newFirstName, newLastName, newBirthDate);
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
        return isValidFirstName(firstName)
                && isValidLastName(lastName)
                && birthDate != null;
    }

    private static boolean isValidFirstName(final String firstName) {
        return firstName != null && firstName.length() <= FirstNameRule.MAX_LENGTH;
    }

    private static boolean isValidLastName(final String lastName) {
        return lastName != null && lastName.length() <= LastNameRule.MAX_LENGTH;
    }
}
