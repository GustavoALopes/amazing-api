package com.gustavo.dev.api.domains.order.entities.inputs;

import com.gustavo.dev.domain.entities.inputs.ExecutionContext;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record ImportOrderInput(
        ExecutionContext executionContext,
        Set<ProductInput> products,
        CustomerInput customer,
        OffsetDateTime purchasedAt,
        String totalValue,
        UUID code,
        String country,
        String state,
        String city,
        String neighborn,
        String street,
        String number,
        String zipcode
) {
    public record ProductInput(String skuCode, String name, String price, int quantity) { }
    public record CustomerInput(
            String customerDocument,
            String customerDocumentType,
            String name,
            LocalDate birthdate
    ) {
        public String firstName() {
            final var parts = splitName();
            return parts == null ? null : parts[0];
        }

        public String lastName() {
            final var parts = splitName();
            return parts == null ? null : parts[1];
        }

        private String[] splitName() {
            if (name == null) return null;
            final var normalized = name.trim();
            final var separator = normalized.indexOf(' ');
            if (separator <= 0 || separator == normalized.length() - 1) return null;
            return new String[]{normalized.substring(0, separator), normalized.substring(separator + 1).trim()};
        }
    }
}
