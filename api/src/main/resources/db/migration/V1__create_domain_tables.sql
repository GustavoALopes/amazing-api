CREATE TABLE customers (
    id UUID PRIMARY KEY,
    first_name VARCHAR(254) NOT NULL,
    last_name VARCHAR(254) NOT NULL,
    birth_date DATE NOT NULL,
    document_value VARCHAR(64) NOT NULL,
    document_type VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    updated_by VARCHAR(255),
    correlation_id UUID,
    CONSTRAINT uk_customers_document UNIQUE (document_value, document_type)
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    sku VARCHAR(49) NOT NULL,
    name VARCHAR(254) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    updated_by VARCHAR(255),
    correlation_id UUID,
    CONSTRAINT uk_products_sku UNIQUE (sku)
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    total_value NUMERIC(38, 2) NOT NULL,
    purchase_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    address_country VARCHAR(255) NOT NULL,
    address_state VARCHAR(255) NOT NULL,
    address_city VARCHAR(255) NOT NULL,
    address_neighborhood VARCHAR(255) NOT NULL,
    address_street VARCHAR(255) NOT NULL,
    address_number VARCHAR(255),
    address_zip_code VARCHAR(255) NOT NULL,
    code UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    updated_by VARCHAR(255),
    correlation_id UUID,
    CONSTRAINT uk_orders_code UNIQUE (code),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE product_item (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    purchase_price NUMERIC(38, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    updated_by VARCHAR(255),
    correlation_id UUID,
    CONSTRAINT ck_product_item_quantity_positive CHECK (quantity > 0),
    CONSTRAINT fk_product_item_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_product_item_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_product_item_order_id ON product_item (order_id);
CREATE INDEX idx_product_item_product_id ON product_item (product_id);
