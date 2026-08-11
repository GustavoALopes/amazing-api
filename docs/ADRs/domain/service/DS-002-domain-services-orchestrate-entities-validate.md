# DS-002 - Domain services orchestrate; entities validate

# STATUS: APPROVED

# Context

## The problem (Analogy)

A travel coordinator arranges the airline, hotel, and payment provider, but does not decide whether a passport is valid. The passport authority owns that rule; duplicating it at every travel desk would produce inconsistent decisions.

## The problem technical

When domain services validate entity rules, the same invariant is easily duplicated across services and entity entry points. Callers can bypass the service, and rule changes can leave the entity and service disagreeing about valid state.

# Common usage

## Traditional solution

A service loads records, checks fields with `if` statements, mutates an entity, and persists it. The entity acts mainly as a data holder.

## Side-effects

Entities can exist in invalid states, validation becomes dependent on the calling path, and services accumulate business rules alongside database and external-API coordination.

# Decision

## Solution

A domain service must not validate an entity's rules or reproduce its validation logic. Entity factories and behavior methods own and enforce all rules that protect that entity's state.

The domain service only orchestrates the operation: it uses external dependency abstractions to obtain required data, invokes entity operations with domain inputs, and passes the resulting entity to the appropriate dependency. It may react to an entity operation's success or failure, but it must not pre-implement the entity's rule to predict that result.

```java
public Order create(CreateNewOrderInputModel input) {
    var order = Order.createNew(input);
    inventoryGateway.reserveFor(order);
    return orderRepository.save(order);
}
```

## Why it works better

Every path through the model reaches one authoritative rule implementation. The service remains focused on coordination that cannot be performed by an entity alone, while the entity protects itself regardless of its caller.

# Outcomes

## Benefits

- Entity invariants have one owner.
- Direct entity calls cannot bypass validation.
- Services remain small and focused on collaboration.
- Rule tests can target the entity without repository or API setup.

## Trade-offs

- Entities require behavior-rich APIs instead of public mutation.
- Services must translate entity failures into an appropriate domain/application outcome.
- Rules requiring external facts require those facts to be obtained before invoking entity behavior.

## Superestimated trade-offs

Keeping validation in entities does not mean an entity performs database or network calls. The service obtains external facts and supplies domain data; the entity still makes the rule decision. Boundary validation may remain for fast user feedback, but it is not authoritative.

# Theory Foundation

Evans's entities and Vernon's aggregates protect their own invariants and consistency boundaries. Martin's Single Responsibility Principle separates changes to entity rules from changes to orchestration. This decision specializes [DE-005](../entities/DE-005-entities-own-validation-rules-and-metadata.md) for collaboration through domain services.
