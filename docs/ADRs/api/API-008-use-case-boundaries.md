# API-008 - Use cases depend on domain inputs and domain services

# STATUS: APPROVED

# Context

## The problem (Analogy)

A restaurant waiter translates a customer's words into a kitchen order before handing it to the head chef. The head chef coordinates the specialist stations and returns a plated meal. The chef does not accept the waiter's notes as the kitchen's internal language, nor reach into ingredient stores and prepare every component personally.

Use cases have the same coordinating role. They accept a domain-defined request, direct the appropriate domain services, and return an application response prepared for the delivery boundary.

## The problem technical

A use case sits between the controller boundary and domain behavior. Accepting an API DTO couples its contract to HTTP serialization and controller versioning. Accessing entities directly moves domain-service responsibilities into orchestration code, duplicates business rules, and allows application transactions to bypass the domain's intended entry points.

Some operations span more than one domain capability. For example, importing an order may require checking whether each product exists, registering missing products, and then registering the order. A single service cannot own this complete workflow without either crossing domain responsibilities or becoming an application orchestrator disguised as a domain service.

# Common usage

## Traditional solution

A controller passes its request DTO directly to a use case. The use case loads repositories or entities, invokes entity methods, persists changes, and returns an entity or a generic result. For cross-domain operations, teams often add all behavior to one large service.

## Side-effects

The use-case interface changes with the HTTP contract, entities escape their domain boundary, and application code duplicates validation or persistence decisions owned by domain services. Large services become coupled to unrelated domains, while transaction and response mapping behavior varies by endpoint.

# Decision

## Solution

Every use case must follow this boundary contract:

- It must receive an operation-specific domain input object from the relevant `domain.entities.<entity>.inputs` package. It must never receive an API DTO from `application.controllers.dtos.input`.
- It must produce an operation-specific view model from `application.controllers.dtos.view` and return it inside `DefaultReturn`, as defined by API-009.
- It must not use domain entities or repositories directly. Its domain dependencies are restricted to services from `domain.services`.
- It owns the application transaction and orchestrates calls to one or more domain services when an operation crosses domain capabilities.
- Domain services retain responsibility for entity access, invariant enforcement, and the domain operations they own.

The controller or a mapper at the controller boundary converts the API input model into the domain input before invoking the use case. The use case maps the result supplied by domain services into the required view model before returning.

For example, an `ImportOrderUseCase` that must ensure referenced products exist depends on both `ProductService` and `OrderService`. Within one controlled transaction, it asks `ProductService` to validate or register missing products and then asks `OrderService` to register the order. It does not load or modify `Product` and `Order` entities itself.

```java
public final class ImportOrderUseCase {
    private final ProductService productService;
    private final OrderService orderService;

    public DefaultReturn<ImportOrderViewModel> execute(ImportOrderInput input) {
        productService.registerMissingProducts(input.products());
        var importedOrder = orderService.importOrder(input);
        var viewModel = ImportOrderViewModel.from(importedOrder);
        return new DefaultReturn<>(viewModel, importedOrder.messages());
    }
}
```

The example is illustrative: the concrete service result and mapper may vary, but neither may expose a domain entity through the use-case boundary.

## Why it works better

The input contract uses domain language and remains independent of transport changes. Domain services are the exclusive gateway to entities, so business rules remain in the domain layer. The use case can still coordinate several domain capabilities and control their shared transaction without absorbing the rules that belong to those services. Returning a wrapped view model gives the caller explicit application data and messages without exposing domain state.

# Outcomes

## Benefits

- Use-case inputs are independent of HTTP and serialization concerns.
- Entities and repositories remain behind domain services.
- Cross-domain workflows have an explicit orchestration point.
- Transactions can cover all service calls in one application operation.
- Use-case results are safe, intentional client-facing models.
- Controllers, use cases, and domain services each have testable responsibilities.

## Trade-offs

- The controller boundary requires API-input-to-domain-input mapping.
- The use case requires service-result-to-view-model mapping.
- Operations spanning several domains introduce multiple service dependencies.
- Service APIs may need purpose-built results so the use case can create a view model without receiving an entity.
- Very simple operations require the same layering as complex workflows.

## Superestimated trade-offs

Restricting entity access can appear to turn domain services into thin wrappers. A service boundary is valuable only when it exposes meaningful domain operations; it must not become a generic CRUD facade. If a service merely forwards arbitrary entity access, its API should be redesigned around domain intent.

Depending on multiple domain services may also be mistaken for excessive coupling. For a genuine cross-domain transaction, that coupling already exists in the business operation. Keeping it visible in an operation-specific use case is safer than hiding it inside one domain service or duplicating it across controllers.

# Theory Foundation

Robert C. Martin's Dependency Rule keeps transport details outside use-case contracts and makes dependencies point toward domain abstractions. Evans's layered architecture assigns workflow coordination to the application layer while domain services express domain operations that do not naturally belong to one entity. Vernon's application-service guidance places transaction control around the orchestration of domain behavior. The Facade principle supports exposing intentional domain-service operations instead of allowing outer layers to manipulate entities and repositories directly.
