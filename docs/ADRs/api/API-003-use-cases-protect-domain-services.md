# API-003 - Application code accesses domain services through use cases

# STATUS: APPROVED

# Context

## The problem (Analogy)

A bank teller does not directly manipulate the ledger for a transfer. The teller submits a transfer procedure that authenticates the request, controls the transaction, and coordinates all ledger changes.

## The problem technical

Calling domain services directly from controllers or other application entry points distributes transaction control and mapping logic. It also allows transport DTOs to reach domain APIs and makes one business operation difficult to test as a unit.

# Common usage

## Traditional solution

A controller injects a domain service, maps only convenient fields, and relies on framework transaction behavior around individual service calls.

## Side-effects

Transaction boundaries become accidental, orchestration is duplicated, and partial changes may be committed when an operation spans multiple collaborators.

# Decision

## Solution

Code in the application delivery layer must never access a domain service directly. Commands must be executed through an operation-specific use case in `application.usecase` (or its subpackages).

The use case owns the business transaction and orchestration. The controller boundary translates its application input DTO into the operation-specific domain input before invoking the use case. As defined in API-008, use cases receive domain inputs, invoke domain services rather than entities directly, and return view models. Domain services and domain inputs must not depend on controller DTOs.

## Why it works better

One use case represents one application operation, giving it an explicit transaction boundary and a single place for domain-service coordination while domain policy remains independent of HTTP.

# Outcomes

## Benefits

- Transaction ownership is explicit.
- Controllers remain thin.
- Application DTOs cannot leak into the domain.
- Business operations can be tested independently of HTTP.

## Trade-offs

- Each command requires a use-case type and mapping.
- Simple write operations contain more explicit orchestration.

## Superestimated trade-offs

Use cases may initially look like pass-through classes. They still establish the correct dependency and transaction boundary, and provide a stable home when coordination grows.

# Theory Foundation

Clean Architecture places use cases at the application-business-rule boundary and requires delivery mechanisms to invoke them. Evans and Vernon distinguish application orchestration and transaction control from domain policy.
