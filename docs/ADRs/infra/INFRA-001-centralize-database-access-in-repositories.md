# INFRA-001 - Database access is centralized in repositories

# STATUS: APPROVED

# Context

## The problem (Analogy)

A company keeps its financial records at one accounting desk rather than letting every department update the ledger independently. A single access point makes changes traceable and rules consistent.

## The problem technical

Database operations spread across controllers, use cases, queries, domain services, and infrastructure services create multiple persistence conventions. Transaction behavior, query construction, mapping, and error handling then become difficult to review and change safely.

# Common usage

## Traditional solution

Any component that needs data injects an `EntityManager`, Spring Data interface, JDBC client, or another persistence API and performs the operation directly.

## Side-effects

Persistence concerns leak across architectural boundaries, database behavior is duplicated, and callers become coupled to the selected persistence technology.

# Decision

## Solution

All database operations must be implemented by repository objects under `infra.data.repositories`.

Application and domain code must access persisted data only through repository interfaces appropriate to their boundary. Controllers, use cases, queries, domain services, and other non-repository components must not execute database operations directly or depend directly on persistence APIs.

A repository owns the persistence details for its responsibility, including query execution and conversion between persistence data and its permitted return type. This rule applies to reads and writes; the return-type rules for command-side and read-only repositories are defined separately.

## Why it works better

One recognizable package becomes the persistence boundary. Database technology and mapping details remain replaceable, while authorization, transaction, and query behavior are easier to inspect and test consistently.

# Outcomes

## Benefits

- Persistence concerns have a single, discoverable home.
- Application and domain code remain independent of database APIs.
- Mapping and query behavior can be changed without altering repository consumers.
- Database access is easier to audit and test.

## Trade-offs

- Even simple database operations require a repository contract and implementation.
- Repository interfaces and adapters add types to the codebase.

## Superestimated trade-offs

Centralization may appear to create large repository classes. The rule centralizes responsibility in repository objects, not in one universal repository; implementations should remain cohesive and resource-specific.

# Theory Foundation

The Repository pattern mediates between the domain and data-mapping layers through collection-like interfaces. Dependency inversion keeps higher-level policy independent of persistence mechanisms, while persistence adapters implement the contracts at the infrastructure boundary.

