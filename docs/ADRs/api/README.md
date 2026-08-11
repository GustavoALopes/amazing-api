# API ADRs

This directory contains the approved Architecture Decision Records for the HTTP API and application layer.

## Approved ADRs

- [API-001 - Controllers belong to the application controllers package](API-001-controllers-package.md)
- [API-002 - Controller DTOs use input and view model conventions](API-002-controller-dto-conventions.md)
- [API-003 - Application code accesses domain services through use cases](API-003-use-cases-protect-domain-services.md)
- [API-004 - Collection endpoints use Pageable and PagedModel](API-004-paged-collections.md)
- [API-005 - Read operations use the query stack](API-005-query-stack-for-reads.md)
- [API-006 - Domain entities are never returned to clients](API-006-entities-mapped-to-view-models.md)
- [API-007 - Resource outcomes use consistent HTTP status codes](API-007-resource-http-status-codes.md)
- [API-008 - Use cases depend on domain inputs and domain services](API-008-use-case-boundaries.md)
- [API-009 - Use-case view models are wrapped in DefaultReturn](API-009-default-return-wrapper.md)

## Conventions

- Use Markdown and one primary decision per ADR.
- Prefix API ADRs with the next sequential `API-NNN` identifier.
- New decisions use `STATUS: APPROVED`; retain superseded records as history.
- Use the package examples relative to `com.gustavo.dev.api`.
