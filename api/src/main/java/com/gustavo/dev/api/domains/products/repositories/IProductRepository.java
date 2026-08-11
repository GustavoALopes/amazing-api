package com.gustavo.dev.api.domains.products.repositories;

import com.gustavo.dev.api.domains.products.entities.Product;

import java.util.Optional;

public interface IProductRepository {
    Optional<Product> findBySku(String sku);
    Product save(Product product);
}
