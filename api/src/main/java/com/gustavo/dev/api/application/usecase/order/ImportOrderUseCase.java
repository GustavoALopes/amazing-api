package com.gustavo.dev.api.application.usecase.order;

import com.gustavo.dev.api.application.controllers.dtos.view.ImportOrderViewModel;
import com.gustavo.dev.api.domains.customer.services.CustomerService;
import com.gustavo.dev.api.domains.order.entities.inputs.ImportOrderInput;
import com.gustavo.dev.api.domains.order.services.OrderService;
import com.gustavo.dev.api.domains.products.services.ProductService;
import com.gustavo.dev.application.dtos.view.DefaultReturn;
import com.gustavo.dev.usecase.BaseUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;

@Component
public final class ImportOrderUseCase
        extends BaseUseCase<ImportOrderInput, DefaultReturn<ImportOrderViewModel>> {

    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderService orderService;

    public ImportOrderUseCase(
            final TransactionTemplate template,
            final CustomerService customerService,
            final ProductService productService,
            final OrderService orderService
    ) {
        super(template);
        this.customerService = customerService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @Override protected DefaultReturn<ImportOrderViewModel> internalExecute(final ImportOrderInput input) {
        try {
            final var customer = customerService.importCustomer(input.executionContext(), input.customer());
            final var products = productService.importProducts(input.executionContext(), input.products());
            orderService.importOrder(customer, products, input);
            return new DefaultReturn<>(new ImportOrderViewModel(input.code()),
                    new HashSet<>(input.executionContext().getMessages().values()));
        } catch (Exception exception) {
            throw new IllegalStateException("Order import failed", exception);
        }
    }
}
