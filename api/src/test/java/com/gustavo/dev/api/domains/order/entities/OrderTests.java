package com.gustavo.dev.api.domains.order.entities;

import com.gustavo.dev.api.domains.customer.entities.Customer;
import com.gustavo.dev.api.domains.order.entities.inputs.ImportOrderInput;
import com.gustavo.dev.api.domains.order.entities.valueobjects.Address;
import com.gustavo.dev.api.domains.products.entities.Product;
import com.gustavo.dev.api.domains.products.entities.valueobjects.SKU;
import com.gustavo.dev.domain.entities.inputs.ExecutionContext;
import com.gustavo.dev.tenant.inputs.TenantInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-11T12:00:00Z"), ZoneOffset.UTC);
    private static final OffsetDateTime PAST = OffsetDateTime.now(CLOCK).minusSeconds(1);
    private static final UUID CODE = UUID.randomUUID();
    private Customer customer;
    private ProductItem item;
    private Product product;
    private Address address;

    @BeforeEach
    void setUp() throws Exception {
        customer = Customer.createNew(context(), new ImportOrderInput.CustomerInput(
                "123", "PASSPORT", "Ada Lovelace", LocalDate.of(2000, 1, 1)));
        product = Product.createNew(context(), productInput("10.00", 1));
        item = ProductItem.createNew(context(), product, productInput("10.00", 1));
        address = Address.of("Portugal", "Lisbon", "Lisbon", "Alfama", "Main Street", null, "1000-001");
    }

    @Test
    void createsAValidOrderAndDefensivelyCopiesProducts() throws Exception {
        final var productInputs = new java.util.HashSet<>(Set.of(productInput("10.00", 1)));
        final var order = Order.createNew(context(), orderInput(
                "10.00", PAST, CODE, "Portugal", productInputs), customer, resolvedProducts(), CLOCK);
        productInputs.clear();

        assertNotNull(order);
        assertNotNull(order.id());
        assertEquals(1, order.products().size());
        assertThrows(UnsupportedOperationException.class, () -> order.products().clear());
    }

    @Test
    void rejectsEveryInvalidCreationBoundary() throws Exception {
        assertNull(Order.createNew(null, orderInput("0", PAST, CODE, "Portugal"), customer, resolvedProducts(), CLOCK));
        assertNull(Order.createNew(context(), null, customer, resolvedProducts(), CLOCK));
        assertNull(Order.createNew(context(), orderInput("0", PAST, CODE, "Portugal", null), customer, resolvedProducts(), CLOCK));
        assertNull(Order.createNew(context(), orderInput("0", PAST, CODE, "Portugal", Set.of()), customer, resolvedProducts(), CLOCK));
        assertNull(Order.createNew(context(), orderInput("0", PAST, CODE, "Portugal"), null, resolvedProducts(), CLOCK));
        assertNull(Order.createNew(context(), orderInput("0", PAST, CODE, "Portugal"), customer, Set.of(), CLOCK));
        assertNull(Order.createNew(context(), orderInput(null, PAST, CODE, "Portugal"), customer, resolvedProducts(), CLOCK));
        assertNull(Order.createNew(context(), orderInput("-0.01", PAST, CODE, "Portugal"), customer, resolvedProducts(), CLOCK));
        assertNull(Order.createNew(context(), orderInput("0", null, CODE, "Portugal"), customer, resolvedProducts(), CLOCK));
        assertNull(Order.createNew(context(), orderInput("0", OffsetDateTime.now(CLOCK), CODE, "Portugal"), customer, resolvedProducts(), CLOCK));
        assertNull(Order.createNew(context(), orderInput("0", PAST, CODE, null), customer, resolvedProducts(), CLOCK));
        assertNull(Order.createNew(context(), orderInput("0", PAST, null, "Portugal"), customer, resolvedProducts(), CLOCK));
        assertNull(Order.createNew(context(), orderInput("0", PAST, CODE, "Portugal"), customer, resolvedProducts(), null));
    }

    @Test
    void modificationsReturnNewInstancesPreserveIdentityAndLeaveOriginalUnchanged() throws Exception {
        final var original = Order.createNew(context(), orderInput("10.00", PAST, CODE, "Portugal"),
                customer, resolvedProducts(), CLOCK);

        final var changed = original.changeTotalValue(new BigDecimal("20.00"));

        assertNotSame(original, changed);
        assertEquals(original.id(), changed.id());
        assertEquals(new BigDecimal("20.00"), changed.totalValue());
        assertEquals(new BigDecimal("10.00"), original.totalValue());
    }

    @Test
    void invalidModificationsReturnNullAndLeaveOriginalUnchanged() throws Exception {
        final var original = Order.createNew(context(), orderInput("10.00", PAST, CODE, "Portugal"),
                customer, resolvedProducts(), CLOCK);

        assertNull(original.changeProducts(null));
        assertNull(original.changeProducts(Set.of()));
        assertNull(original.changeCustomer(null));
        assertNull(original.changeTotalValue(null));
        assertNull(original.changeTotalValue(new BigDecimal("-0.01")));
        assertNull(original.changePurchaseDate(OffsetDateTime.now(CLOCK), CLOCK));
        assertNull(original.changeAddress(null));
        assertEquals(new BigDecimal("10.00"), original.totalValue());
    }

    @Test
    void productItemValidatesCapturedPurchasePriceAndCopiesOnChange() throws Exception {
        final var product = item.product();
        assertNull(ProductItem.createNew(null, product, productInput("0", 1)));
        assertNull(ProductItem.createNew(context(), null, productInput("0", 1)));
        assertNull(ProductItem.createNew(context(), product, productInput(null, 1)));
        assertNull(ProductItem.createNew(context(), product, productInput("-0.01", 1)));
        assertNull(ProductItem.createNew(context(), product, productInput("0", 0)));

        final var changed = item.changePurchasePrice(new BigDecimal("12.00"));
        assertNotSame(item, changed);
        assertEquals(item.id(), changed.id());
        assertEquals(new BigDecimal("10.00"), item.purchasePrice());
        assertNull(item.changePurchasePrice(null));
    }

    private static ExecutionContext context() {
        return new ExecutionContext(OffsetDateTime.now(CLOCK), UUID.randomUUID(),
                new TenantInfo(UUID.randomUUID(), "test"), "test-user");
    }

    private static ImportOrderInput.ProductInput productInput(final String price, final int quantity) {
        return new ImportOrderInput.ProductInput("SKU-123", "Keyboard", price, quantity);
    }

    private static ImportOrderInput orderInput(
            final String total, final OffsetDateTime purchasedAt, final UUID code, final String country) {
        return orderInput(total, purchasedAt, code, country, Set.of(productInput("10.00", 1)));
    }

    private static ImportOrderInput orderInput(
            final String total, final OffsetDateTime purchasedAt, final UUID code, final String country,
            final Set<ImportOrderInput.ProductInput> products) {
        return new ImportOrderInput(context(), products,
                new ImportOrderInput.CustomerInput("123", "PASSPORT", "Ada Lovelace", LocalDate.of(2000, 1, 1)),
                purchasedAt, total, code, country, "Lisbon", "Lisbon", "Alfama", "Main Street", null, "1000-001");
    }

    private Set<Product> resolvedProducts() { return Set.of(product); }
}
