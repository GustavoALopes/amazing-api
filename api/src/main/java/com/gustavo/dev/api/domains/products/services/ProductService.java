package com.gustavo.dev.api.domains.products.services;

import com.gustavo.dev.api.domains.order.entities.inputs.ImportOrderInput;
import com.gustavo.dev.api.domains.products.entities.Product;
import com.gustavo.dev.api.domains.products.repositories.IProductRepository;
import com.gustavo.dev.domain.entities.inputs.Message;
import com.gustavo.dev.domain.entities.inputs.ExecutionContext;
import com.gustavo.dev.observation.interfaces.IMetricsPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Service
public final class ProductService {
    private static final String IMPORTED_PRODUCTS_METRIC = "products.imported";
    private static final Map<String, String> IMPORT_ORDER_TAGS = Map.of(
            "service", "amazing-api",
            "usecase", "importOrder"
    );

    private final IProductRepository repository;
    private final IMetricsPublisher metricsPublisher;

    public ProductService(
            final IProductRepository repository,
            final IMetricsPublisher metricsPublisher
    ) {
        this.repository = repository;
        this.metricsPublisher = metricsPublisher;
    }

    public Set<Product> importProducts(
            final ExecutionContext executionContext,
            final Set<ImportOrderInput.ProductInput> products
    ) throws Exception {
        if (products == null || products.isEmpty()) {
            executionContext.addMessage(new Message(Message.Type.ERROR, "Products are invalid"));
            return Set.of();
        }
        final var importedProducts = new HashSet<Product>();
        for (final var source : products) {
            final var product = importProduct(executionContext, source);
            if (product != null) importedProducts.add(product);
        }
        return Set.copyOf(importedProducts);
    }

    private Product importProduct(
            final ExecutionContext executionContext,
            final ImportOrderInput.ProductInput source
    )
            throws Exception {
        if (source == null) {
            executionContext.addMessage(new Message(Message.Type.ERROR, "Product is invalid"));
            return null;
        }
        final var product = Product.createNew(executionContext, source);
        if (product == null) {
            executionContext.addMessage(new Message(Message.Type.ERROR,
                    "Product " + source.skuCode() + " is invalid"));
            return null;
        }
        final var persistedProduct = repository.findBySku(product.sku().value())
                .orElseGet(() -> repository.save(product));
        executionContext.addMessage(new Message(Message.Type.SUCCESS,
                "Product " + source.skuCode() + " processed successfully"));
        metricsPublisher.increment(IMPORTED_PRODUCTS_METRIC, source.quantity(), IMPORT_ORDER_TAGS);
        return persistedProduct;
    }

}
