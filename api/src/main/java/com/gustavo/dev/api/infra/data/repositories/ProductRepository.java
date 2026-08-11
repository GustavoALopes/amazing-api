package com.gustavo.dev.api.infra.data.repositories;

import com.gustavo.dev.api.domains.products.entities.Product;
import com.gustavo.dev.api.domains.products.repositories.IProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProductRepository implements IProductRepository {
    @PersistenceContext private EntityManager entityManager;

    @Override public Optional<Product> findBySku(final String sku) {
        return entityManager.createQuery("select p from Product p where p.sku.value = :sku", Product.class)
                .setParameter("sku", sku).getResultStream().findFirst();
    }

    @Override public Product save(final Product product) {
        entityManager.persist(product);
        return product;
    }
}
