# API-004 - Collection endpoints use Pageable and PagedModel

# STATUS: APPROVED

# Context

## The problem (Analogy)

A library catalog returns one numbered page and tells the reader how many pages exist. Delivering the entire catalog for every search would be slow and would leave the reader without navigation information.

## The problem technical

Unbounded collection responses have unpredictable memory, latency, and database costs. Returning a bare list also omits pagination metadata and encourages incompatible endpoint-specific conventions.

# Common usage

## Traditional solution

Endpoints return `List<T>` and later add optional page and size query parameters with custom response wrappers.

## Side-effects

Large datasets can exhaust resources, clients cannot navigate consistently, and pagination metadata differs across endpoints.

# Decision

## Solution

Every endpoint that returns a collection must accept Spring Data's `Pageable` parameter and return a Spring HATEOAS `PagedModel` whose content consists of view models. A collection endpoint must not return a bare `List`, `Set`, entity `Page`, or unbounded iterable.

The requested sort and page constraints must be passed to the query stack. Project-wide validation or defaults must cap page size.

## Why it works better

The request bounds database work and response size, while `PagedModel` provides one stable representation for content, page metadata, and navigation links.

# Outcomes

## Benefits

- Collection cost is bounded.
- Clients receive consistent navigation metadata.
- Pagination and sorting are expressed through standard Spring abstractions.

## Trade-offs

- Clients must handle a paged envelope.
- Defaults, maximum page size, and stable sorting require configuration.

## Superestimated trade-offs

Paging can seem unnecessary for small tables, but data size changes over time and adopting it later breaks response contracts. A sensible default keeps small collections simple to consume.

# Theory Foundation

REST representations should expose navigation relevant to resource traversal. Fowler's patterns for data-source access support limiting result sets at the query boundary instead of truncating materialized in-memory collections.

