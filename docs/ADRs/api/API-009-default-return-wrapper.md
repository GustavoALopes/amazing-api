# API-009 - Use-case view models are wrapped in DefaultReturn

# STATUS: APPROVED

# Context

## The problem (Analogy)

A courier does not deliver an item without its delivery note. The package contains the requested item, while the note communicates relevant conditions or observations about the delivery in a consistent place.

A use-case result similarly contains both the requested response data and messages produced while executing the operation. Clients of the application layer need one predictable envelope for both.

## The problem technical

If use cases return view models directly, there is no standard channel for the `Message` values produced by validation or domain processing. Individual operations may invent wrappers, discard messages, add fields to unrelated view models, or require controllers to reconstruct the operation outcome.

The cross-cutting module already provides the generic result envelope:

```java
public record DefaultReturn<T>(T data, Set<Message> messages) {
}
```

Its canonical type is `com.gustavo.dev.application.dtos.view.DefaultReturn`.

# Common usage

## Traditional solution

Use cases return an operation-specific view model directly. Controllers separately catch exceptions or collect messages and build endpoint-specific response objects.

## Side-effects

Response shapes differ between operations, messages can be lost or duplicated, and controllers acquire responsibility for interpreting domain results. Callers cannot rely on one use-case result contract.

# Decision

## Solution

Every use case that returns an operation-specific view model must encapsulate it in `DefaultReturn<T>` from the cross-cutting module, where `T` is the view-model type from `application.controllers.dtos.view`.

For example:

```java
public DefaultReturn<CreateNewOrderViewModel> execute(CreateNewOrderInput input) {
    var result = orderService.createNew(input);
    var viewModel = CreateNewOrderViewModel.from(result);
    return new DefaultReturn<>(viewModel, result.messages());
}
```

The `data` component contains the use case's view model. The `messages` component contains the relevant `Set<Message>` for the completed operation. A use case must not return the view model bare, define an operation-specific equivalent of `DefaultReturn`, or expose a domain entity as `data`.

The wrapper standardizes the application-layer result; it does not override HTTP semantics. Controllers remain responsible for translating the result into the status codes and headers defined by API-007. Collection data must still follow the pagination decision in API-004.

## Why it works better

All use cases expose data and messages through one known generic contract. View models remain focused on resource representation, controllers do not need to reconstruct domain outcomes, and cross-cutting response behavior can be implemented consistently.

# Outcomes

## Benefits

- Every use-case result has a predictable envelope.
- Domain and validation messages are preserved alongside response data.
- Operation-specific view models remain focused on their resource representation.
- Controllers require less operation-specific result assembly.
- Shared tooling can process use-case results uniformly.

## Trade-offs

- Callers must unwrap `data` even when no messages are present.
- The API application depends on the cross-cutting module's result contract.
- The meaning and lifecycle of `Message` values must remain consistent across domains.
- HTTP response bodies may need separate mapping when the public contract must not expose the internal envelope directly.

## Superestimated trade-offs

The wrapper can appear unnecessary for operations that currently produce no messages. A uniform return type prevents callers from branching on operation-specific conventions and allows messages to be added without changing the use-case method's outer return shape.

Wrapping a view model does not mean every HTTP endpoint must serialize `DefaultReturn` unchanged. The use-case contract and public wire contract are separate boundaries; controllers can preserve required HTTP representations while consuming the same application result structure.

# Theory Foundation

The Result pattern represents an operation's value and diagnostic outcome explicitly instead of relying on hidden side channels. The Generic Wrapper pattern provides a reusable envelope while preserving the concrete data type. Martin's boundary principles support keeping this application result distinct from both domain entities and transport-specific response behavior.
