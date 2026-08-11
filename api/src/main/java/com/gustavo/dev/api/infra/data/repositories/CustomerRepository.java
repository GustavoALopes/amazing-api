package com.gustavo.dev.api.infra.data.repositories;

import com.gustavo.dev.api.domains.customer.entities.Customer;
import com.gustavo.dev.api.domains.customer.repositories.ICustomerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CustomerRepository implements ICustomerRepository {
    @PersistenceContext private EntityManager entityManager;

    @Override
    public Optional<Customer> findByDocument(final String documentValue, final String documentType) {
        return entityManager.createQuery(
                        "select c from Customer c where c.document.value = :value and c.document.type = :type",
                        Customer.class)
                .setParameter("value", documentValue).setParameter("type", documentType)
                .getResultStream().findFirst();
    }

    @Override public Customer save(final Customer customer) {
        entityManager.persist(customer);
        return customer;
    }
}
