# DS-004 - Domain services depend on interfaces

# STATUS: APPROVED

# Context

## The problem (Analogy)

An appliance relies on a standardized power socket, not on the internal machinery of one power station. The supplier can change without redesigning the appliance.

## The problem technical

Domain-service orchestration may require persistence or external APIs. Depending on concrete adapters couples domain code to databases, HTTP clients, frameworks, configuration, and infrastructure lifecycles.

# Common usage

## Traditional solution

A service constructs or injects a concrete repository or API client and calls implementation-specific methods.

## Side-effects

Infrastructure changes ripple into the domain, isolated tests require real technical systems or heavy mocking, and dependency direction points from the domain outward.

# Decision

## Solution

A domain service must depend only on domain-owned interfaces for external capabilities. Repository contracts belong in the relevant `domain.repository` package and use intention-revealing names such as `IOrderRepository`. Equivalent domain ports must be defined for external APIs when required.

Concrete database repositories, HTTP clients, SDK adapters, and framework components implement those interfaces outside the domain and are supplied through dependency injection.

```java
public final class OrderService {
    private final IOrderRepository orderRepository;

    public OrderService(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
```

## Why it works better

The domain declares the capability it needs without knowing the technology that provides it. Implementations can change, and service tests can use small fakes that obey the same contract.

# Outcomes

## Benefits

- Dependencies point toward domain-owned abstractions.
- Infrastructure can be replaced without changing service policy.
- Unit tests can use deterministic fakes.
- Required external capabilities are explicit in constructors.

## Trade-offs

- Each external capability requires an interface and adapter.
- Interfaces must remain domain-oriented rather than mirror a vendor SDK.
- Dependency wiring is required at the application composition root.

## Superestimated trade-offs

An interface may appear unnecessary when only one implementation exists. Its purpose here is boundary ownership, not implementation count. It becomes needless ceremony only for collaborators that are already stable domain types and do not cross an infrastructure boundary.

# Theory Foundation

Martin's Dependency Inversion Principle requires high-level policy to depend on abstractions it owns. Ports and Adapters places persistence and external systems behind inbound-facing ports. Evans's Repository pattern provides a domain-oriented collection abstraction without exposing persistence mechanics.
