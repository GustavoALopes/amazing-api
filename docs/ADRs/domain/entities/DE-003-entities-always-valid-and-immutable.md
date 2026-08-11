# DE-003 - Domain entities exist only in valid states

# STATUS: APPROVED

# Context

## The problem (Analogy)

A school does not issue a student card first and hope that a name, enrollment, and valid course selection will be supplied later. It checks the enrollment rules and issues the card only when the student is validly registered. When the student changes course, the school produces an updated record that has passed the same checks rather than distributing an unfinished record.

Likewise, an entity visible to application code must represent a valid domain fact, not a promise that somebody will complete or repair it later.

## The problem technical

Mutable entities built in steps can occupy invalid intermediate states. Validation performed only at API or database boundaries can be bypassed by another use case, a test, a message consumer, or a future adapter. In-place modification can also leave a partially changed entity when a later check fails. This complicates concurrency, debugging, and reasoning about whether any reference still represents valid state.

# Common usage

## Traditional solution

Application code constructs an empty entity, calls setters, invokes an optional `validate()` method, and eventually persists it. Updates mutate fields in place and rely on service-layer validation or database constraints.

## Side-effects

Invalid states exist in memory and may escape before validation. Rules are duplicated among services, mutation order affects correctness, and failed updates may leave the original object corrupted. Shared references can observe changes at unexpected times.

# Decision

## Solution

An entity exposed to domain or application code must always satisfy all of its invariants.

- Domain constructors are private, apart from the infrastructure-only Hibernate constructor.
- Public factory methods validate all creation inputs before returning an entity.
- A factory returns `null` when validation fails and returns a fully valid entity on success.
- Callers must handle the nullable result immediately and must not persist or dereference it without a null check.
- Domain changes are expressed as behavior methods that validate the requested transition and return a new valid entity instance. They do not mutate the original instance.
- Failed changes return `null`; the original instance remains unchanged and valid.
- Fields and referenced collections must be immutable or defensively copied so external code cannot bypass validation.

Hibernate reconstitution is a controlled infrastructure exception: the provider may temporarily allocate an unhydrated instance, but that instance must never be exposed to application code before hydration is complete. Post-load validation should be used when stored data cannot otherwise be trusted.

## Why it works better

Validity becomes a property of every observable entity rather than a phase callers must remember. Validating before construction or replacement prevents partial changes, and returning a new instance gives both success and failure atomic semantics: either a complete valid result is returned, or the existing valid entity is preserved.

# Outcomes

## Benefits

- Invalid domain entities cannot be deliberately created through the public API.
- Updates are atomic from the caller's perspective.
- Existing references cannot observe partially applied changes.
- Tests can assume that every non-null entity satisfies its invariants.
- Domain behavior is easier to reason about under concurrency.

## Trade-offs

- `null` does not explain which validation rule failed and can cause a delayed `NullPointerException` if callers ignore the contract.
- Static analysis and nullability annotations are needed to make the nullable contract visible.
- Copy-on-change can allocate more objects and requires careful handling of identity, audit history, and domain events.
- Hibernate and other reflection-based tools require integration-specific accommodations.
- Large object graphs may need structural sharing or aggregate redesign to avoid expensive copies.

## Superestimated trade-offs

Object allocation is often assumed to make immutable changes prohibitively expensive. Short-lived Java objects are generally inexpensive, and domain correctness usually dominates this cost. It becomes a genuine concern only after measurement identifies allocation or copying as a material bottleneck.

The nullable result is sometimes described as sufficient error handling for every caller. Its simplicity is useful when callers need only success or failure, but the limitation is real when a UI, API, or audit trail requires failure details. A future ADR may replace `null` with an explicit result type without weakening the always-valid rule.

# Theory Foundation

Evans's aggregates define consistency boundaries whose invariants must hold when an operation completes. Vernon recommends small aggregates and consistency within the aggregate boundary, which makes validated replacement practical. Bloch's guidance to minimize mutability, use defensive copies, and prevent partially initialized objects supports immutable transitions. Martin's encapsulation principles place the rule beside the state it protects. The GoF State pattern may be used when lifecycle states require distinct valid behaviors, but it must still prevent invalid transitions.
