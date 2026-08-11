# DE-001 - Domain entities are final

# STATUS: APPROVED

# Context

## The problem (Analogy)

A driving school certifies a car only after checking its brakes, steering, and safety equipment. If somebody can later convert that certified car into a different kind of vehicle while keeping the same certificate, the guarantee no longer means anything. The certification is trustworthy only when the certified type and its rules cannot be silently replaced.

A domain entity has the same responsibility. Its public type promises that every operation follows the business rules encoded by that entity. Subclassing can weaken that promise by overriding behavior or exposing a construction path that bypasses those rules.

## The problem technical

An inheritable entity exposes extension points beyond its intended domain API. A subtype may override a state transition, change validation behavior, add mutable state, or violate equality and persistence assumptions. Callers typed to the parent entity cannot know whether the original invariants still hold. Domain rules therefore stop being fully encapsulated by the declared entity.

This decision concerns concrete domain entities. Framework base classes and deliberately designed abstract domain concepts are not concrete entities and are outside this rule.

# Common usage

## Traditional solution

Java entities are commonly declared as non-final classes with public or protected methods. This is often done by habit or to allow Hibernate to generate subclass proxies for lazy loading.

## Side-effects

Inheritance becomes an unplanned domain extension mechanism. A subtype can change behavior in ways the base entity did not anticipate, tests may pass against the base type while production uses a proxy or subtype, and invariants become distributed across a hierarchy. Entity equality is also harder to implement safely when runtime types differ.

# Decision

## Solution

Every concrete domain entity must be declared `final`. Its invariant-enforcing behavior must not be overridable. Reuse must use composition, value objects, domain services, or an explicitly modeled abstract domain concept rather than subclassing a concrete entity.

Hibernate mappings must not rely on subclass proxies for these entities. The persistence configuration must use an approach compatible with final classes, such as build-time bytecode enhancement, eager loading where justified, or explicit no-proxy association handling. This compatibility must be verified by an integration test.

## Why it works better

The runtime type cannot redefine the entity's transitions or relax its validation. The entity's API becomes the complete behavioral boundary for its rules, making its guarantees easier to reason about and test. Composition also makes additional behavior explicit instead of hiding it behind polymorphism that was introduced for infrastructure convenience.

# Outcomes

## Benefits

- Domain behavior and invariants cannot be weakened through subclassing.
- Entity APIs have stable semantics.
- Equality and identity rules are easier to reason about.
- Composition and explicit domain abstractions replace accidental inheritance.
- Persistence concerns do not dictate the domain type hierarchy.

## Trade-offs

- Default Hibernate subclass proxies cannot proxy final entity classes.
- Lazy-loading configuration requires deliberate design and integration testing.
- A legitimate polymorphic domain model requires an explicit exception or a different model.
- Test doubles cannot subclass entities; tests must use real instances through their factories.

## Superestimated trade-offs

The loss of inheritance is often presented as a major loss of flexibility. For concrete domain entities, that flexibility is usually unsafe because it allows their contract to change without changing their public type. Composition covers most reuse needs. The concern becomes genuine when subtype identity and behavior are part of the ubiquitous language; in that case the hierarchy must be modeled explicitly rather than created as an incidental extension point.

# Theory Foundation

Eric Evans describes entities through identity, lifecycle, and continuity, not through unrestricted implementation inheritance. Keeping a concrete entity final protects that lifecycle boundary. Robert C. Martin's Open/Closed Principle does not require every class to be inheritable; stable abstractions can be extended through composition while remaining closed against modification. Joshua Bloch's guidance to design and document for inheritance or prohibit it supports making entities final when their invariants were not designed for overriding. The GoF preference for composition over class inheritance supports assembling behavior without exposing domain rules to subclasses.
