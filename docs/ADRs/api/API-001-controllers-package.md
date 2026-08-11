# API-001 - Controllers belong to the application controllers package

# STATUS: APPROVED

# Context

## The problem (Analogy)

A public building uses one reception area so visitors know where to enter and staff know where requests cross into internal operations. Entrances scattered through private offices make access hard to govern.

## The problem technical

HTTP controllers are inbound adapters. If they are placed beside domain entities, configuration, or persistence code, the delivery boundary becomes unclear and transport concerns can leak into business code.

# Common usage

## Traditional solution

Controllers are grouped by feature and placed wherever the related implementation happens to live.

## Side-effects

Package scanning, security review, dependency rules, and controller discovery become inconsistent. Feature proximity also makes it easier for controllers to bypass the application layer.

# Decision

## Solution

Every HTTP controller must be placed in the `application.controllers` package or one of its feature-specific subpackages. Controllers must not be declared under domain, persistence, configuration, or query packages.

## Why it works better

The package forms a visible inbound boundary. Architectural checks can identify controllers reliably and enforce their allowed dependencies.

# Outcomes

## Benefits

- Controllers have one predictable location.
- Transport concerns remain outside the domain.
- Package scanning and architecture tests are straightforward.

## Trade-offs

- Controller files may be physically separated from domain code for the same feature.
- Existing controllers outside the package require migration.

## Superestimated trade-offs

The separation can appear to hurt discoverability, but feature subpackages and IDE navigation retain cohesion without weakening the layer boundary.

# Theory Foundation

Hexagonal Architecture treats controllers as inbound adapters. Robert C. Martin's Dependency Rule requires this outer delivery mechanism to depend inward rather than be mixed with domain policy.

