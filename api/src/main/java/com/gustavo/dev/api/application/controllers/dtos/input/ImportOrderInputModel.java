package com.gustavo.dev.api.application.controllers.dtos.input;

import com.gustavo.dev.api.domains.order.entities.inputs.ImportOrderInput;
import com.gustavo.dev.domain.entities.inputs.ExecutionContext;
import com.gustavo.dev.tenant.TenantContext;
import com.gustavo.dev.tenant.inputs.TenantInfo;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record ImportOrderInputModel(
        Set<Product> products,
        Customer customer,
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
    public record Product(String skuCode, String name, String price, int quantity) { }
    public record Customer(
            String customerDocument,
            String customerDocumentType,
            String name,
            LocalDate birthdate
    ) { }

    public ImportOrderInput toDomain() {
        final var tenantId = TenantContext.getTenantId();
        final var tenant = new TenantInfo(parseTenantId(tenantId), tenantId);
        final var context = ExecutionContext.Create(UUID.randomUUID(), tenant, "order-import");
        final Set<ImportOrderInput.ProductInput> domainProducts = products == null ? null
                : products.stream().map(product -> product == null ? null : new ImportOrderInput.ProductInput(
                        product.skuCode(), product.name(), product.price(), product.quantity()))
                .collect(Collectors.toSet());
        final var domainCustomer = customer == null ? null : new ImportOrderInput.CustomerInput(
                customer.customerDocument(), customer.customerDocumentType(), customer.name(), customer.birthdate());
        return new ImportOrderInput(context, domainProducts, domainCustomer, purchasedAt, totalValue, code,
                country, state, city, neighborn, street, number, zipcode);
    }

    private static UUID parseTenantId(final String value) {
        if (value == null) return new UUID(0, 0);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
        }
    }
}
