# 8. UI State Management with UDF

Date: 2026-07-28

## Status

Accepted

## Context

Managing UI state in a complex, multi-module app requires a predictable and scalable pattern to avoid bugs and ensure a smooth user experience.

## Decision

Adopt Unidirectional Data Flow (UDF) using ViewModels, StateFlow, and Jetpack Compose.
- **State**: Each screen has a single source of truth (a data class representing the UI state) exposed by a ViewModel.
- **Events**: UI interactions are sent to the ViewModel as events (functions or sealed classes).
- **Updates**: The ViewModel updates the state, which is then observed and rendered by Compose.

## Consequences

- **Pros**:
    - Predictable UI behavior and easier debugging.
    - Improved testability of business logic.
    - Clean separation between UI and logic.
- **Cons**:
    - Can lead to more boilerplate for simple screens.
