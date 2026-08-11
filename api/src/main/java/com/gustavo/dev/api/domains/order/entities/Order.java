package com.gustavo.dev.api.domains.order.entities;

import com.gustavo.dev.api.domains.customer.entities.Customer;
import com.gustavo.dev.api.domains.order.entities.valueobjects.Address;
import com.gustavo.dev.api.domains.order.entities.inputs.ImportOrderInput;
import com.gustavo.dev.api.domains.products.entities.Product;
import com.gustavo.dev.domain.entities.BaseEntity;
import com.gustavo.dev.domain.entities.inputs.ExecutionContext;
import com.gustavo.dev.domain.entities.interfaces.IAggregateRoot;
import com.gustavo.dev.uuid.UuidProvider;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Entity
@Table(name = "orders")
public final class Order extends BaseEntity<UUID> implements IAggregateRoot {

    public static final class ProductsRule {
        public static final int MIN_SIZE = 1;

        private ProductsRule() {
        }
    }

    public static final class TotalValueRule {
        public static final BigDecimal MIN_VALUE = BigDecimal.ZERO;

        private TotalValueRule() {
        }
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private Set<ProductItem> products;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "total_value", nullable = false)
    private BigDecimal totalValue;

    @Column(name = "purchase_date", nullable = false)
    private OffsetDateTime purchaseDate;

    @Embedded
    private Address address;

    @Column(name = "code", nullable = false, unique = true)
    private UUID code;

    /** Infrastructure-only constructor for Hibernate. */
    protected Order() {
        super();
    }

    private Order(
            final Set<ProductItem> products,
            final Customer customer,
            final BigDecimal totalValue,
            final OffsetDateTime purchaseDate,
            final Address address,
            final UUID code
    ) {
        this.products = Set.copyOf(products);
        this.customer = customer;
        this.totalValue = totalValue;
        this.purchaseDate = purchaseDate;
        this.address = address;
        this.code = code;
    }

    public static Order createNew(
            final ExecutionContext executionContext,
            final ImportOrderInput input,
            final Customer customer,
            final Set<Product> products
    ) throws Exception {
        return createNew(executionContext, input, customer, products,
                Clock.systemDefaultZone());
    }

    public static Order createNew(
            final ExecutionContext executionContext,
            final ImportOrderInput input,
            final Customer customer,
            final Set<Product> resolvedProducts,
            final Clock clock
    ) throws Exception {
        if (input == null) return null;
        final var products = createProductItems(executionContext, input, resolvedProducts);
        final var totalValue = parseTotalValue(input.totalValue());
        final var purchaseDate = input.purchasedAt();
        final var address = Address.of(input.country(), input.state(), input.city(), input.neighborn(),
                input.street(), input.number(), input.zipcode());
        final var code = input.code();
        if (executionContext == null || code == null || !isValid(products, customer, totalValue, purchaseDate, address, clock)) {
            return null;
        }

        final var order = new Order(products, customer, totalValue, purchaseDate, address, code);
        order.baseCreateNew(executionContext, UuidProvider::getV7);
        return order;
    }

    private static Set<ProductItem> createProductItems(
            final ExecutionContext executionContext,
            final ImportOrderInput input,
            final Set<Product> resolvedProducts
    ) throws Exception {
        if (input.products() == null || resolvedProducts == null) return null;
        final var items = new HashSet<ProductItem>();
        for (final var productInput : input.products()) {
            if (productInput == null) continue;
            final var product = resolvedProducts.stream()
                    .filter(candidate -> candidate.sku().value().equals(productInput.skuCode()))
                    .findFirst()
                    .orElse(null);
            final var item = ProductItem.createNew(
                    executionContext, product, productInput);
            if (item != null) items.add(item);
        }
        return items;
    }

    private static BigDecimal parseTotalValue(final String value) {
        try { return value == null ? null : new BigDecimal(value); }
        catch (NumberFormatException exception) { return null; }
    }

    public Order changeProducts(final Set<ProductItem> newProducts) {
        return isValidProducts(newProducts) ? copyOf(newProducts, customer, totalValue, purchaseDate, address, code) : null;
    }

    public Order changeCustomer(final Customer newCustomer) {
        return newCustomer != null ? copyOf(products, newCustomer, totalValue, purchaseDate, address, code) : null;
    }

    public Order changeTotalValue(final BigDecimal newTotalValue) {
        return isValidTotalValue(newTotalValue)
                ? copyOf(products, customer, newTotalValue, purchaseDate, address, code) : null;
    }

    public Order changePurchaseDate(final OffsetDateTime newPurchaseDate) {
        return changePurchaseDate(newPurchaseDate, Clock.systemDefaultZone());
    }

    public Order changePurchaseDate(final OffsetDateTime newPurchaseDate, final Clock clock) {
        return isValidPurchaseDate(newPurchaseDate, clock)
                ? copyOf(products, customer, totalValue, newPurchaseDate, address, code) : null;
    }

    public Order changeAddress(final Address newAddress) {
        return newAddress != null ? copyOf(products, customer, totalValue, purchaseDate, newAddress, code) : null;
    }

    public UUID id() { return id; }
    public Set<ProductItem> products() { return Set.copyOf(products); }
    public Customer customer() { return customer; }
    public BigDecimal totalValue() { return totalValue; }
    public OffsetDateTime purchaseDate() { return purchaseDate; }
    public Address address() { return address; }
    public UUID code() { return code; }

    private Order copyOf(
            final Set<ProductItem> newProducts,
            final Customer newCustomer,
            final BigDecimal newTotalValue,
            final OffsetDateTime newPurchaseDate,
            final Address newAddress,
            final UUID newCode
    ) {
        final var copy = new Order(newProducts, newCustomer, newTotalValue, newPurchaseDate, newAddress, newCode);
        copy.id = id;
        copy.auditInfo = auditInfo;
        copy.correlationId = correlationId;
        return copy;
    }

    private static boolean isValid(
            final Set<ProductItem> products,
            final Customer customer,
            final BigDecimal totalValue,
            final OffsetDateTime purchaseDate,
            final Address address,
            final Clock clock
    ) {
        return isValidProducts(products)
                && customer != null
                && isValidTotalValue(totalValue)
                && isValidPurchaseDate(purchaseDate, clock)
                && address != null;
    }

    private static boolean isValidProducts(final Set<ProductItem> products) {
        return products != null
                && products.size() >= ProductsRule.MIN_SIZE
                && products.stream().noneMatch(java.util.Objects::isNull);
    }

    private static boolean isValidTotalValue(final BigDecimal value) {
        return value != null && value.compareTo(TotalValueRule.MIN_VALUE) >= 0;
    }

    private static boolean isValidPurchaseDate(final OffsetDateTime value, final Clock clock) {
        return value != null && clock != null && value.isBefore(OffsetDateTime.now(clock));
    }
}
