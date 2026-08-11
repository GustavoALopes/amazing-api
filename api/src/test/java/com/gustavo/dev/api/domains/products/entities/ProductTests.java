package com.gustavo.dev.api.domains.products.entities;

import com.gustavo.dev.api.domains.products.entities.valueobjects.SKU;
import com.gustavo.dev.domain.entities.inputs.ExecutionContext;
import com.gustavo.dev.domain.entities.interfaces.IAggregateRoot;
import com.gustavo.dev.tenant.inputs.TenantInfo;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductTests {

    private static final SKU SKU_VALUE = SKU.of("SKU-123");

    @Test
    void createsAValidProduct() throws Exception {
        final var product = Product.createNew(context(), SKU_VALUE, "Keyboard");

        assertNotNull(product);
        assertInstanceOf(IAggregateRoot.class, product);
        assertNotNull(product.id());
        assertEquals(SKU_VALUE, product.sku());
        assertEquals("Keyboard", product.name());
    }

    @Test
    void rejectsInvalidCreationInputsAndNameLengthBoundary() throws Exception {
        assertNull(Product.createNew(null, SKU_VALUE, "Keyboard"));
        assertNull(Product.createNew(context(), null, "Keyboard"));
        assertNull(Product.createNew(context(), SKU_VALUE, null));
        assertNotNull(Product.createNew(context(), SKU_VALUE, "a".repeat(254)));
        assertNull(Product.createNew(context(), SKU_VALUE, "a".repeat(255)));
    }

    @Test
    void modificationsReturnNewInstancesAndPreserveIdentityAndOriginal() throws Exception {
        final var original = Product.createNew(context(), SKU_VALUE, "Keyboard");
        final var replacementSku = SKU.of("SKU-456");

        final var reidentified = original.changeSku(replacementSku);
        final var renamed = original.changeName("Mouse");

        assertNotSame(original, reidentified);
        assertNotSame(original, renamed);
        assertEquals(original.id(), reidentified.id());
        assertEquals(original.id(), renamed.id());
        assertEquals(replacementSku, reidentified.sku());
        assertEquals("Mouse", renamed.name());
        assertEquals(SKU_VALUE, original.sku());
        assertEquals("Keyboard", original.name());
    }

    @Test
    void invalidModificationsReturnNullAndLeaveOriginalUnchanged() throws Exception {
        final var original = Product.createNew(context(), SKU_VALUE, "Keyboard");

        assertNull(original.changeSku(null));
        assertNull(original.changeName(null));
        assertNull(original.changeName("a".repeat(255)));
        assertEquals(SKU_VALUE, original.sku());
        assertEquals("Keyboard", original.name());
    }

    private static ExecutionContext context() {
        return new ExecutionContext(
                OffsetDateTime.now(),
                UUID.randomUUID(),
                new TenantInfo(UUID.randomUUID(), "test"),
                "test-user"
        );
    }
}
