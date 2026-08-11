# API-005 - Read operations use the query stack

# STATUS: APPROVED

# Context

## The problem (Analogy)

A museum visitor asks the information desk where an exhibit is; the staff consult the catalog instead of opening the display case and handling the artifact. Reading information should not require a path designed for changing it.

## The problem technical

Retrieving current database state through repositories, entities, or command use cases couples read shapes to the write model. It can load unnecessary aggregates and makes it unclear whether a request is allowed to mutate state.

# Common usage

## Traditional solution

Controllers call repositories or reuse domain services for both reads and writes, then serialize the returned entities.

## Side-effects

Read performance follows aggregate-loading constraints, presentation mapping spreads across controllers, and supposedly read-only paths can accidentally modify managed entities.

# Decision

## Solution

Requests that retrieve current state without modifying entities must use a query implementation under `application.queries`. Each domain resource normally has a dedicated query facade, such as `OrderQueries` or `ProductQueries`.

Queries must return projections or view-ready data rather than managed domain entities. They must honor filtering, sorting, and `Pageable` requirements and must not contain command-side business mutations.

## Why it works better

Read models can be shaped and optimized for the client while the write model remains focused on invariants and state transitions. The package makes read-only intent reviewable.

# Outcomes

## Benefits

- Read and write responsibilities are explicit.
- Queries can select only required data.
- Domain aggregates are not exposed for presentation.
- Collection pagination is handled close to the data source.

## Trade-offs

- Query models and mappings add types.
- Read and write representations may evolve separately.

## Superestimated trade-offs

The separate stack may appear to duplicate repository methods. Query code serves a different consumer and consistency need; the concern becomes real only if identical logic is copied instead of shared at an appropriate infrastructure boundary.

# Theory Foundation

Command Query Separation distinguishes operations that return state from those that change it. CQRS permits read representations to differ from aggregate write models without requiring separate physical databases.

