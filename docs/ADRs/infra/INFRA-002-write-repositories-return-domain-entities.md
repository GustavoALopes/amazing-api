# INFRA-002 - Write-side repositories return domain entities

# STATUS: APPROVED

# Context

## The problem (Analogy)

A workshop requests complete parts from its storeroom, not display photographs of those parts. The workshop needs objects it can use while preserving their operating rules.

## The problem technical

When a command-side repository returns a controller view model, presentation concerns enter the domain workflow. Use cases can no longer rely on aggregate behavior and may accidentally treat a partial projection as an entity suitable for business changes.

# Common usage

## Traditional solution

One repository interface returns whichever shape is most convenient to each caller, mixing domain entities, persistence models, tuples, and controller DTOs.

## Side-effects

The repository contract becomes ambiguous, domain operations depend on presentation types, and partial read shapes can be mistaken for mutable aggregates.

# Decision

## Solution

A repository implementation under `infra.data.repositories` whose name does not end in `<Entity>RepositoryReadOnly` must return domain entities for operations that return persisted business objects. Returning a `ViewModel`, including any type from `application.controllers.dtos.view`, is forbidden.

These repositories must implement an interface declared under the relevant `domains.*.repositories` package. For example, `OrderRepository` implements the domain-owned order repository interface and returns `Order` domain entities.

Persistence-specific representations must be mapped to domain entities inside the infrastructure repository. This decision does not require every method to return an entity: write operations may legitimately return `void`, a status, or another result explicitly modeled by the domain repository contract.

## Why it works better

The domain owns the contract needed to retrieve and persist aggregates, and infrastructure supplies the implementation. Command workflows receive behavior-rich domain objects rather than presentation-specific data shapes.

# Outcomes

## Benefits

- Command-side code operates on domain entities and their invariants.
- The domain contract does not depend on controller DTOs.
- Repository names make the permitted return model clear.
- Persistence-to-domain mapping stays inside infrastructure.

## Trade-offs

- Loading an aggregate can cost more than selecting a narrow projection.
- Separate read-only repositories are needed for view-optimized queries.

## Superestimated trade-offs

Returning entities may seem inefficient for every read. View-oriented reads are deliberately handled by `<Entity>RepositoryReadOnly` implementations, so command repositories need only serve workflows that require the domain model.

# Theory Foundation

Dependency inversion places repository abstractions with the domain policy that consumes them. In Domain-Driven Design, repositories reconstruct aggregates so callers can execute behavior while maintaining aggregate invariants.

