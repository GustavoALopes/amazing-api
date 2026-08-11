# DE-002 - Domain entities use private constructors and public factory methods

# STATUS: APPROVED

# Context

## The problem (Analogy)

A car factory does not hand customers a chassis, loose wheels, and an engine and call the result a car. The assembly line follows an ordered process, checks required parts, assigns a vehicle identity, and releases the car only after it satisfies the production rules. A side door that lets anybody skip the assembly line would make the factory's guarantees worthless.

Creating a domain entity is also a business operation. Identity, required data, audit information, and initial state must be established together before the object is released to application code.

## The problem technical

Public constructors permit callers to select arbitrary creation paths. Overloaded constructors can duplicate validation, omit required initialization, or expose persistence-oriented details. An entity can then be instantiated with missing identity, audit information, or an illegal initial state. Constructor calls also communicate mechanics rather than the domain intent of operations such as `createNew`, `register`, or `place`.

Hibernate needs a no-argument constructor to reconstitute persisted state. That technical path must not become a supported domain creation path.

# Common usage

## Traditional solution

Entities commonly expose a public no-argument constructor for ORM use and public setters for hydration. Application code then uses the same constructor and gradually fills the object.

## Side-effects

Partially initialized entities become observable. Callers must know the correct order of setter calls, validation is repeated outside the entity, and persistence requirements leak into the domain API. Searching for entity creation does not reveal a single controlled creation policy.

# Decision

## Solution

Domain creation constructors must be private. Each supported creation use case must be exposed through a named public static factory method whose name uses the ubiquitous language, for example `Order.createNew(context)`.

The sole exception is the no-argument constructor required for Hibernate reconstitution. It must have the narrowest visibility supported by the selected JPA/Hibernate configuration, contain no domain behavior, and be documented as infrastructure-only. Under standard JPA portability this constructor is `protected`; a private constructor may be used only when the verified Hibernate configuration supports it.

Factories must validate their inputs, create identity and audit data, establish the complete initial state, and return an entity only through the supported outcome contract. Application code must never invoke the Hibernate constructor directly.

## Why it works better

All domain creation enters through named operations with one enforceable policy. A factory expresses why the entity is being created and can coordinate validation and initialization before exposing it. The isolated Hibernate constructor satisfies persistence mechanics without presenting an invalid construction route as part of the domain API.

# Outcomes

## Benefits

- Every supported creation path is explicit and searchable.
- Required identity, audit data, and initial state are established consistently.
- Callers do not need to reproduce entity invariants.
- Factory names communicate domain intent better than constructor overloads.
- ORM requirements are isolated from normal application use.

## Trade-offs

- Reflection-based frameworks and serializers may require configuration.
- Hibernate constructor visibility depends on the chosen provider and JPA portability requirements.
- Factories add methods and may need distinct input models for different creation cases.
- Persistence integration tests are required to prove that reconstitution still works.

## Superestimated trade-offs

Static factories are sometimes considered unnecessary ceremony around `new`. The additional method is small compared with the cost of finding and repairing invalid entities, and it provides a stable place for creation rules. The ceremony becomes a real concern only for data structures with no identity, lifecycle, or invariant; those objects should normally be modeled as value objects or DTOs rather than entities.

# Theory Foundation

Evans and Vernon treat factories as the place to encapsulate complex creation and ensure that aggregates begin life consistently. The GoF Factory Method concept separates object creation from use and gives the creation operation a meaningful contract. Bloch recommends static factory methods because they can have descriptive names, control instance creation, and return an outcome other than a newly allocated object. Martin's encapsulation and boundary guidance supports keeping persistence-only mechanisms outside the domain's public API.
