# Domain ADR Authoring Guide

This directory contains Architecture Decision Records (ADRs) about the domain layer. Create one ADR for each meaningful domain-modeling decision, especially when choosing entity identity, lifecycle, invariants, relationships, ownership, persistence boundaries, or domain-service responsibilities.

An ADR explains **why** a decision was made. It is not merely a description of the resulting code.

## Approved ADRs

- [DE-001 - Domain entities are final](entities/DE-001-final-domain-entities.md)
- [DE-002 - Domain entities use private constructors and public factory methods](entities/DE-002-private-constructors-and-factory-methods.md)
- [DE-003 - Domain entities exist only in valid states](entities/DE-003-entities-always-valid-and-immutable.md)
- [DE-004 - Aggregate roots implement IAggregateRoot](entities/DE-004-aggregate-roots-implement-marker-interface.md)
- [DE-005 - Domain entities own validation rules and metadata](entities/DE-005-entities-own-validation-rules-and-metadata.md)
- [DE-006 - Domain entity public methods use domain input objects](entities/DE-006-public-methods-use-domain-input-objects.md)
- [DE-007 - Domain entity setters are private boolean validators behind business operations](entities/DE-007-private-boolean-setters-behind-business-operations.md)

### Domain services

- [DS-001 - Domain service methods accept domain inputs](service/DS-001-domain-service-methods-accept-domain-inputs.md)
- [DS-002 - Domain services orchestrate; entities validate](service/DS-002-domain-services-orchestrate-entities-validate.md)
- [DS-003 - Use cases own transaction boundaries](service/DS-003-use-cases-own-transaction-boundaries.md)
- [DS-004 - Domain services depend on interfaces](service/DS-004-domain-services-depend-on-interfaces.md)
- [DS-005 - Domain services are concrete classes in domain.services](service/DS-005-concrete-domain-services-package.md)

## File conventions

- Use Markdown (`.md`).
- Prefix each ADR with the next sequential domain-entity identifier using `DE-NNN`, followed by a short kebab-case description, for example `DE-006-customer-as-aggregate-root.md`.
- Put the same identifier at the beginning of the ADR title, for example `# DE-006 - Customer is an aggregate root`.
- Discuss one primary decision per ADR.
- Define domain terminology consistently with the project's ubiquitous language.
- Prefer concrete examples over abstract claims.
- Explain alternatives and costs honestly; do not present the decision as universally correct.
- Link related ADRs, source code, diagrams, or requirements when useful.
- Update `# STATUS` if the decision changes; preserve superseded ADRs as historical records.

## Required structure

Every domain entity ADR must use the following structure.

```markdown
# <ADR title>

# STATUS: APPROVED

# Context

## The problem (Analogy)

## The problem technical

# Common usage

## Traditional solution

## Side-effects

# Decision

## Solution

## Why it works better

# Outcomes

## Benefits

## Trade-offs

## Superestimated trade-offs

# Theory Foundation
```

## Section guidance

### `# STATUS: APPROVED`

State the lifecycle status of the ADR. New ADRs covered by this guide use `APPROVED`. If the repository later adopts a broader lifecycle, recommended values are `PROPOSED`, `APPROVED`, `DEPRECATED`, and `SUPERSEDED`.

### `# Context`

Describe the forces that make a decision necessary: business rules, constraints, current behavior, risks, and relevant system boundaries. Include enough context for an engineer unfamiliar with the original discussion to understand the decision.

#### `## The problem (Analogy)`

Explain the problem through one familiar real-world analogy, such as a car, supermarket, school, library, restaurant, or bank. Map the important parts of the analogy to the domain explicitly. Keep it accurate and simple; avoid an analogy that hides an important technical constraint.

Example: a supermarket receipt has a stable identity even when two receipts contain identical products. In the same way, two `Order` entities may have equal field values while remaining different orders because their identities differ.

#### `## The problem technical`

Restate the problem precisely as a software engineer. Identify the affected domain entities, value objects, aggregates, invariants, lifecycle, identity rules, transaction boundaries, and dependencies where relevant. Describe the failure mode or design pressure without prescribing the solution yet.

### `# Common usage`

Describe the approach commonly found in similar systems or currently used in this codebase.

#### `## Traditional solution`

Explain the conventional implementation and why teams tend to choose it. Include a small code example or model sketch when that makes the approach clearer.

#### `## Side-effects`

Describe observable consequences of the traditional approach, including accidental coupling, invalid states, weak encapsulation, persistence leakage, excessive mutation, transaction complexity, or difficult testing. Distinguish demonstrated problems from anticipated risks.

### `# Decision`

State the selected architectural decision in direct, testable language.

#### `## Solution`

Describe the chosen domain model and its rules. Specify, as applicable:

- entity identity and equality;
- aggregate ownership and boundaries;
- where invariants are enforced;
- allowed state transitions;
- construction and mutation policies;
- repository and domain-service responsibilities;
- domain events;
- persistence considerations that affect the model.

Use technology-neutral domain language first, then document implementation details.

#### `## Why it works better`

Compare the solution directly with the traditional approach under the constraints stated in the context. Explain which failure modes it prevents and why. Avoid claims such as "cleaner" or "more scalable" unless they are supported by specific reasoning or evidence.

### `# Outcomes`

Record the expected positive and negative consequences. Outcomes should make later review possible.

#### `## Benefits`

List concrete improvements, such as stronger invariant protection, clearer ownership, better testability, reduced coupling, explicit lifecycle rules, or closer alignment between code and business language.

#### `## Trade-offs`

List real costs accepted by the decision, such as additional types, mapping code, learning effort, more explicit orchestration, migration work, or reduced convenience for simple CRUD operations. Where possible, describe mitigations.

#### `## Superestimated trade-offs`

Record objections or costs that may initially appear larger than they are. Explain the assumption behind each concern, why its impact is likely overestimated, and under which conditions it would become a genuine problem. Do not use this section to dismiss valid trade-offs.

### `# Theory Foundation`

Connect the decision to established design principles. Cite only concepts that materially support the decision, and explain the connection rather than listing authors' names.

Useful foundations include:

- **Eric Evans, _Domain-Driven Design_**: entities, value objects, aggregates, repositories, services, factories, domain events, and ubiquitous language.
- **Vaughn Vernon, _Implementing Domain-Driven Design_**: aggregate design rules, consistency boundaries, application services, and domain events.
- **Gang of Four, _Design Patterns_**: patterns such as Factory, Strategy, State, Specification, and Observer when they solve the stated modeling problem.
- **Robert C. Martin**: Single Responsibility, Dependency Inversion, Stable Dependencies, boundaries, and use-case-centered design.
- **Joshua Bloch, _Effective Java_**: equality contracts, immutability, controlled construction, defensive copying, and API design.

When possible, name the book, principle or pattern, and the way it applies. For example: "Entity equality follows Bloch's equality-contract guidance while using the stable domain identifier described by Evans."

## Quality checklist

Before approving an ADR, confirm that:

- the business problem is understandable without reading the implementation;
- the analogy and technical explanation describe the same problem;
- identity and equality semantics are explicit when entities are involved;
- invariants and the component responsible for enforcing them are named;
- the traditional solution and its side-effects are represented fairly;
- the decision can be distinguished from rejected alternatives;
- benefits and trade-offs are concrete and balanced;
- overestimated concerns are supported by reasoning, not dismissed;
- theory is connected to the actual decision;
- terminology matches the domain's ubiquitous language.
