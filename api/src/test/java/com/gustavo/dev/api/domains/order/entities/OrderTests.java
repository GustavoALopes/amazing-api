package com.gustavo.dev.api.domains.order.entities;

import com.gustavo.dev.api.domains.customer.entities.Customer;
import com.gustavo.dev.api.domains.customer.entities.valueobjects.BirthDate;
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
    private Customer customer;
    private ProductItem item;
    private Address address;

    @BeforeEach
    void setUp() throws Exception {
        customer = Customer.createNew(context(), "Ada", "Lovelace",
                BirthDate.of(LocalDate.of(2000, 1, 1), CLOCK));
        final var product = Product.createNew(context(), SKU.of("SKU-123"), "Keyboard");
        item = ProductItem.createNew(context(), product, new BigDecimal("10.00"));
        address = Address.of("Portugal", "Lisbon", "Alfama", "Main Street", null, "1000-001");
    }

    @Test
    void createsAValidOrderAndDefensivelyCopiesProducts() throws Exception {
        final var products = new java.util.HashSet<>(Set.of(item));
        final var order = Order.createNew(context(), products, customer, new BigDecimal("10.00"), PAST, address, CLOCK);
        products.clear();

        assertNotNull(order);
        assertNotNull(order.id());
        assertEquals(Set.of(item), order.products());
        assertThrows(UnsupportedOperationException.class, () -> order.products().clear());
    }

    @Test
    void rejectsEveryInvalidCreationBoundary() throws Exception {
        assertNull(Order.createNew(null, Set.of(item), customer, BigDecimal.ZERO, PAST, address, CLOCK));
        assertNull(Order.createNew(context(), null, customer, BigDecimal.ZERO, PAST, address, CLOCK));
        assertNull(Order.createNew(context(), Set.of(), customer, BigDecimal.ZERO, PAST, address, CLOCK));
        assertNull(Order.createNew(context(), Set.of(item), null, BigDecimal.ZERO, PAST, address, CLOCK));
        assertNull(Order.createNew(context(), Set.of(item), customer, null, PAST, address, CLOCK));
        assertNull(Order.createNew(context(), Set.of(item), customer, new BigDecimal("-0.01"), PAST, address, CLOCK));
        assertNull(Order.createNew(context(), Set.of(item), customer, BigDecimal.ZERO, null, address, CLOCK));
        assertNull(Order.createNew(context(), Set.of(item), customer, BigDecimal.ZERO,
                OffsetDateTime.now(CLOCK), address, CLOCK));
        assertNull(Order.createNew(context(), Set.of(item), customer, BigDecimal.ZERO, PAST, null, CLOCK));
        assertNull(Order.createNew(context(), Set.of(item), customer, BigDecimal.ZERO, PAST, address, null));
    }

    @Test
    void modificationsReturnNewInstancesPreserveIdentityAndLeaveOriginalUnchanged() throws Exception {
        final var original = Order.createNew(context(), Set.of(item), customer,
                new BigDecimal("10.00"), PAST, address, CLOCK);

        final var changed = original.changeTotalValue(new BigDecimal("20.00"));

        assertNotSame(original, changed);
        assertEquals(original.id(), changed.id());
        assertEquals(new BigDecimal("20.00"), changed.totalValue());
        assertEquals(new BigDecimal("10.00"), original.totalValue());
    }

    @Test
    void invalidModificationsReturnNullAndLeaveOriginalUnchanged() throws Exception {
        final var original = Order.createNew(context(), Set.of(item), customer,
                new BigDecimal("10.00"), PAST, address, CLOCK);

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
        assertNull(ProductItem.createNew(null, product, BigDecimal.ZERO));
        assertNull(ProductItem.createNew(context(), null, BigDecimal.ZERO));
        assertNull(ProductItem.createNew(context(), product, null));
        assertNull(ProductItem.createNew(context(), product, new BigDecimal("-0.01")));

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
}
