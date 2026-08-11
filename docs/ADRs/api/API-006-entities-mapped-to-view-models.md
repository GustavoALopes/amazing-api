# API-006 - Domain entities are never returned to clients

# STATUS: APPROVED

# Context

## The problem (Analogy)

A restaurant serves a plated meal, not the kitchen's storage container. Even when both currently contain the same food, the serving contract and the kitchen's internal organization have different purposes.

## The problem technical

Serializing an entity makes its fields, relationships, persistence annotations, and loading behavior part of the external contract. Domain changes can then break clients, and clients may gain access to information that was never intentionally published.

# Common usage

## Traditional solution

Controllers return entities directly because their fields initially match the desired JSON response.

## Side-effects

Lazy-loading failures, recursive object graphs, accidental data disclosure, unstable JSON, and domain changes coupled to API versioning can result.

# Decision

## Solution

A domain entity must never be returned directly to a client. Every response must map data to an operation-specific view model in `application.controllers.dtos.view`, even when the view model is structurally identical to the entity at the time it is introduced.

The mapping must be explicit and occur before the controller response is serialized. Collection responses contain view models inside `PagedModel`, never entities.

## Why it works better

The view model is an intentional public contract. Domain and persistence structures can evolve without silently changing the API, and every exposed field is selected deliberately.

# Outcomes

## Benefits

- API contracts are stable and intentional.
- Persistence details and lazy associations stay internal.
- Field-level exposure is easy to review.
- Domain refactoring is decoupled from client versioning.

## Trade-offs

- Mapping code and response types must be maintained.
- Identical models temporarily duplicate field declarations.

## Superestimated trade-offs

The duplicate shape may look wasteful, but equivalence today does not mean shared ownership. Records and focused mapper functions make the isolation inexpensive.

# Theory Foundation

The Data Transfer Object pattern packages data for a remote boundary. Information Hiding and the Dependency Rule prevent an internal domain representation from becoming an accidental public interface.

