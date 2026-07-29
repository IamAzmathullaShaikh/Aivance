# 6. Local Persistence Strategy

Date: 2026-07-28

## Status

Accepted

## Context

The application needs to store structured data (like job applications) and simple user preferences (like theme settings) locally to ensure offline functionality and a personalized experience.

## Decision

- **Structured Data**: Use Room for relational data persistence. Define entities, DAOs, and a central `AivanceDatabase`.
- **Preferences**: Use Jetpack DataStore (Proto or Preferences) for simple key-value pairs and user settings, replacing the deprecated SharedPreferences.
- **Data Flow**: Expose data from persistence layers as Kotlin Flows to ensure the UI stays in sync with the database.

## Consequences

- **Pros**:
    - Type-safe database access with Room.
    - Reactive data updates with Flow.
    - Modern, non-blocking preferences management with DataStore.
- **Cons**:
    - Overhead of database migrations for Room.
    - Initial setup for DataStore serializers.
