# Aivance

Aivance is a production-grade Android application foundation built with a multi-module Clean Architecture and a premium dark-first design system. It serves as a robust starting point for high-performance, maintainable, and scalable Android applications.

## Overview

Aivance leverages modern Android development practices to provide a seamless user experience and developer ergonomics. The project is designed with a focus on:
- **Clean Architecture**: Separation of concerns into data, domain, and UI layers.
- **Modularity**: A feature-based multi-module structure for improved build times and code isolation.
- **Premium Design**: A dark-first, Material 3-compliant design system that is both expressive and adaptive.

## Key Features

- **Resume Optimizer & ATS Matcher**: Analyze and optimize your resume to pass Applicant Tracking Systems (ATS) with ease. Get real-time feedback and keyword optimization suggestions.
- **AI Career Roadmap**: Generate personalized career paths based on your current skills and target roles.
- **Dynamic Cover Letter Generator**: Create tailored cover letters for every job application using advanced AI models.
- **AI Interview Coach**: Practice interviews with a real-time AI coach that provides feedback on your answers and delivery.
- **Universal Job Tracker**: Manage your entire job search funnel in one place, from discovery to offer.

## Architecture

Aivance is built upon the principles of **Clean Architecture**, ensuring a clear separation of concerns, testability, and maintainability.

Detailed design decisions and architectural evolutions are documented in our [Architecture Decision Records (ADRs)](docs/adr/).

### Core Pillars
- **Multi-module Architecture**: The project is organized into feature and core modules to promote isolation, reusability, and faster build times.
- **Dependency Injection (DI)**: Using [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for robust and scalable dependency management.
- **Unidirectional Data Flow (UDF)**: UI state management follows UDF principles using [StateFlow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/) and [Compose](https://developer.android.com/jetpack/compose) to ensure predictable UI updates.

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

- **`:app`**: The entry point of the application (containing `MainActivity` and `AivanceApp`). It handles global dependency injection, application configuration, and orchestrates the navigation.
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
  - `:feature:resume`: **Resume Optimizer & ATS Matcher** tools.
  - `:feature:tracker`: Job application tracking and status monitoring.
- **`:navigation`**: Centralized navigation logic using the Jetpack Navigation 3 Adaptive APIs.

## 🚀 Usage Guide

Maximize your career growth with Aivance by following these five steps:

1.  **Step 1: Define Your North Star (Profile/Roadmap)**
    Start by completing your profile and generating an AI Career Roadmap. This helps Aivance understand your goals and provide personalized recommendations.
2.  **Step 2: Optimize Your Assets (Resume & ATS)**
    Upload your resume to the **Resume Optimizer & ATS Matcher**. Use the AI-driven feedback to ensure your resume is perfectly tailored for modern ATS filters.
3.  **Step 3: Tailor Your Outreach (Cover Letter)**
    For every job you're interested in, use the Cover Letter Generator to create a compelling, personalized application that stands out to recruiters.
4.  **Step 4: Prepare for the Battle (Interview Coach)**
    Once you land an interview, head over to the **Interview Coach**. Practice common questions and get real-time feedback on your performance and technical accuracy.
5.  **Step 5: Manage the Funnel (Tracker)**
    Keep all your applications organized in the **Tracker**. Monitor your progress, set follow-up reminders, and analyze your conversion rates to optimize your job search strategy.

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

## API Setup (Gemini AI)

Some features (like AI integration) require API keys. These must be configured in your `local.properties` file or through the App Settings menu to avoid committing sensitive information.

### Configuration via `local.properties`
To use the AI features (e.g., in the `:core:network` module), you need a Gemini API key:
1. Go to the [Google AI Studio](https://aistudio.google.com/app/apikey).
2. Create or copy your API key.
3. Open your `local.properties` file in the project root.
4. Add the following line:
   ```properties
   GEMINI_API_KEY=your_api_key_here
   ```

### Configuration via App Settings
Alternatively, you can provide the API key directly within the app:
1. Open the **Aivance** app on your device.
2. Navigate to **Profile > Settings**.
3. Enter your Gemini API key in the designated field.
4. The key will be securely stored in your local DataStore.

> [!IMPORTANT]
> Never commit `local.properties` to version control. It is already included in the `.gitignore` by default.

## License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.
