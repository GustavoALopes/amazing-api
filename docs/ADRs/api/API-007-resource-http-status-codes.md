# API-007 - Resource outcomes use consistent HTTP status codes

# STATUS: APPROVED

# Context

## The problem (Analogy)

A parcel service gives different receipts for a delivered parcel, an accepted shipment still being processed, and an address that cannot be found. Reusing one receipt for all outcomes forces the customer to guess what happened.

## The problem technical

Clients rely on HTTP status codes and headers to distinguish missing resources, completed creation, and asynchronous acceptance. Returning `200 OK` indiscriminately creates ambiguous contracts and prevents standard client behavior.

# Common usage

## Traditional solution

Endpoints return `200 OK` with a message body for every successful or unsuccessful business outcome, and creation responses omit the resource URI.

## Side-effects

Clients parse custom messages, cannot reliably identify a newly created resource, and may mistake accepted background work for completed work.

# Decision

## Solution

Resource endpoints must apply these semantics:

- return `404 Not Found` whenever the addressed resource does not exist;
- return `201 Created` when a resource is created synchronously and include a `Location` header containing the canonical URI of the created resource;
- return `202 Accepted` when processing is asynchronous and has been accepted but is not complete.

If the creation command does not already provide enough information to build the canonical `Location`, the application must retrieve the created resource through the query stack defined by API-005 and build the URI from that result. It must not expose an entity or query a repository directly from the controller. When an asynchronous operation exposes a status resource, its URI should be supplied in `Location`.

The API's exception or outcome mapping must enforce these rules consistently rather than duplicating ad hoc response logic in every controller.

## Why it works better

Standard semantics let clients react using the protocol itself. The `Location` header makes synchronous creation discoverable, while `202` truthfully communicates that completion has not occurred.

# Outcomes

## Benefits

- Clients can distinguish outcomes without parsing messages.
- Created resources have a canonical discoverable URI.
- Asynchronous work is not represented as complete.
- Missing resources behave consistently.

## Trade-offs

- Creation flows must retain or query the new resource identifier.
- Central exception and response mapping must be maintained.
- Asynchronous APIs may need a status resource and polling contract.

## Superestimated trade-offs

Resolving a canonical URI can look like needless work when an identifier is already known. No extra query is required in that case; the query stack is the fallback when the command result is insufficient. The rule becomes costly only when commands routinely discard identifiers, which should prompt improvement of their result contract.

# Theory Foundation

HTTP semantics define `201 Created` for completed creation with `Location` identifying a primary created resource, `202 Accepted` for accepted but incomplete processing, and `404 Not Found` when the origin server did not find a current representation for the target resource. REST uses these uniform protocol semantics to reduce client-server coupling.

