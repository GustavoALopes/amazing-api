# DS-001 - Domain service methods accept domain inputs

# STATUS: APPROVED

# Context

## The problem (Analogy)

A warehouse accepts an internal picking order, not the web shop's HTTP form. The form can change with the storefront, while the picking order describes what the warehouse needs in its own language.

## The problem technical

Application DTOs represent delivery contracts and may contain serialization, presentation, or versioning concerns. Accepting them in a domain service reverses the dependency direction and makes domain behavior change when an outer boundary changes.

# Common usage

## Traditional solution

A controller or use case passes its request DTO directly to a domain service because the fields happen to match.

## Side-effects

The domain becomes coupled to the application or API, domain operations inherit transport terminology, and non-HTTP callers must construct an unrelated delivery type.

# Decision

## Solution

Every domain service method that receives operation data must receive an operation-specific domain input from the owning entity's `inputs` package, such as `domains.order.entities.inputs.CreateNewOrderInputModel`. A domain service must never accept an application or controller DTO.

The use-case layer maps its DTO to the domain input before calling the service. Parameters that are collaborators rather than operation data are governed by DS-004.

```java
public Order create(CreateNewOrderInputModel input) {
    // Coordinate repositories/APIs and invoke Order behavior.
}
```

## Why it works better

The service contract uses domain language and remains independent of delivery mechanisms. Each boundary can evolve its own model, with mapping making the translation explicit.

# Outcomes

## Benefits

- Application DTOs cannot leak into the domain.
- Domain operations have cohesive, intention-revealing inputs.
- HTTP, messaging, batch, and test callers share the same domain contract.
- Boundary changes are isolated by explicit mapping.

## Trade-offs

- The application layer must maintain mapping code.
- Each operation may require an additional input type.
- Structurally similar DTO and input types can look repetitive.

## Superestimated trade-offs

The mapping may appear wasteful when two types have identical fields. Their ownership and reasons to change are different, so a small mapping cost prevents long-lived boundary coupling. A shared generic input would remove that protection.

# Theory Foundation

Martin's Dependency Rule requires source-code dependencies to point toward the domain. Evans's layered architecture similarly keeps the domain model independent of presentation concerns. Fowler's Parameter Object supports grouping operation data behind a meaningful contract. This decision extends [DE-006](../entities/DE-006-public-methods-use-domain-input-objects.md) from entity methods to domain services.
