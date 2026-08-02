# AiVance v2 — Complete Repository Documentation

Welcome to the comprehensive technical documentation for **AiVance v2**, an AI-Powered Career Operating System built for Android. This document provides an end-to-end architectural, technical, and operational overview of the entire codebase.

---

## 📋 Table of Contents
1. [Executive Summary & Overview](#1-executive-summary--overview)
2. [Architectural Blueprint & Layering](#2-architectural-blueprint--layering)
3. [Multi-Module Architecture](#3-multi-module-architecture)
   - [App Module (`:app`)](#app-module-app)
   - [Navigation Module (`:navigation`)](#navigation-module-navigation)
   - [Core Modules (`:core:*`)](#core-modules-core)
   - [Feature Modules (`:feature:*`)](#feature-modules-feature)
4. [Data & Persistence Layer](#4-data--persistence-layer)
   - [Room Database & Schema](#room-database--schema)
   - [Encryption & Security Model](#encryption--security-model)
   - [Backup & Restore Pipeline](#backup--restore-pipeline)
5. [Multi-Provider AI & Enrichment SDK](#5-multi-provider-ai--enrichment-sdk)
6. [Network Security & Certificate Pinning](#6-network-security--certificate-pinning)
7. [Background Processing & WorkManager](#7-background-processing--workmanager)
8. [Testing & Verification Pass](#8-testing--verification-pass)
9. [Build Configuration & Optimization](#9-build-configuration--optimization)
10. [Developer Setup & Onboarding](#10-developer-setup--onboarding)

---

## 1. Executive Summary & Overview

**AiVance v2** is a production-grade, offline-first Android application designed to orchestrate the entire job search and career growth workflow. Powered by a flexible multi-provider AI engine, AiVance allows users to optimize resumes, pass Applicant Tracking Systems (ATS), generate tailored cover letters, practice interviews with real-time feedback, track applications across a Kanban pipeline, and extract recruiter intelligence.

### Technical Highlights
- **Language**: 100% Kotlin with Coroutines & Flow for reactive asynchronous streams.
- **UI Framework**: Modern Jetpack Compose with Material 3 Dark-first Design System.
- **Architecture**: Multi-Module Clean Architecture with Unidirectional Data Flow (UDF).
- **Dependency Injection**: Hilt for compile-time safe dependency injection across modules.
- **Navigation**: Jetpack Navigation 3 Adaptive APIs.
- **Local Storage**: Room Database with Tink AES-256-GCM encryption & DataStore for settings.
- **AI / SDK Integration**: Pluggable provider architecture supporting OpenAI, Groq, Gemini, Apify, Adzuna, USAJobs, Remotive, RemoteOK, and Hunter.io.
- **Security**: Strict Certificate Pinning, encrypted backups (AES-256-GCM + PBKDF2), and zero-plaintext API secret persistence.

---

## 2. Architectural Blueprint & Layering

The codebase strictly adheres to **Clean Architecture** combined with **Unidirectional Data Flow (UDF)** to decouple business logic from UI elements and external frameworks.

```
       ┌────────────────────────────────────────────────────────┐
       │                     UI Layer                           │
       │  Jetpack Compose Screens & ViewModels (UDF Flow)       │
       └──────────────────────────┬─────────────────────────────┘
                                  │
                                  ▼
       ┌────────────────────────────────────────────────────────┐
       │                   Domain Layer                         │
       │  Use Cases, Domain Models, Repository Interfaces       │
       └──────────────────────────┬─────────────────────────────┘
                                  │
                                  ▼
       ┌────────────────────────────────────────────────────────┐
       │                    Data Layer                          │
       │  Repository Implementations, Room DAOs, DataStore      │
       └──────────────────────────┬─────────────────────────────┘
                                  │
             ┌────────────────────┴────────────────────┐
             ▼                                         ▼
┌───────────────────────────┐             ┌───────────────────────────┐
│     Local Data Sources    │             │    Remote SDK & APIs      │
│  Room DB (Encrypted)      │             │  Multi-Provider AI/Jobs   │
└───────────────────────────┘             └───────────────────────────┘
```

### Key Principles:
1. **Separation of Concerns**: UI modules depend only on `:core:domain` and `:core:designsystem`, avoiding direct dependencies on concrete data sources.
2. **Unidirectional Data Flow (UDF)**: ViewModels expose immutable `StateFlow` to Compose UI and receive user interactions via strongly-typed `Event` objects.
3. **No Hardcoded Data**: All UI surfaces are backed by live Room Database streams or real SDK implementations.

---

## 3. Multi-Module Architecture

The project consists of **24 Gradle modules** structured into Application, Navigation, Core, and Feature layers:

### App Module (`:app`)
- **Package**: `com.bangersoul.aivance`
- **Role**: Application entry point, `Application` class configuration, global Hilt initialization, and WorkManager background job orchestration.
- **Key Components**:
  - `AivanceApp`: Custom application class initializing Timber logging, WorkManager, and notification channels.
  - `MainActivity`: Single-activity entry point hosting the root navigation shell.
  - `worker/*`: Background workers including `JobSyncWorker`, `AnalyticsUploadWorker`, `SecurityMigrationWorker`, `DatabaseCleanupWorker`, `CacheCleanupWorker`, `HealthCheckWorker`, `FollowUpWorker`, `NotificationWorker`, `ResumeAnalysisWorker`, `UploadManager`, and `DownloadManager`.

### Navigation Module (`:navigation`)
- **Package**: `com.bangersoul.aivance.navigation`
- **Role**: Type-safe destination routing using Jetpack Navigation 3.
- **Key Components**:
  - `Destination`: Sealed class defining all app destinations (`CareerHQ`, `ResumeEngine`, `AtsLiveScore`, `PrepStudio`, `JobTracker`, `ProfileSettings`, `Assistant`, etc.) with type-safe arguments.
  - `NavGraph`: Adaptive single-shell and multi-pane layout management.

### Core Modules (`:core:*`)

| Module | Description | Key Contents |
| :--- | :--- | :--- |
| `:core:common` | Base utilities, Result types, models | `Result<T>`, Common constants, Base Dispatchers |
| `:core:domain` | Core business logic & models | Repository interfaces, Use cases, Domain models |
| `:core:data` | Repository implementations | `JobRepositoryImpl`, `ResumeRepositoryImpl`, `AnalyticsRepositoryImpl` |
| `:core:database` | Local persistence | `AivanceDatabase`, Room DAOs, `EncryptionService` (Tink) |
| `:core:datastore` | Key-value settings & secrets | `SecretsManager`, `UserPreferencesDataStore` |
| `:core:network` | Retrofit & network security | OkHttp client, `CertificatePinningInterceptor` |
| `:core:designsystem` | UI tokens & Compose components | Material 3 Dark theme, custom buttons, cards, dialogs |
| `:core:util` | Export & file helpers | `PdfExporter`, `DocxExporter`, `BackupExporter`, `BackupImporter` |
| `:core:sdk` | Provider SDK engine | Modular SDK core interfaces, telemetry, provider registry |
| `:core:ai-providers` | AI engine adapters | OpenAI, Groq, Gemini provider implementations |
| `:core:job-providers` | Job search engine adapters | RemoteOK, Remotive, Adzuna, USAJobs |
| `:core:enrichment-providers` | Data enrichment adapters | Apify web scrapers, Hunter.io recruiter lookup |

### Feature Modules (`:feature:*`)

| Module | Feature / Description |
| :--- | :--- |
| `:feature:dashboard` | **Career HQ**: Central dashboard aggregating saved jobs, upcoming interviews, ATS status, and active roadmap. |
| `:feature:resume` | **Resume Engine**: 7-step pipeline (Import, OCR, Parsing, Scoring, Optimization, Formatting, Export to PDF/DOCX). |
| `:feature:ats` | **ATS Live Scoring**: Real-time keyword matching, readability scoring, and formatting error detection against job descriptions. |
| `:feature:coverletter` | **Cover Letter Generator**: AI-driven cover letter generation with tone customization and export features. |
| `:feature:tracker` | **Job Application Tracker**: Drag-and-drop Kanban board for managing application stages (Applied, Interviewing, Offer, Rejected). |
| `:feature:interview` | **Prep Studio & Interview Coach**: Q&A bank, mock interview simulator, real-time voice response scoring, and feedback. |
| `:feature:jobs` | **Universal Job Search**: Multi-provider live job search, filtering, bookmarking, and application deep-linking. |
| `:feature:profile` | **Settings & Privacy Hub**: User profile management, API provider configuration, secure data export/restore, and account privacy. |
| `:feature:recruiter` | **Recruiter Intelligence**: Recruiter contact lookup via Hunter.io and company background search. |
| `:feature:analytics` | **Analytics & Insights**: Job search velocity metrics, interview score progression, and conversion funnel charts. |
| `:feature:assistant` | **AI Assistant**: Multimodal AI career assistant with voice input, text attachment, and contextual guidance. |

---

## 4. Data & Persistence Layer

### Room Database & Schema
The database (`AivanceDatabase`) manages all persistent offline state:

- **Entities**:
  - `ResumeEntity` & `ResumeVersionEntity`: Multi-version resume documents and structured section blocks.
  - `JobEntity`: Cached job listings, metadata, match score, and bookmark status.
  - `JobApplicationEntity`: Kanban application tracker records with status history.
  - `CoverLetterEntity`: Generated cover letters associated with jobs/resumes.
  - `UserProfileEntity`: User preferences, target roles, target location, and skills profile.
  - `RecruiterEntity` & `ContactEntity`: Discovered recruiter profiles and contact info.
  - `AnalyticsEventEntity`: Offline-first telemetry and user action logs.

### Encryption & Security Model
- **Field-Level Encryption**: Sensitive provider keys are stored in `SecretsManager` backed by EncryptedSharedPreferences and Tink AES-256-GCM.
- **Migration Worker**: `SecurityMigrationWorker` automatically scans database settings for legacy plaintext secrets, encrypts them, and strips plaintext values from the database.

### Backup & Restore Pipeline
- **Backup Exporter (`BackupExporter.kt`)**: Serializes Room database tables to JSON, encrypts using PBKDF2 (10,000 iterations) + AES-256-GCM, and exports a password-protected `.aivance_backup` file via FileProvider.
- **Backup Importer (`BackupImporter.kt`)**: Decrypts user-selected `.aivance_backup` files via Storage Access Framework (SAF) and restores entities into Room.

---

## 5. Multi-Provider AI & Enrichment SDK

AiVance utilizes a pluggable provider abstraction pattern (`ProviderManager`), allowing dynamic hot-swapping and fallback configuration for AI, Job, and Data Enrichment APIs.

```
                  ┌───────────────────────────────┐
                  │        ProviderManager        │
                  └───────────────┬───────────────┘
                                  │
      ┌───────────────────────────┼───────────────────────────┐
      ▼                           ▼                           ▼
┌──────────────┐           ┌──────────────┐           ┌──────────────┐
│ AI Providers │           │ Job Search   │           │ Enrichment   │
│ - OpenAI     │           │ - RemoteOK   │           │ - Apify      │
│ - Groq       │           │ - Remotive   │           │ - Hunter.io  │
│ - Gemini     │           │ - Adzuna     │           └──────────────┘
└──────────────┘           │ - USAJobs    │
                           └──────────────┘
```

- **Runtime Credentials Configuration**: Users can provide API keys at runtime via **Profile > Provider Management**. The app re-hydrates credentials into `SecretsManager` and invokes `ProviderManager.reconfigure()` dynamically without requiring app restarts.

---

## 6. Network Security & Certificate Pinning

Production network requests are secured via `CertificatePinningInterceptor.kt` in `:core:network`.

- **Enforced Enpoints**:
  - `api.groq.com`
  - `api.openai.com`
  - `openrouter.ai`
  - `remoteok.com`
  - `remotive.com`
  - `api.apify.com`
- **Pin Configuration**: Features dual-pinning per domain using primary leaf SHA-256 public key hashes and secondary CA backup pins (Cloudflare, GTS, ISRG Root X1, Amazon Root CA 1).

---

## 7. Background Processing & WorkManager

Background sync and system maintenance are managed via Hilt-injected WorkManager jobs:

- `JobSyncWorker`: Periodic background sync of new job postings based on user search preferences.
- `AnalyticsUploadWorker` & `AnalyticsSnapshotWorker`: Batched offline analytics aggregation and upload.
- `SecurityMigrationWorker`: One-time idempotent security migration for secret encryption.
- `DatabaseCleanupWorker`: Purges stale temporary cache tables and expired job search entries.
- `CacheCleanupWorker`: Cleans up temporary exported PDF/DOCX files from internal storage.
- `HealthCheckWorker`: Periodically checks connectivity and provider health status.
- `FollowUpWorker` & `NotificationWorker`: Schedules interview reminders and application follow-up notifications.

---

## 8. Testing & Verification Pass

The repository enforces a strict zero-warning, zero-error, zero-test-failure verification pipeline.

### Verification Suite Commands
```bash
# 1. Build Debug Variant
./gradlew assembleDebug

# 2. Execute JVM Unit Tests across all 24 modules
./gradlew testDebugUnitTest

# 3. Static Code & Lint Analysis
./gradlew lintDebug

# 4. Release Build & R8 Minification Optimization Pass
./gradlew assembleRelease
```

---

## 9. Build Configuration & Optimization

### Heap & Daemon Optimizations
- **Gradle JVM Heap**: Set to `4096m` (`org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8`) in `gradle.properties` to ensure R8 shrinker and cross-module dexing complete without OutOfMemory errors.
- **R8 / ProGuard Configuration**: Strict shrink and obfuscation rules defined in `app/proguard-rules.pro` for Room, Hilt, Apache POI, OkHttp, and KotlinX Serialization.

---

## 10. Developer Setup & Onboarding

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer.
- **JDK**: Version 17+.
- **Android SDK**: `minSdk = 26`, `compileSdk = 37`, `targetSdk = 37`.

### Getting Started
1. Clone the repository:
   ```bash
   git clone https://github.com/IamAzmathullaShaikh/Aivance.git
   cd Aivance
   ```
2. (Optional) Configure test API keys in `local.properties`:
   ```properties
   groqApiKey=your_groq_key
   apifyApiKey=your_apify_key
   geminiApiKey=your_gemini_key
   hunterApiKey=your_hunter_key
   ```
3. Open in Android Studio, sync Gradle, select the `app` target, and run on an emulator or physical device.

---

*Documentation updated for AiVance v2 Release.*
