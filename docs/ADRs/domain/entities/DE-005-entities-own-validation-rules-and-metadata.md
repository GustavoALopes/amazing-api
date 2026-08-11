# DE-005 - Domain entities own validation rules and metadata

# STATUS: APPROVED

# Context

## The problem (Analogy)

In a supermarket, the official rule for selling an age-restricted product cannot be different at the shelf, checkout, and customer-service desk. A single store policy defines the minimum age; signs and software may display that rule, but they derive it from the same policy. If every department invents its own number, customers receive contradictory decisions.

Domain validation has the same risk. APIs, forms, database mappings, and services may all need rule metadata, but the entity is the authority for whether its own state and transitions are valid.

## The problem technical

Validation constraints are often duplicated in controllers, DTO annotations, use cases, entities, and database schemas. When values such as maximum lengths, allowed ranges, or required formats drift, the same input can be accepted at one boundary and rejected at another. If validation exists only outside the entity, another entry point can bypass it and create invalid domain state.

# Common usage

## Traditional solution

Each layer defines convenient local validation: Bean Validation annotations on request DTOs, constants in UI code, checks in application services, JPA column metadata, and database constraints. The entity is treated mainly as a persistence structure.

## Side-effects

Rules diverge over time, changes require edits in several places, and engineers cannot identify the authoritative value. Boundary validation can provide good feedback but creates false confidence because it does not protect direct domain calls. Persistence constraints may reject invalid state only at transaction commit, far from the source of the error.

# Decision

## Solution

A domain entity is the single source of truth for validation rules that govern that entity's state and behavior. Its factories and transition methods must enforce those rules before returning a result.

Reusable validation metadata must be declared as `static final` fields owned by the entity and grouped in a nested static class representing the relevant domain sub-rule. For example:

```java
public final class Order extends BaseEntity<UUID> implements IAggregateRoot {
    public static final class ReferenceRule {
        public static final int MIN_LENGTH = 1;
        public static final int MAX_LENGTH = 50;

        private ReferenceRule() {
        }
    }
}
```

Names must express domain meaning rather than a UI or database concern. Constants must be immutable and must not expose mutable collections. Other layers may reference or translate this metadata to provide early feedback, but they must not redefine the authoritative rule. Database constraints should provide defense in depth and be kept consistent through tests or schema generation; they do not replace entity validation.

Rules that span multiple entities or aggregates belong to the smallest appropriate domain abstraction, such as an aggregate root, value object, specification, policy, or domain service. They must not be forced into an unrelated entity merely to satisfy this convention.

## Why it works better

The rule and the state it protects change together. Every creation and modification path receives the same decision, regardless of whether it originates from HTTP, messaging, tests, or a scheduled job. Grouped metadata makes simple constraints discoverable and reusable without moving authority to an outer layer.

# Outcomes

## Benefits

- Domain invariants have one authoritative implementation.
- Rule metadata is discoverable beside the entity behavior it supports.
- Entry points cannot bypass validation by omitting a DTO or controller check.
- Boundary layers can reuse metadata for earlier user feedback.
- Rule changes are easier to review and test.

## Trade-offs

- Presentation and persistence layers remain responsible for translating domain rules into their own mechanisms.
- Some duplication may remain where annotations or database DDL require compile-time literals.
- Public constants enlarge the entity's API and can couple consumers to implementation-level metadata.
- Dynamic, tenant-specific, or time-dependent policies cannot be represented safely as fixed static constants.
- Cross-aggregate rules require policies or domain services rather than entity-local metadata.

## Superestimated trade-offs

Keeping metadata on the entity may appear to mix validation and business behavior. Validation of an entity's own invariants is business behavior, so colocating it strengthens cohesion. The concern becomes valid for transport formatting, localization, authorization, or rules requiring external data; those concerns belong at another boundary and must not be mislabeled as entity invariants.

Public constants are also sometimes assumed to eliminate every duplicated constraint. They cannot directly supply every annotation or database declaration. The goal is one source of truth for the domain decision, supported by consistency tests where technical duplication is unavoidable.

# Theory Foundation

Evans's entities and aggregates encapsulate the invariants associated with their identity and lifecycle, while ubiquitous language gives the rules domain-oriented names. Vernon places invariant enforcement inside the aggregate consistency boundary and uses domain services or policies for rules that do not naturally belong to one entity. Martin's Single Responsibility Principle supports giving the entity responsibility for changes to its own domain rules, and his Dependency Inversion Principle keeps delivery and persistence mechanisms from becoming the authority. Bloch's `static final` constant and immutability guidance supports safe, named metadata, including defensive treatment of mutable values. The GoF Specification pattern is appropriate when a rule must be composed or expressed independently while remaining a domain concept.
