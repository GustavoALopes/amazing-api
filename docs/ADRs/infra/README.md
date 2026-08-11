# Infrastructure ADRs

This directory contains the approved Architecture Decision Records for infrastructure concerns.

## Approved ADRs

- [INFRA-001 - Database access is centralized in repositories](INFRA-001-centralize-database-access-in-repositories.md)
- [INFRA-002 - Write-side repositories return domain entities](INFRA-002-write-repositories-return-domain-entities.md)
- [INFRA-003 - Read-only repositories return record projections](INFRA-003-read-only-repositories-return-record-projections.md)

## Conventions

- Use Markdown and one primary decision per ADR.
- Prefix infrastructure ADRs with the next sequential `INFRA-NNN` identifier.
- New decisions use `STATUS: APPROVED`; retain superseded records as history.
- Package examples are relative to `com.gustavo.dev.api`.
- `<Entity>` is a placeholder for the resource name, for example `OrderRepository` or `OrderRepositoryReadOnly`.

