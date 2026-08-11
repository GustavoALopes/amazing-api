# DS-005 - Domain services are concrete classes in domain.services

# STATUS: APPROVED

# Context

## The problem (Analogy)

A workshop keeps all specialist coordinators at one clearly labelled desk. It does not create a second clerk merely to repeat the name and duties of each coordinator when no alternative coordinator exists.

## The problem technical

Domain services become difficult to discover when placed in application, infrastructure, or arbitrary feature packages. Creating an interface for every service also adds a one-to-one abstraction even though external dependencies, rather than the service itself, are the replaceable boundary.

# Common usage

## Traditional solution

Teams scatter service classes across generic `service`, `application`, and `impl` packages and create matching `IOrderService`/`OrderServiceImpl` pairs by convention.

## Side-effects

Package location no longer communicates architectural role. One-implementation interfaces duplicate APIs, add navigation cost, and can encourage application code to bypass the use-case boundary by injecting domain services directly.

# Decision

## Solution

Every domain service must be a concrete class placed in the relevant domain's `services` package (for example, `com.gustavo.dev.api.domains.order.services.OrderService`). Within the project structure, this is the domain-services package. A separate interface for the domain service is not required and must not be created solely as a convention.

The class should be named for the domain capability it provides, may be `final`, and receives its external port interfaces through its constructor. This decision does not remove interfaces at infrastructure boundaries; those are required by DS-004.

An interface may be introduced only when the domain genuinely defines multiple interchangeable service strategies or another explicit polymorphic contract. It must not produce an `Impl` class naming pattern by default.

## Why it works better

The package makes the architectural role immediately visible, while the concrete class represents domain policy directly. Abstractions are reserved for actual substitution boundaries instead of mechanically duplicating every service API.

# Outcomes

## Benefits

- Domain services have one predictable, discoverable location.
- One-to-one service interfaces and `Impl` classes are avoided.
- Class names express domain capabilities rather than implementation status.
- External dependencies remain substitutable through domain ports.

## Trade-offs

- Tests instantiate the concrete service rather than mock a service interface.
- Introducing true service-level polymorphism later requires extracting a contract.
- The package convention must be applied consistently across domains.

## Superestimated trade-offs

The absence of a service interface does not make infrastructure concrete or tests integration-heavy. Repository and external-API boundaries remain interfaces, and the concrete domain service can be tested with fake implementations. Extracting an interface later is straightforward when a real second strategy appears.

# Theory Foundation

Evans defines a domain service by stateless domain behavior that does not naturally belong to an entity or value object, not by an interface/class pair. Martin's Dependency Inversion Principle applies at volatile boundaries and does not require an interface for every class. Package-by-domain and explicit architectural naming improve cohesion and communicate ownership.
