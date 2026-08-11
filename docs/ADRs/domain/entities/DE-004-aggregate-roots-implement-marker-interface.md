# DE-004 - Aggregate roots implement IAggregateRoot

# STATUS: APPROVED

# Context

## The problem (Analogy)

In a school, visitors do not walk into every classroom and independently alter attendance records. They go through the school office, which is the recognized entry point and coordinates changes across the school. A clear sign identifies that office so people and procedures know where requests belong.

An aggregate root is the recognized entry point to a consistency boundary. The codebase needs an equally clear sign that distinguishes roots from ordinary entities inside the boundary.

## The problem technical

Entities and aggregate roots may look structurally similar in Java. Without an explicit type-level distinction, repositories, application services, event infrastructure, and reviewers cannot reliably tell which entities may be loaded or modified directly. Developers may accidentally create repositories for child entities or bypass the root, breaking aggregate invariants and transaction boundaries.

# Common usage

## Traditional solution

Aggregate roots are identified by naming conventions, package placement, annotations, documentation, or developer knowledge. Repositories accept any entity type and rely on reviews to detect boundary violations.

## Side-effects

The architectural rule is invisible to the compiler and easy to apply inconsistently. Refactoring or moving a class can erase package-based meaning. Generic repository APIs cannot constrain their type parameters to aggregate roots, and automated architecture tests have no stable contract to inspect.

# Decision

## Solution

Every aggregate root must implement `com.gustavo.dev.domain.entities.interfaces.IAggregateRoot`. Entities that are not aggregate roots must not implement it.

`IAggregateRoot` is a marker interface: it declares architectural role, not shared behavior. Repositories and other aggregate-level infrastructure should constrain their generic types to `IAggregateRoot` where practical. Architecture tests must verify that repository-managed domain types implement the interface.

Implementing the interface does not by itself make a class a valid aggregate root. The model must still define a real consistency boundary, protect internal entities, and allow external references to address internal members through the root rather than by direct repository access.

## Why it works better

The aggregate boundary becomes explicit in the type system and remains visible through refactoring. Infrastructure can enforce the intended access path without depending on naming or package conventions. The marker also gives reviewers a direct prompt to examine whether the type truly owns invariants and a transaction boundary.

# Outcomes

## Benefits

- Aggregate roots are immediately recognizable in code.
- Repository APIs can restrict operations to aggregate roots.
- Architecture tests can enforce boundary rules.
- Accidental repositories for internal entities become easier to prevent.
- The marker adds no domain behavior or runtime state.

## Trade-offs

- A marker interface cannot prove that the aggregate is designed correctly.
- Developers may apply the marker mechanically to gain repository access.
- Domain code gains a small dependency on a shared architectural interface.
- Existing roots and repository bounds may require migration.

## Superestimated trade-offs

Marker interfaces are sometimes dismissed as empty types. Here, emptiness is intentional: the interface represents a semantic category that generic constraints and architecture tests can consume. It becomes harmful only if used as a substitute for modeling or if unrelated behaviors are gradually added to it.

# Theory Foundation

Evans defines the aggregate root as the only externally accessible member responsible for protecting the aggregate's invariants. Vernon emphasizes one repository per aggregate and transactional consistency through the root. `IAggregateRoot` turns that DDD role into an explicit Java type. Martin's Dependency Inversion Principle supports infrastructure depending on a stable domain-level abstraction, while the Interface Segregation Principle supports keeping the marker free of unrelated operations.
