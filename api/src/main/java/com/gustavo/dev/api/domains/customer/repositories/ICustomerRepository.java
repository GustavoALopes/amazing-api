package com.gustavo.dev.api.domains.customer.repositories;

import com.gustavo.dev.api.domains.customer.entities.Customer;

import java.util.Optional;

public interface ICustomerRepository {
    Optional<Customer> findByDocument(String documentValue, String documentType);
    Customer save(Customer customer);
}
