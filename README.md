# Aivance

Aivance is a production-grade Android application foundation built with a multi-module Clean Architecture and a premium dark-first design system. It serves as a robust starting point for high-performance, maintainable, and scalable Android applications.

## Overview

Aivance leverages modern Android development practices to provide a seamless user experience and developer ergonomics. The project is designed with a focus on:
- **Clean Architecture**: Separation of concerns into data, domain, and UI layers.
- **Modularity**: A feature-based multi-module structure for improved build times and code isolation.
- **Premium Design**: A dark-first, Material 3-compliant design system that is both expressive and adaptive.

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Architecture**: Clean Architecture, MVVM, Multi-module
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **Database**: [Room](https://developer.android.com/training/data-storage/room)
- **Persistence**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- **Navigation**: [Jetpack Navigation 3 (Adaptive)](https://developer.android.com/guide/navigation/navigation-3)
- **Asynchronous**: [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Logging**: [Timber](https://github.com/JakeWharton/timber)
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **Package Manager**: [Gradle](https://gradle.org/) with [Version Catalog](https://developer.android.com/build/migrate-to-catalogs)

## Project Structure

The project is divided into several modules to ensure a clean separation of concerns:

- **`:app`**: The entry point of the application (containing `MainActivity` and `AvianceApp`). It handles global dependency injection, application configuration, and orchestrates the navigation.
- **`:core`**: Shared infrastructure and utility modules.
  - `:core:common`: Shared models, constants, and basic utilities used across modules.
  - `:core:database`: Local data persistence using Room.
  - `:core:datastore`: Key-value storage for user preferences and settings.
  - `:core:designsystem`: The design system containing UI components, themes, and design tokens (Material 3).
  - `:core:network`: Remote data source handling using Retrofit.
  - `:core:util`: Low-level helpers and extension functions.
- **`:feature`**: Independent feature modules, each containing its own UI, ViewModel, and business logic.
  - `:feature:ats`: Applicant Tracking System integration features.
  - `:feature:coverletter`: Tools for generating and managing cover letters.
  - `:feature:dashboard`: The central hub and overview screen.
  - `:feature:interview`: Resources and prep tools for interviews.
  - `:feature:jobs`: Job search and listing functionality.
  - `:feature:profile`: User profile management and settings.
  - `:feature:resume`: Resume building and management tools.
  - `:feature:tracker`: Job application tracking and status monitoring.
- **`:navigation`**: Centralized navigation logic using the Jetpack Navigation 3 Adaptive APIs.

## Requirements

- **Android Studio**: Ladybug (2024.2.1) or later
- **JDK**: 17+
- **Android SDK**:
  - Minimum SDK: 26 (Android 8.0)
  - Target SDK: 37 (Android 16)

## Getting Started

### 1. Cloning the Repository
```bash
git clone https://github.com/your-repo/aivance.git
cd aivance
```

### 2. Syncing Gradle
Open the project in Android Studio. Gradle should automatically start syncing. If not, go to `File > Sync Project with Gradle Files`.

### 3. Running the App
Select the `app` configuration and choose a device or emulator running API 26 or higher, then click the **Run** button.

## Scripts

Use these Gradle commands for common development tasks:

- **Build Debug APK**:
  ```bash
  ./gradlew assembleDebug
  ```
- **Run Unit Tests**:
  ```bash
  ./gradlew test
  ```
- **Run Instrumented Tests**:
  ```bash
  ./gradlew connectedAndroidTest
  ```

## Environment Variables

Some features (like future AI integration) may require API keys. These should be configured in your `local.properties` file or via environment variables to avoid committing sensitive information.

- **TODO**: Add instructions for specific API keys (e.g., Gemini API, OpenAI) as they are integrated.

## License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.
