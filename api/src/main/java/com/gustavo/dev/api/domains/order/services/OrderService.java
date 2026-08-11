package com.gustavo.dev.api.domains.order.services;

import com.gustavo.dev.api.domains.customer.entities.Customer;
import com.gustavo.dev.api.domains.order.entities.Order;
import com.gustavo.dev.api.domains.order.entities.inputs.ImportOrderInput;
import com.gustavo.dev.api.domains.order.repositories.IOrderRepository;
import com.gustavo.dev.api.domains.products.entities.Product;
import com.gustavo.dev.domain.entities.inputs.Message;
import com.gustavo.dev.observation.interfaces.IMetricsPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public final class OrderService {
    private static final String IMPORTED_ORDERS_METRIC = "orders.imported";
    private static final Map<String, String> IMPORT_ORDER_TAGS = Map.of(
            "service", "amazing-api",
            "usecase", "importOrder"
    );

    private final IOrderRepository orders;
    private final IMetricsPublisher metricsPublisher;

    public OrderService(
            final IOrderRepository orders,
            final IMetricsPublisher metricsPublisher
    ) {
        this.orders = orders;
        this.metricsPublisher = metricsPublisher;
    }

    public boolean importOrder(
            final Customer customer,
            final Set<Product> products,
            final ImportOrderInput input
    ) throws Exception {
        final var context = input.executionContext();
        if (input.code() != null && orders.existsByCode(input.code())) {
            context.addMessage(new Message(Message.Type.SUCCESS, "Order already imported"));
            return true;
        }
        final var order = Order.createNew(context, input, customer, products);
        if (order == null) {
            context.addMessage(new Message(Message.Type.ERROR, "Order is invalid"));
            return false;
        }
        orders.save(order);
        metricsPublisher.increment(IMPORTED_ORDERS_METRIC, 1, IMPORT_ORDER_TAGS);
        context.addMessage(new Message(Message.Type.SUCCESS, "Order imported successfully"));
        return true;
    }
}
