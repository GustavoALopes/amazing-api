# API-002 - Controller DTOs use input and view model conventions

# STATUS: APPROVED

# Context

## The problem (Analogy)

A customs office uses different forms for goods entering and leaving a country. Even when the fields resemble each other, each form represents a different direction, audience, and contract.

## The problem technical

Request and response DTOs change for different reasons. Generic names such as `OrderDto` obscure direction and operation, while mixing them in one package encourages accidental reuse and exposes fields that do not belong to a particular API contract.

# Common usage

## Traditional solution

A single DTO is reused for create, update, and response payloads, often beside the controller or domain entity.

## Side-effects

Serialization concerns become coupled, request-only fields leak into responses, and changes to one operation unexpectedly affect others.

# Decision

## Solution

Controller DTOs must be placed below `application.controllers.dtos`:

- inbound request or payload models belong to `application.controllers.dtos.input` and are named `<Operation>InputModel`, for example `CreateNewOrderInputModel`;
- outbound client models belong to `application.controllers.dtos.view` and are named `<Operation>ViewModel`, for example `CreateNewOrderViewModel`.

DTOs must be operation-specific. They are transport models and must not be used as domain inputs or entities.

An inbound input model must encapsulate its mechanical mapping to the corresponding domain input through a `toDomain()` method when such a domain input exists. The controller calls this method instead of containing field-by-field mapping code. This keeps the controller focused on HTTP concerns and makes the transport-to-domain boundary discoverable beside the transport model.

`toDomain()` may instantiate the domain input and its nested input records, copy fields, and create request execution metadata such as `ExecutionContext`. It must not validate domain invariants, invoke entity or value-object factories, query repositories, normalize business values, or make business decisions. Domain interpretation and validation remain behind domain entity operations.

```java
public record ImportOrderInputModel(/* transport fields */) {
    public ImportOrderInput toDomain() {
        return new ImportOrderInput(/* mechanical field mapping */);
    }
}
```

## Why it works better

Package and type names state both direction and intent. Independent DTOs allow request and response contracts to evolve without coupling the domain or unrelated operations.

# Outcomes

## Benefits

- DTO direction and ownership are explicit.
- API evolution has a smaller impact radius.
- Sensitive or irrelevant fields are less likely to cross the boundary.
- Controllers do not accumulate repetitive DTO-to-domain construction code.

## Trade-offs

- Similar operations may require structurally similar classes.
- Explicit mapping code is required.

## Superestimated trade-offs

The extra types can look repetitive, but shared DTOs create semantic coupling even when their current fields are identical. Small, operation-specific records keep the cost low.

# Theory Foundation

The Interface Segregation Principle favors contracts tailored to each consumer. Evans's layered architecture and Martin's Dependency Rule keep transport representations outside the domain model.
