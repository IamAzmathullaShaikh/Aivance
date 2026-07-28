# Aivance Architecture Guide

This document provides an overview of the Aivance Android application's architecture, module structure, and technical stack.

## Clean Architecture Overview

Aivance follows a multi-module Clean Architecture approach, ensuring separation of concerns, scalability, and testability. The project is divided into several layers:

1.  **App Module**: The entry point of the application. It initializes the dependency injection graph and sets up the main navigation.
2.  **Feature Modules**: Contain the UI and business logic for specific functional areas (e.g., Dashboard, Resume, Jobs).
3.  **Navigation Module**: Centralizes navigation logic using Jetpack Navigation 3.
4.  **Core Modules**: Provide shared infrastructure, data sources, and design system components.

---

## Module Breakdown

### 1. `:app`
- **Purpose**: Application entry point and orchestrator.
- **Key Files**: `MainActivity.kt`, `AivanceApplication.kt`.
- **Dependencies**: Depends on `:navigation` and all feature modules to assemble the final app.

### 2. `:feature:*`
- **Examples**: `:feature:dashboard`, `:feature:resume`, `:feature:jobs`.
- **Purpose**: Each module represents a self-contained feature.
- **Components**:
    - **UI**: Compose screens and components.
    - **ViewModel**: Manages UI state and business logic.
    - **Domain (planned)**: Use cases for complex logic.
- **Dependencies**: Depends on `:core:designsystem`, `:core:common`, and other relevant core modules.

### 3. `:navigation`
- **Purpose**: Manages app-wide navigation.
- **Key Files**: `AivanceNavGraph.kt`, `Destination.kt`.
- **Tech**: Jetpack Navigation 3.
- **Dependencies**: Depends on all feature modules.

### 4. `:core:*`
- **`:core:designsystem`**: Contains Material 3 theme, dynamic color setup, and reusable UI components.
- **`:core:database`**: Room database setup and local data sources.
- **`:core:network`**: Retrofit/OkHttp setup for remote API communication.
- **`:core:datastore`**: Preferences and lightweight data storage.
- **`:core:common`**: Shared utilities, base classes, and extensions.
- **`:core:util`**: Generic utility functions.

---

## Guide: Adding a New Feature

To add a new feature (e.g., "Settings"), follow these steps:

1.  **Create Module**: Create a new Android Library module `:feature:settings`.
2.  **Configure Build**: Add necessary dependencies (Compose, Hilt, Core modules) in `build.gradle.kts`.
3.  **Implement Screen**: Create `SettingsScreen.kt` using Jetpack Compose.
4.  **Implement ViewModel**: Create `SettingsViewModel.kt` and annotate with `@HiltViewModel`.
5.  **Define Destination**: Add `Settings` to the `Destination` sealed interface in the `:navigation` module.
6.  **Update NavGraph**: Add the `Settings` route to `AivanceNavGraph.kt` in the `:navigation` module.
7.  **Register in App**: Ensure the `:app` module depends on the new `:feature:settings` module.

---

## Tech Stack & Design System

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Navigation**: Jetpack Navigation 3
- **Dependency Injection**: Hilt
- **Local Database**: Room
- **Networking**: Retrofit & OkHttp
- **Serialization**: Kotlinx Serialization
- **Image Loading**: Coil (planned)
- **Concurrency**: Coroutines & Flow

### Design System
- **Theme**: Material Design 3 (M3)
- **Color Strategy**: Dark-first, supports Dynamic Color (Material You).
- **Iconography**: Material Symbols (Rounded variant).
- **Layouts**: Edge-to-edge with proper window inset handling.
- **Typography**: Expressive typography using the "Montserrat" or "Inter" style (via M3 defaults).
