# 5. Dependency Injection with Hilt

Date: 2026-07-28

## Status

Accepted

## Context

A multi-module project requires a robust and scalable dependency injection (DI) solution to manage the object graph, ensure modularity, and facilitate testing.

## Decision

Use Hilt as the project's DI framework.
- **Scoping**: Use standard Hilt scopes (`@Singleton`, `@ViewModelScoped`, etc.) to manage component lifecycles.
- **Modularity**: Define Hilt modules within each feature and core module to provide local dependencies while maintaining encapsulation.
- **Entry Points**: Use `@AndroidEntryPoint` for Activities and Fragments, and `@HiltViewModel` for ViewModels.

## Consequences

- **Pros**:
    - Reduced boilerplate compared to standard Dagger.
    - Compile-time validation of the dependency graph.
    - Seamless integration with Jetpack components (ViewModel, WorkManager, Navigation).
    - Simplified testing with Hilt's testing APIs.
- **Cons**:
    - Annotation processing can slightly increase build times.
    - Requires understanding of Dagger concepts.
