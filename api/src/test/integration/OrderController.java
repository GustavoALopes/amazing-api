package com.gustavo.dev.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderController {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("order_import_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void databaseProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void importsCustomerProductsAndOrder() throws Exception {
        final var orderCode = UUID.randomUUID();
        final var payload = """
                {
                  "products": [
                    {"skuCode":"KEYBOARD-001","name":"Mechanical Keyboard","price":"89.90","quantity":2},
                    {"skuCode":"MOUSE-001","name":"Wireless Mouse","price":"39.90","quantity":3}
                  ],
                  "customer": {
                    "customerDocument":"123456789",
                    "customerDocumentType":"NIF",
                    "name":"Ada Lovelace",
                    "birthdate":"1990-12-10"
                  },
                  "purchasedAt":"2026-08-10T12:00:00Z",
                  "totalValue":"299.50",
                  "code":"%s",
                  "country":"Portugal",
                  "state":"Lisbon",
                  "city":"Lisbon",
                  "neighborn":"Alfama",
                  "street":"Main Street",
                  "number":"42",
                  "zipcode":"1100-001"
                }
                """.formatted(orderCode);

        final var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/orders/import"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("tenantId", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        final var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), response.body());
        assertEquals(1, count("select count(*) from customers where document_value = '123456789' "
                + "and document_type = 'NIF' and first_name = 'Ada' and last_name = 'Lovelace'"));
        assertEquals(2, count("select count(*) from products where sku in ('KEYBOARD-001', 'MOUSE-001')"));
        assertEquals(1, jdbcTemplate.queryForObject(
                "select count(*) from orders where code = ? and total_value = 299.50 "
                        + "and address_country = 'Portugal' and address_city = 'Lisbon'",
                Integer.class, orderCode));
        assertEquals(2, jdbcTemplate.queryForObject(
                "select count(*) from product_item pi join orders o on o.id = pi.order_id where o.code = ?",
                Integer.class, orderCode));
        assertEquals(5, jdbcTemplate.queryForObject(
                "select sum(pi.quantity) from product_item pi join orders o on o.id = pi.order_id where o.code = ?",
                Integer.class, orderCode));
    }

    private int count(final String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
