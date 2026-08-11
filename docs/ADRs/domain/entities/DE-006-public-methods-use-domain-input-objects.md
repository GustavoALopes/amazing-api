# DE-006 - Domain entity public methods use domain input objects

# STATUS: APPROVED

# Context

## The problem (Analogy)

A shipping company accepts a shipment through one manifest rather than asking the sender to hand over an unlabelled sequence of addresses, weights, dates, and service codes. The manifest gives every value a name, keeps related information together, and can evolve when the shipping process gains a new requirement.

Public operations on a domain entity form a similarly long-lived contract. Creation and state-transition methods need an input model that expresses the domain request as one cohesive concept.

## The problem technical

Public entity methods such as `createNew` often begin with a few individual parameters and accumulate more as the application evolves. Long parameter lists are difficult to read and easy to pass in the wrong order when values share a type. Adding or reorganizing data changes the method signature and every caller, encourages overloads, and makes the domain operation harder to evolve while preserving a clear contract.

Passing an API request DTO directly does not solve the boundary problem. An API DTO represents a transport contract and may contain serialization, validation, versioning, or presentation concerns that must not become dependencies of the domain model.

# Common usage

## Traditional solution

Factories and behavior methods accept each required value as a separate parameter. When the list becomes inconvenient, callers pass a controller request DTO or introduce several overloaded methods.

## Side-effects

Call sites become fragile and unclear, especially when several arguments have the same Java type. Routine domain changes ripple across the application, overloads drift into inconsistent behavior, and coupling to API DTOs makes the domain depend on an outer delivery layer.

# Decision

## Solution

Every public domain entity factory or behavior method that receives operation data must accept that data through a single, operation-specific domain input object instead of individual parameters. For example:

```java
public static Order createNew(CreateNewOrderInput input) {
    // Validate the input and create a valid Order.
}
```

The input object is part of the domain model. It is not an API DTO and must not depend on controllers, transport schemas, serialization annotations, or other delivery concerns. An application or API layer must map its own request model to the domain input object before invoking the entity.

Each input object must be placed in an `inputs` package inside the folder of the entity it belongs to. For example, an input for `Order.createNew` belongs under:

```text
domains/order/entities/inputs/CreateNewOrderInput.java
```

Input object names must describe the operation they support, such as `CreateNewOrderInput` or `ChangeDeliveryAddressInput`. They must expose only the values required by that domain operation and should be immutable. Validation of entity invariants remains the responsibility of the entity; the input object groups and names the requested data but does not replace entity validation.

Parameterless methods and read-only methods that receive no operation data are unaffected. A public operation must not retain a convenience overload that accepts the same data as individual parameters, because that would preserve a second, less maintainable contract.

## Why it works better

The method signature remains small and communicates one domain request. Named fields remove positional ambiguity, and new operation data can be introduced within the input contract without growing a long parameter list or multiplying overloads. Keeping the input beside its entity makes ownership explicit, while mapping at the application boundary prevents transport concerns from leaking into the domain.

# Outcomes

## Benefits

- Public entity operations have concise, intention-revealing signatures.
- Named input fields prevent mistakes caused by argument order.
- Related operation data evolves as one cohesive domain contract.
- Domain code remains independent of API and transport DTOs.
- Input ownership and discoverability are clear from package placement.
- Factories and transition methods avoid proliferating overloads as requirements grow.

## Trade-offs

- Each operation with input data requires an additional domain type.
- Application and delivery layers must map their DTOs to domain input objects.
- Small operations may initially appear to gain ceremony.
- Changing the meaning of an existing input field can still require coordinated caller changes.
- Input objects can become unfocused parameter bags unless they remain operation-specific.

## Superestimated trade-offs

An input object may look excessive for an operation that currently takes only one or two values. Domain operations tend to outlive their first signature, however, and adopting one consistent contract avoids deciding too late, after callers and overloads have spread. This rule does not justify a universal input shared across unrelated operations; such an object would hide intent and recreate coupling under a different form.

Mapping an API DTO to a structurally similar domain input can also appear redundant. The two types change for different reasons: the DTO follows a delivery contract, while the domain input follows the entity operation. The explicit mapping protects that separation and makes boundary changes visible.

# Theory Foundation

Evans's layered architecture keeps the domain model independent of user-interface and application delivery mechanisms, while ubiquitous language supports naming each domain operation and its data explicitly. Martin's Dependency Rule requires dependencies to point inward, so a domain entity cannot depend on an API DTO. Fowler's Parameter Object refactoring groups naturally related parameters, reduces long parameter lists, and gives the group a meaningful name. Bloch's guidance on immutable value-like objects supports making domain inputs safe to share and straightforward to reason about.
