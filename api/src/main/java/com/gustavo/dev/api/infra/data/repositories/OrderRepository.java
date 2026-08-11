package com.gustavo.dev.api.infra.data.repositories;

import com.gustavo.dev.api.domains.order.entities.Order;
import com.gustavo.dev.api.domains.order.repositories.IOrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class OrderRepository implements IOrderRepository {
    @PersistenceContext private EntityManager entityManager;

    @Override public boolean existsByCode(final UUID code) {
        return entityManager.createQuery("select count(o) from Order o where o.code = :code", Long.class)
                .setParameter("code", code).getSingleResult() > 0;
    }

    @Override public Order save(final Order order) {
        entityManager.persist(order);
        return order;
    }
}
