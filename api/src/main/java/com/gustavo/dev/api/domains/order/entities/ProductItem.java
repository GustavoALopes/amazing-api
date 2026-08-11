package com.gustavo.dev.api.domains.order.entities;

import com.gustavo.dev.api.domains.products.entities.Product;
import com.gustavo.dev.api.domains.order.entities.inputs.ImportOrderInput;
import com.gustavo.dev.domain.entities.BaseEntity;
import com.gustavo.dev.domain.entities.inputs.ExecutionContext;
import com.gustavo.dev.uuid.UuidProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_item")
public final class ProductItem extends BaseEntity<UUID> {

    public static final class PurchasePriceRule {
        public static final BigDecimal MIN_VALUE = BigDecimal.ZERO;

        private PurchasePriceRule() {
        }
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "purchase_price", nullable = false)
    private BigDecimal purchasePrice;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    /** Infrastructure-only constructor for Hibernate. */
    protected ProductItem() {
        super();
    }

    private ProductItem(final Product product, final BigDecimal purchasePrice, final int quantity) {
        this.product = product;
        this.purchasePrice = purchasePrice;
        this.quantity = quantity;
    }

    public static ProductItem createNew(
            final ExecutionContext executionContext,
            final Product product,
            final ImportOrderInput.ProductInput input
    ) throws Exception {
        final var purchasePrice = parsePrice(input == null ? null : input.price());
        final var quantity = input == null ? 0 : input.quantity();
        if (executionContext == null || product == null || !isValidPrice(purchasePrice) || quantity <= 0) {
            return null;
        }

        final var item = new ProductItem(product, purchasePrice, quantity);
        item.baseCreateNew(executionContext, UuidProvider::getV7);
        return item;
    }

    public ProductItem changePurchasePrice(final BigDecimal newPurchasePrice) {
        return isValidPrice(newPurchasePrice) ? copyOf(product, newPurchasePrice, quantity) : null;
    }

    public UUID id() { return id; }
    public Product product() { return product; }
    public BigDecimal purchasePrice() { return purchasePrice; }
    public int quantity() { return quantity; }

    private ProductItem copyOf(final Product newProduct, final BigDecimal newPurchasePrice, final int newQuantity) {
        final var copy = new ProductItem(newProduct, newPurchasePrice, newQuantity);
        copy.id = id;
        copy.auditInfo = auditInfo;
        copy.correlationId = correlationId;
        return copy;
    }

    private static boolean isValidPrice(final BigDecimal value) {
        return value != null && value.compareTo(PurchasePriceRule.MIN_VALUE) >= 0;
    }

    private static BigDecimal parsePrice(final String value) {
        try { return value == null ? null : new BigDecimal(value); }
        catch (NumberFormatException exception) { return null; }
    }
}
