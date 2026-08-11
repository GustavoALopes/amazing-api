---
name: create-domain-entity
description: Create or implement a Java domain entity and any requested value objects according to this repository's domain entity ADRs. Use when the user invokes ./create-domain-entity, asks to create a domain entity, aggregate root, entity mapping, or ADR-compliant domain model.
---

# Create a domain entity

Create production code that follows the repository's approved domain entity decisions.

## Gather the specification

Ask these five questions before implementing, even when some answers appear implied. Combine them into one concise numbered prompt:

1. What is the entity name?
2. Which domain does the entity belong to?
3. What is the database table name?
4. What are the fields? For each field, indicate whether it is a Value Object.
5. What validation and business rules apply to creation and modification?

If the initial command already contains answers, restate the inferred answers beside the questions and ask the user to confirm or correct them. Do not create files until all five answers are available.

## Load repository rules

Before designing or editing code:

1. Read `docs/ADRs/domain/README.md` completely.
2. Read every approved `DE-*.md` ADR in `docs/ADRs/domain` completely.
3. If `docs/ADRs/domain/entities` exists in the future, also read every ADR in it completely.
4. Read `docs/templates/domain/entities/SimpleAggregateRoot.java` and `BirthDate.java` as the canonical implementation examples.
5. Inspect the target module, neighboring domain types, persistence mappings, tests, Java version, and repository instructions such as `AGENTS.md`.

The ADRs are authoritative. If the requested design conflicts with an ADR, identify the conflict and ask whether the ADR should be changed; do not silently violate it.

## Design the entity

- Always place the entity in `api/src/main/java/com/gustavo/dev/api/domains/<domain>/entities`, replacing `<domain>` with the domain supplied by the user.
- Use the matching Java package `com.gustavo.dev.api.domains.<domain>.entities`.
- Place domain-specific Value Objects beneath the same user-specified domain, following an existing value-object subpackage convention when one exists. If none exists, use `api/src/main/java/com/gustavo/dev/api/domains/<domain>/entities/valueobjects` and package `com.gustavo.dev.api.domains.<domain>.entities.valueobjects`.
- Treat the domain answer as a Java package segment. Ask the user to resolve it if it cannot be represented as a valid package path without changing its meaning.
- Never place a generated domain entity outside `api/src/main/java/com/gustavo/dev/api/domains/<domain>`.
- Make each concrete entity `final`.
- Implement `IAggregateRoot` only when the entity is an aggregate root.
- Keep domain constructors private.
- Add only the narrow Hibernate no-argument constructor required by the project's persistence configuration, with the narrowest supported visibility, and document it as infrastructure-only.
- Expose named public static factory methods for supported creation cases.
- Validate before returning an entity. Follow the approved ADR outcome contract, including returning `null` on failed validation.
- Never expose a partially initialized entity to application code.
- Express modifications as domain-named methods that validate first and return a new valid instance while preserving entity identity.
- Keep fields and collections immutable from callers; use defensive copies where needed.
- Put entity-owned validation metadata in meaningfully named nested `public static final` rule classes with immutable `public static final` fields.
- Keep cross-entity rules in the smallest correct domain abstraction instead of forcing them into one entity.
- Use JPA mappings that match the supplied table and field definitions without making persistence annotations the source of truth for domain validation.

## Design value objects

For each field marked as a Value Object:

- Create a dedicated final type unless an existing compatible type already exists.
- Give it controlled construction and enforce its invariants in public factories.
- Implement value equality and a consistent hash code.
- Keep it immutable from domain callers.
- Add an infrastructure-only Hibernate constructor and mapping only when persistence requires them.
- Keep rules intrinsic to the value object inside that value object.

## Implement and verify

1. Create or update the entity, value objects, persistence mapping, and focused tests within the requested scope.
2. Test valid creation and every invalid rule boundary.
3. Test that failed factories and modifications return `null`.
4. Test that modifications return a new instance, preserve entity identity, leave the original unchanged, and produce a valid result.
5. Add persistence integration coverage when final classes, constructor visibility, embedded values, or Hibernate hydration could fail.
6. Run the smallest relevant test suite, then any broader repository verification justified by the change.
7. Report created files, modeled rules, and verification results concisely.
