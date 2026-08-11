# DE-007 - Domain entity setters are private boolean validators behind business operations

# STATUS: APPROVED

# Context

## The problem (Analogy)

A bank account holder does not edit the account balance directly. The holder requests a business operation, such as a withdrawal, through the bank's public service counter. The bank checks the withdrawal rules and changes its internal ledger only when every rule is satisfied. The ledger-writing mechanism is private and reports whether it accepted the value.

A domain entity needs the same separation. Callers request a meaningful business operation through a public port; they do not assign properties directly. Internal assignment must still enforce the rules owned by the entity.

## The problem technical

Public setters expose implementation details as an unrestricted mutation API. They let callers change one property without expressing the business reason, bypass transition rules, and place an entity in an invalid or partially updated state. A private setter that assigns without validation merely hides the same weakness inside the class: factories or future behavior methods can still introduce invalid state.

The project already requires entities to remain valid, to use copy-on-change transitions, and to own their validation metadata. A consistent internal assignment contract is needed so every property is checked against that metadata before it becomes part of a candidate entity.

# Common usage

## Traditional solution

Java entities commonly expose `void setProperty(value)` methods for frameworks and application services. Validation is performed in a controller, DTO, service, annotation, or database constraint before or after those setters are called.

```java
public void setName(String name) {
    this.name = name;
}
```

## Side-effects

The public API describes storage operations instead of domain behavior. Any caller can bypass the intended use case, validation becomes dependent on call order and caller discipline, and a multi-property update can fail after some fields were already assigned. A `void` setter also gives its internal caller no immediate success signal with which to reject a candidate entity.

# Decision

## Solution

Every method that assigns a domain entity property and follows setter semantics must be `private` and return `boolean`.

Before assigning, the setter must validate the proposed value using the authoritative metadata rules owned by that entity, as established by DE-005. It returns `false` without assigning when validation fails. It assigns the value and returns `true` when validation succeeds. Setters must not be exposed as `public`, `protected`, or package-private, including solely for persistence frameworks.

```java
private boolean setName(String name) {
    if (name == null
            || name.length() < NameRule.MIN_LENGTH
            || name.length() > NameRule.MAX_LENGTH) {
        return false;
    }

    this.name = name;
    return true;
}
```

A domain entity may be created or changed only through a public business operation: a public factory for creation or an intention-revealing public behavior method for a transition. These methods are the entity's public ports. Names such as `createNew`, `rename`, `activate`, or `changeDeliveryAddress` express business intent; a public `setName` is not a business operation.

In accordance with DE-003, a change operation must not mutate the observable original entity. It constructs a candidate replacement, invokes all required private setters, and publishes the replacement only if every setter and every cross-property or transition invariant succeeds. It returns the failure result required by the entity contract when any setter returns `false`; the original remains unchanged and valid.

```java
public Customer rename(RenameCustomerInput input) {
    Customer candidate = copyOf(this);

    if (!candidate.setName(input.name())) {
        return null;
    }

    return candidate;
}
```

The boolean result is the mandatory internal setter contract; it is not required to be the return type of the public business operation. A public operation returns the valid entity or result prescribed by the domain API. It may coordinate several setters and must also validate rules involving multiple properties, lifecycle transitions, external policy, or the aggregate as a whole. A private setter validates the property rules it owns but does not replace operation-level validation.

Persistence tools must use field access, constructor hydration, or another infrastructure mechanism that does not require widening setter visibility. Reconstitution remains the controlled infrastructure exception described by DE-003 and must not expose an invalid entity to application code.

## Why it works better

Callers see a small API expressed in ubiquitous business language and cannot select arbitrary assignments. Every internal assignment has the same explicit success-or-failure protocol, and colocating the checks with the assignment prevents a factory or later transition method from accidentally skipping property validation. Applying setters only to an unpublished candidate preserves atomic copy-on-change behavior: either a complete valid replacement is returned, or no observable entity changes.

# Outcomes

## Benefits

- Public entity APIs describe business capabilities rather than field mutation.
- Property assignment cannot bypass entity-owned metadata validation.
- Failed private setters do not modify their property.
- Boolean results make validation failure explicit to factories and behavior methods.
- Candidate construction preserves the always-valid and copy-on-change guarantees.
- Persistence requirements do not enlarge the domain mutation API.

## Trade-offs

- Factories and change operations must check every setter result and handle failure consistently.
- A boolean identifies failure but does not explain which rule failed; richer diagnostics must be produced by an operation-level result when required.
- Multi-property and transition invariants still need explicit operation-level validation.
- Copying a candidate introduces code and allocation overhead.
- Persistence configuration may require field access or custom mapping.

## Superestimated trade-offs

Private setters can appear incompatible with object-relational mapping. Mature persistence tools support field access or private-member access, so public setters are usually unnecessary. The concern becomes real only when a selected tool mandates public mutation, in which case that tool conflicts with the domain boundary and requires an explicit architectural exception.

Returning `boolean` from every private setter may appear to provide weak error reporting. These setters are internal guard mechanisms, not user-facing validation APIs. A public business operation may translate a failed assignment into a richer domain result. The limitation becomes genuine if callers need to distinguish several failures and the operation exposes only `null` or a boolean; that concern belongs to the public result contract.

# Theory Foundation

Evans's entities and aggregates protect invariants through behavior expressed in the ubiquitous language; public business operations therefore represent allowed transitions while raw setters remain hidden. Vernon's aggregate consistency rules support validating a complete transition before publishing it. Martin's encapsulation and Tell, Don't Ask principles support asking an entity to perform a business operation instead of retrieving and assigning its data. Bloch's guidance to minimize mutability and restrict member accessibility supports private assignment mechanisms, while DE-003's copy-on-change rule prevents partial observable mutation. DE-005 supplies the single authoritative metadata used by each setter.
