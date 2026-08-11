package com.gustavo.dev.api.domains.customer.services;

import com.gustavo.dev.api.domains.customer.entities.Customer;
import com.gustavo.dev.api.domains.customer.repositories.ICustomerRepository;
import com.gustavo.dev.api.domains.order.entities.inputs.ImportOrderInput;
import com.gustavo.dev.domain.entities.inputs.Message;
import org.springframework.stereotype.Service;

@Service
public final class CustomerService {
    private final ICustomerRepository repository;

    public CustomerService(final ICustomerRepository repository) { this.repository = repository; }

    public Customer importCustomer(
            final com.gustavo.dev.domain.entities.inputs.ExecutionContext context,
            final ImportOrderInput.CustomerInput source
    ) throws Exception {
        final var customer = Customer.createNew(context, source);
        if (customer == null) {
            error(context, "Customer is invalid");
            return null;
        }

        final var persistedCustomer = repository.findByDocument(
                customer.document().value(), customer.document().type()).orElseGet(() -> repository.save(customer));
        context.addMessage(new Message(Message.Type.SUCCESS, "Customer processed successfully"));
        return persistedCustomer;
    }

    private static void error(final com.gustavo.dev.domain.entities.inputs.ExecutionContext context,
                              final String text) {
        context.addMessage(new Message(Message.Type.ERROR, text));
    }
}
