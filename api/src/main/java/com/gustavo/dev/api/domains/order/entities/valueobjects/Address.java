package com.gustavo.dev.api.domains.order.entities.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public final class Address {

    public static final class TextRule {
        public static final int MAX_LENGTH = 255;

        private TextRule() {
        }
    }

    @Column(name = "address_country", nullable = false, length = TextRule.MAX_LENGTH)
    private String country;

    @Column(name = "address_state", nullable = false, length = TextRule.MAX_LENGTH)
    private String state;

    @Column(name = "address_neighborhood", nullable = false, length = TextRule.MAX_LENGTH)
    private String neighborhood;

    @Column(name = "address_street", nullable = false, length = TextRule.MAX_LENGTH)
    private String street;

    @Column(name = "address_number", length = TextRule.MAX_LENGTH)
    private String number;

    @Column(name = "address_zip_code", nullable = false, length = TextRule.MAX_LENGTH)
    private String zipCode;

    /** Infrastructure-only constructor for Hibernate. */
    protected Address() {
    }

    private Address(
            final String country,
            final String state,
            final String neighborhood,
            final String street,
            final String number,
            final String zipCode
    ) {
        this.country = country;
        this.state = state;
        this.neighborhood = neighborhood;
        this.street = street;
        this.number = number;
        this.zipCode = zipCode;
    }

    public static Address of(
            final String country,
            final String state,
            final String neighborhood,
            final String street,
            final String number,
            final String zipCode
    ) {
        if (!isRequiredTextValid(country)
                || !isRequiredTextValid(state)
                || !isRequiredTextValid(neighborhood)
                || !isRequiredTextValid(street)
                || !isOptionalTextValid(number)
                || !isRequiredTextValid(zipCode)) {
            return null;
        }

        return new Address(country, state, neighborhood, street, number, zipCode);
    }

    public String country() { return country; }
    public String state() { return state; }
    public String neighborhood() { return neighborhood; }
    public String street() { return street; }
    public String number() { return number; }
    public String zipCode() { return zipCode; }

    private static boolean isRequiredTextValid(final String value) {
        return value != null && value.length() <= TextRule.MAX_LENGTH;
    }

    private static boolean isOptionalTextValid(final String value) {
        return value == null || value.length() <= TextRule.MAX_LENGTH;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (!(other instanceof Address that)) return false;
        return Objects.equals(country, that.country)
                && Objects.equals(state, that.state)
                && Objects.equals(neighborhood, that.neighborhood)
                && Objects.equals(street, that.street)
                && Objects.equals(number, that.number)
                && Objects.equals(zipCode, that.zipCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, state, neighborhood, street, number, zipCode);
    }
}
