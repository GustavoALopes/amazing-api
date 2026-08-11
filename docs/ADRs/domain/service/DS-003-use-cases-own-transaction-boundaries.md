# DS-003 - Use cases own transaction boundaries

# STATUS: APPROVED

# Context

## The problem (Analogy)

A bank transfer procedure defines when the complete operation starts, commits, or is reversed. An individual ledger clerk must not independently commit halfway through the procedure.

## The problem technical

A domain service does not know the complete application operation. If it starts or commits a transaction, a use case that combines several domain calls can expose partial results or require nested transaction behavior.

# Common usage

## Traditional solution

Services are annotated with framework transaction annotations because they call repositories, making each service method an implicit transaction boundary.

## Side-effects

Transaction scope follows class placement instead of the business operation. Nested calls become difficult to reason about, framework concerns enter the domain, and multi-step use cases can commit partially.

# Decision

## Solution

A domain service must never initialize, commit, or roll back a transaction and must not use transaction-framework annotations. The operation-specific use case owns the transaction boundary and invokes domain services inside it.

The transaction covers the complete state-changing use case. Repository implementations participate in that existing transaction; they do not cause the domain service to own it.

## Why it works better

The component that knows the full application operation also controls its atomic boundary. Domain services remain framework-independent and can be composed safely within a larger use case.

# Outcomes

## Benefits

- Transaction scope matches the complete use case.
- Multiple service calls can succeed or fail atomically.
- Domain services remain independent of transaction frameworks.
- Tests do not need implicit transactional behavior to invoke a service.

## Trade-offs

- Every state-changing use case must declare its transaction deliberately.
- Developers must ensure repository implementations join the active transaction.
- Long-running external calls require careful transaction design at the use-case layer.

## Superestimated trade-offs

Moving transaction control out of the service can look like lost safety. The transaction is not removed; it is raised to the layer with enough context to choose the correct scope. Very small use cases still retain an explicit and consistent boundary.

# Theory Foundation

Evans and Vernon distinguish application-service orchestration and transaction control from domain behavior. Clean Architecture places use-case coordination outside the domain model. This decision complements [API-003](../../api/API-003-use-cases-protect-domain-services.md), which requires application entry points to reach domain services through use cases.
