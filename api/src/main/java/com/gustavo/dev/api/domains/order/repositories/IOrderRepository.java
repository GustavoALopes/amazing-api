package com.gustavo.dev.api.domains.order.repositories;

import com.gustavo.dev.api.domains.order.entities.Order;

import java.util.UUID;

public interface IOrderRepository {
    boolean existsByCode(UUID code);
    Order save(Order order);
}
