# DS-006 - Domain services must not depend on domain services

# STATUS: APPROVED

# Context

## The problem (Analogy)

An orchestra conductor coordinates musicians to perform a piece. Conductors do not conduct one another in a chain; if broader coordination is required, the concert program coordinates the separate performances.

## The problem technical

A domain service exists to orchestrate a business operation through domain entities, value objects, and domain-owned dependency interfaces. When one domain service depends on another, orchestration layers become chained, ownership of the business operation becomes unclear, and service behavior can no longer be understood or tested in isolation.

The coupling still exists when the dependency is hidden behind static methods, service locators, factories, or indirect lookup rather than declared in a constructor.

# Common usage

## Traditional solution

A domain service injects another domain service, calls one of its static methods, or retrieves it indirectly to reuse an existing orchestration flow.

## Side-effects

Service graphs become difficult to follow and may become cyclic. Transaction and failure behavior crosses unclear boundaries, tests require chains of services, and an internal orchestration method becomes an accidental shared API.

# Decision

## Solution

A domain service must never depend on another domain service, whether both services belong to the same domain or to different domains.

This prohibition includes:

- constructor, field, setter, or method-parameter dependencies on another domain service;
- direct calls to another domain service instance;
- static calls to methods or members of another domain service;
- indirect resolution through a service locator, application context, factory, registry, or reflection;
- inheritance or composition used to reuse another domain service's orchestration.

A domain service may depend on domain entities, value objects, domain inputs, and domain-owned interfaces for repositories or external capabilities, as established by DS-004. If multiple business operations require the same domain rule, that rule must be placed in its natural domain owner, such as an entity, value object, specification, policy, or domain-owned port. Shared orchestration is not extracted into a domain service dependency.

When a business case requires multiple domain services, the application use case must coordinate them as separate dependencies. The use case owns the end-to-end workflow and transaction boundary; neither domain service calls the other.

```java
public final class CompleteOrderUseCase {
    private final OrderService orderService;
    private final PaymentService paymentService;

    public void execute(CompleteOrderInput input) {
        var order = orderService.prepare(input.order());
        paymentService.charge(input.payment(), order.total());
    }
}
```

## Why it works better

Each domain service remains a self-contained orchestrator with explicit domain dependencies. Cross-service workflow remains visible at the application boundary, while reusable business rules live with the domain concept that owns them.

# Outcomes

## Benefits

- Domain-service dependency graphs cannot become cyclic.
- Each service has a clear business responsibility and can be tested independently.
- Cross-domain and multi-operation workflows remain visible in use cases.
- Reusable rules are modeled explicitly instead of hidden inside orchestration code.
- Static access cannot conceal architectural coupling.

## Trade-offs

- A use case may need to inject and coordinate more than one domain service.
- Existing service-to-service reuse must be refactored toward the owning domain concept or port.
- Similar orchestration steps may remain separate when they represent distinct business cases.

## Superestimated trade-offs

Prohibiting domain-service dependencies does not prohibit code reuse. It prevents orchestration from becoming a reusable dependency. Stable domain rules can still be shared through their proper owner, and technical capabilities can be shared through domain-owned interfaces. If two services must always execute as one indivisible operation, that is evidence that their responsibilities or the enclosing use case should be redesigned.

# Theory Foundation

Evans defines a domain service as an operation that expresses domain behavior without a natural entity or value-object owner. The Single Responsibility Principle favors one clear reason for each orchestrator to change, while application services provide the appropriate boundary for coordinating multiple domain operations. Acyclic Dependencies further requires dependencies between architectural components to remain free of cycles.
