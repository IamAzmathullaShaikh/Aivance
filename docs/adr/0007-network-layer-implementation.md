# 7. Network Layer Implementation

Date: 2026-07-28

## Status

Accepted

## Context

The app needs to communicate with remote APIs for job searches, resume analysis, and other AI-powered features.

## Decision

- **Retrofit**: Use Retrofit for defining and consuming REST APIs.
- **OkHttp**: Use OkHttp as the underlying HTTP client, configured with interceptors for logging and authentication.
- **Serialization**: Use Kotlin Serialization for JSON parsing to ensure type safety and multi-platform compatibility.
- **Error Handling**: Implement custom interceptors and error mappers to convert network errors into domain-specific exceptions.

## Consequences

- **Pros**:
    - Industry-standard networking stack.
    - Highly customizable and extensible through interceptors.
    - Efficient JSON parsing with Kotlin Serialization.
- **Cons**:
    - Requires defining interfaces for each API service.
