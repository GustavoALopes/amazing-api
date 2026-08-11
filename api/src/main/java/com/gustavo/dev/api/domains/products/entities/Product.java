package com.gustavo.dev.api.domains.products.entities;

import com.gustavo.dev.api.domains.products.entities.valueobjects.SKU;
import com.gustavo.dev.api.domains.order.entities.inputs.ImportOrderInput;
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
@Table(name = "products")
public final class Product extends BaseEntity<UUID> implements IAggregateRoot {

    public static final class NameRule {
        public static final int MAX_LENGTH_EXCLUSIVE = 255;

        private NameRule() {
        }
    }

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "sku", nullable = false, unique = true,
                    length = SKU.ValueRule.MAX_LENGTH_EXCLUSIVE - 1)
    )
    private SKU sku;

    @Column(name = "name", nullable = false, length = NameRule.MAX_LENGTH_EXCLUSIVE - 1)
    private String name;

    /** Infrastructure-only constructor for Hibernate. */
    protected Product() {
        super();
    }

    private Product(final SKU sku, final String name) {
        this.sku = sku;
        this.name = name;
    }

    public static Product createNew(
            final ExecutionContext executionContext,
            final ImportOrderInput.ProductInput input
    ) throws Exception {
        if (executionContext == null || input == null) {
            return null;
        }

        final var sku = SKU.of(input.skuCode());
        final var name = input.name();
        if (!isValid(sku, name)) return null;

        final var product = new Product(sku, name);
        product.baseCreateNew(executionContext, UuidProvider::getV7);
        return product;
    }

    public Product changeSku(final SKU newSku) {
        return newSku != null ? copyOf(newSku, name) : null;
    }

    public Product changeName(final String newName) {
        return isValidName(newName) ? copyOf(sku, newName) : null;
    }

    public UUID id() {
        return id;
    }

    public SKU sku() {
        return sku;
    }

    public String name() {
        return name;
    }

    private Product copyOf(final SKU newSku, final String newName) {
        final var copy = new Product(newSku, newName);
        copy.id = id;
        copy.auditInfo = auditInfo;
        copy.correlationId = correlationId;
        return copy;
    }

    private static boolean isValid(final SKU sku, final String name) {
        return sku != null && isValidName(name);
    }

    private static boolean isValidName(final String name) {
        return name != null && name.length() < NameRule.MAX_LENGTH_EXCLUSIVE;
    }
}
