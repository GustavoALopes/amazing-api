# INFRA-003 - Read-only repositories return record projections

# STATUS: APPROVED

# Context

## The problem (Analogy)

A dashboard receives a purpose-built report rather than the organization's working ledger. The report contains exactly what the reader needs and cannot be mistaken for a record that should be edited.

## The problem technical

Read paths that return managed domain entities load behavior and state that presentation does not need. They also risk exposing aggregates outside the domain workflow and encourage view mapping after unnecessarily broad database queries.

# Common usage

## Traditional solution

Read repositories load entities and later map them to DTOs, or return untyped arrays and tuples assembled by ad hoc queries.

## Side-effects

Queries select excess data, managed entities escape into presentation paths, and untyped query results make constructor order and field types fragile.

# Decision

## Solution

A repository implementation under `infra.data.repositories` whose name ends in `<Entity>RepositoryReadOnly` must:

- implement an interface declared under `application.queries.repositories`;
- return view models declared under `application.controllers.dtos.view`;
- never return a domain entity;
- use projection queries for returned rows; and
- model each projected view as a Java `record`.

Read-only repository implementations must build projection queries with `EntityManager.createQuery(...)`. JPQL constructor expressions must construct the view record directly, selecting only the fields required by that view. Typed queries must be used whenever `createQuery` offers a typed overload.

For example, `OrderRepositoryReadOnly` implements an application query repository interface and projects query results directly into an `OrderViewModel` record. It does not load `Order` domain entities as an intermediate result.

The `RepositoryReadOnly` suffix expresses a strict architectural role, not merely a naming preference. A repository that must return domain entities belongs to the domain repository contract and must not use this suffix.

## Why it works better

The query boundary owns client-oriented data shapes and can retrieve only the required columns. Immutable records provide explicit, typed projection constructors, while the repository name makes read-model behavior visible during review.

# Outcomes

## Benefits

- Read queries avoid loading complete aggregates unnecessarily.
- Domain entities cannot leak through query-oriented repositories.
- Java records provide immutable and strongly typed view models.
- Query repository contracts live with their application query consumers.
- Projection selection and result construction remain in one database query.

## Trade-offs

- Each view shape may require its own record and query expression.
- Refactoring a record constructor requires updating its JPQL constructor expression.
- Read and write repository contracts evolve independently.

## Superestimated trade-offs

Separate projections may appear duplicative when their fields resemble an entity. Their purpose and evolution differ: entities protect business invariants, while view records provide stable, minimal response shapes.

# Theory Foundation

Command Query Responsibility Segregation allows the read model to differ from the write model. The Projection pattern retrieves only the data required by a consumer, and immutable records prevent read results from being confused with managed aggregates.

