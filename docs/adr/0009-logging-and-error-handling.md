# 9. Logging and Error Handling Strategy

Date: 2026-07-28

## Status

Accepted

## Context

Consistent logging and error handling are crucial for monitoring app health and providing a good user experience when things go wrong.

## Decision

- **Logging**: Use Timber for all logging. Plant different trees for debug (Logcat) and production (e.g., Crashlytics) builds.
- **Error UI**: Use a centralized `ErrorUI` component in the `:core:designsystem` to show consistent error messages and retry actions.
- **Domain Errors**: Map technical errors (network, database) to domain-specific error types in the repository layer.

## Consequences

- **Pros**:
    - Clean and manageable logging across the app.
    - Consistent user experience during error states.
    - Easier troubleshooting in production.
- **Cons**:
    - Initial setup of the error mapping layer.
