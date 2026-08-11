# Domain Service ADRs

This directory contains the approved Architecture Decision Records for domain services.

## Approved ADRs

- [DS-001 - Domain service methods accept domain inputs](DS-001-domain-service-methods-accept-domain-inputs.md)
- [DS-002 - Domain services orchestrate; entities validate](DS-002-domain-services-orchestrate-entities-validate.md)
- [DS-003 - Use cases own transaction boundaries](DS-003-use-cases-own-transaction-boundaries.md)
- [DS-004 - Domain services depend on interfaces](DS-004-domain-services-depend-on-interfaces.md)
- [DS-005 - Domain services are concrete classes in domain.services](DS-005-concrete-domain-services-package.md)
- [DS-006 - Domain services must not depend on domain services](DS-006-domain-services-must-not-depend-on-domain-services.md)

## Conventions

- Use Markdown and one primary decision per ADR.
- Prefix domain-service ADRs with the next sequential `DS-NNN` identifier.
- New decisions use `STATUS: APPROVED`; retain superseded records as history.
- Package examples are relative to `com.gustavo.dev.api.domains.<domain>`.
