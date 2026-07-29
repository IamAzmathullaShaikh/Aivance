# 2. Standardize Technical Stack

Date: 2026-07-28

## Status

Accepted

## Context

Need a modern, stable, and industry-standard tech stack for a long-term commercial project to ensure developer productivity, code quality, and ease of hiring.

## Decision

Standardize on the following technical stack and libraries:
- **Language**: Kotlin (latest stable version).
- **Dependency Injection**: Hilt (Google's recommendation for Android).
- **Persistence**: Room (standard SQLite abstraction).
- **Networking**: Retrofit (industry standard for REST APIs).
- **Preferences**: DataStore (modern replacement for SharedPreferences).
- **Asynchronous Programming**: Coroutines and Flow (native Kotlin support for reactive and async code).
- **UI Framework**: Jetpack Compose (modern declarative UI).

## Consequences

- **Pros**:
    - Faster development cycles using modern tools.
    - Type-safety and reduced boilerplate code.
    - Robust error handling and lifecycle-aware components.
    - Strong community support and documentation.
- **Cons**:
    - Learning curve for developers new to these technologies.
    - Need to keep dependencies up-to-date.
    - Hilt can sometimes increase build times slightly due to annotation processing.
