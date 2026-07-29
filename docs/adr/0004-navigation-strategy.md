# 4. State-Driven Adaptive Navigation

Date: 2026-07-28

## Status

Accepted

## Context

Aivance is intended to be a modern Android application that provides a seamless experience across various form factors, including phones, tablets, and foldables. Managing complex backstacks and ensuring consistent navigation behavior across these devices can be challenging with traditional navigation approaches.

## Decision

Adopt Jetpack Navigation 3 as the primary navigation framework.
- **Type Safety**: Use type-safe `Destination` objects (using Kotlin Serialization) to define routes, ensuring compile-time safety and reducing runtime errors.
- **State-Driven**: Implement navigation as a function of the application state, allowing for more predictable and testable transitions.
- **Adaptive UI**: Leverage `NavigationSuiteScaffold` and other adaptive components to automatically adjust the navigation UI (e.g., from a bottom bar on phones to a navigation rail on tablets).
- **Multi-Module Integration**: Centralize navigation logic within the `:navigation` module while allowing features to define their own internal routes.

## Consequences

- **Pros**:
    - Predicable and manageable navigation state.
    - Native and robust support for multi-window, large screens, and foldables.
    - Improved developer experience through type-safe APIs.
    - Better separation of concerns by decoupling navigation from UI components.
- **Cons**:
    - Requires a learning curve for the team to become proficient with Navigation 3 APIs.
    - Navigation 3 is relatively new, which might lead to encountering early-stage limitations or evolving best practices.
