# AVIANCE - MASTER ENGINEERING SPECIFICATION & ARCHITECTURAL AUDIT

**Document Type:** Internal Architecture Decision Record (ADR), Software Design Document (SDD), Repository Specification, UI Specification, and Engineering Audit  
**Target Repository:** Aviance (Android)  
**Package Root:** `com.bangersoul.aivance`  
**Author:** Principal Software Architecture & Engineering Audit Team  
**Status:** Master Source of Truth / Active Architectural Reference  

---

## SECTION 1: PROJECT OVERVIEW

### Project Purpose
Aviance is an AI-powered career co-pilot and job search optimization platform built natively for Android. The application assists job seekers by providing automated resume analysis, Applicant Tracking System (ATS) compatibility scoring, AI-generated cover letters, interactive mock interview simulations with feedback, job application tracking, and career roadmap generation.

### Technology Stack
*   **Primary Language:** Kotlin 2.0.21
*   **UI Framework:** Jetpack Compose (Compiler extension managed via Kotlin Compose Plugin `2.0.21`)
*   **Design System:** Material Design 3 (Compose Material3 `1.3.1`, Material Icons Extended `1.7.5`)
*   **Adaptive Navigation:** Navigation Suite (`1.3.1`), Navigation 3 (`1.0.0-alpha01`)
*   **Dependency Injection:** Hilt / Dagger (`2.51.1`), Hilt Navigation Compose (`1.2.0`), Hilt Work (`1.2.0`)
*   **Database:** Room (`2.6.1`) with KSP code generation (`2.0.21-1.0.27`)
*   **Preferences / Key-Value:** DataStore Preferences Proto (`1.1.1`)
*   **Networking:** Retrofit (`2.11.0`), OkHttp (`4.12.0`) with `logging-interceptor`
*   **Serialization:** Kotlinx Serialization JSON (`1.7.3`)
*   **AI Integration:** Google Generative AI Client SDK (`0.9.0`) - Gemini 1.5 Flash
*   **Document Processing:** PDF Box Android (`2.0.27.0`), Android Native `PdfRenderer`
*   **Background Processing:** AndroidX WorkManager (`2.9.1`)
*   **Concurrency:** Kotlinx Coroutines (`1.9.0`)
*   **Asynchronous Processing:** Reactive Streams / Kotlin Flow
*   **Build System:** Gradle 8.11 with Kotlin DSL (`build.gradle.kts`) and Version Catalog (`gradle/libs.versions.toml`)

### SDK Specifications
*   **Compile SDK:** 35 (Android 15)
*   **Min SDK:** 26 (Android 8.0 Oreo)
*   **Target SDK:** 35 (Android 15)
*   **Java Toolchain Version:** JDK 17
*   **JVM Target:** 17

### Build System & Version Catalog Summary
*   **Gradle Version:** 8.11
*   **Plugin Management:** Plugin Management defined in `settings.gradle.kts` using `pluginManagement.repositories` (Google, MavenCentral, Gradle Plugin Portal).
*   **Catalog Location:** `gradle/libs.versions.toml`
*   **Custom Gradle Conventions:** Custom build logic exists in `build.gradle.kts` at root and subproject configuration blocks.

### Repository Statistics
*   **Gradle Modules:** 15 modules (`:app`, `:navigation`, `:core:common`, `:core:database`, `:core:datastore`, `:core:designsystem`, `:core:network`, `:core:util`, `:feature:ats`, `:feature:coverletter`, `:feature:dashboard`, `:feature:interview`, `:feature:jobs`, `:feature:profile`, `:feature:resume`, `:feature:tracker`).
*   **Total Source Files (.kt):** ~65 Kotlin source files.
*   **Feature Count:** 8 distinct user-facing features (Dashboard, Resume, ATS Analysis, Cover Letter, Job Search, Interview Prep, Application Tracker, Career Profile/Roadmap).
*   **Database Entities:** 5 Room entities (`AivanceEntity`, `ApplicationEntity`, `AtsResultEntity`, `CoverLetterEntity`, `RoadmapEntity`, `RoadmapStepEntity`).
*   **Database DAOs:** 4 DAOs (`AivanceDao`, `ApplicationDao`, `AtsDao`, `CoverLetterDao`, `RoadmapDao`).

### Architectural Strengths
1.  **Strict Modularization:** Clean separation between `:core` infrastructure, `:feature` domains, `:navigation`, and `:app` application assembly.
2.  **Modern Android Tech Stack:** Built entirely with Kotlin Coroutines, StateFlow, Jetpack Compose, Room, Hilt, and Navigation 3.
3.  **Unidirectional Data Flow (UDF):** ViewModels expose explicit, immutable `UiState` StateFlow objects to Composables, emitting events via lambdas.
4.  **Local First Architecture:** Persistence using Room database allows offline reading of past ATS scores, cover letters, and tracked applications.

### Architectural Weaknesses & Key Deficiencies
1.  **PdfRenderer API Level Incompatibility (Critical Bug):** `PdfTextExtractor.kt` in `:core:util` attempts to use `PdfRenderer.textContents`, which was only introduced in Android 15 (API level 35). Running this on minSdk 26 through 34 causes a `NoSuchMethodError` or returns an unparsed error string, breaking PDF upload for ~95% of Android devices.
2.  **Hardcoded Job Search Mocking:** `:feature:jobs` contains no network API or web scraping logic. `JobSearchRepositoryImpl` returns static hardcoded Kotlin lists, rendering the Job Search feature non-functional in real-world scenarios.
3.  **Stubbed Interview AI Feedback Parsing:** In `InterviewRepositoryImpl.kt`, `getFeedback()` calls AI but discards the JSON response and returns hardcoded fake string lists ("Good overall performance.", "Clear communication").
4.  **API Key Leakage & Hardcoded Credentials:** `GeminiAiService.kt` reads `BuildConfig.GEMINI_API_KEY` or falls back to `UserPreferences`, while `local.properties` or build configs expose default development keys in VCS.
5.  **Room Schema Export Disabled & Missing Migrations:** `AivanceDatabase` has `exportSchema = false` and database version 4. Destructive migration is not handled gracefully, and schema changes will crash existing installs.
6.  **Navigation 3 Experimental API Usage:** The project uses `androidx.navigation3:navigation3-ui:1.0.0-alpha01`, which is highly unstable and subject to breaking API shifts.
7.  **DataStore Serializer Null Safety Risks:** `UserPreferencesSerializer.kt` uses basic Kotlinx JSON string conversion without robust exception recovery, potentially corrupting user preferences on disk if schema changes occur.

---

## SECTION 2: REPOSITORY STRUCTURE

### Complete Directory Tree
```
Aivance/
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── local.properties
├── gradlew
├── gradlew.bat
├── Audit.md
├── README.md
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.jar
│   │   └── gradle-wrapper.properties
│   └── libs.versions.toml
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── androidTest/java/com/bangersoul/aivance/ExampleInstrumentedTest.kt
│       ├── test/java/com/bangersoul.aivance/ExampleUnitTest.kt
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/bangersoul/aivance/
│           │   ├── AivanceApp.kt
│           │   ├── MainActivity.kt
│           │   └── worker/
│           │       └── FollowUpWorker.kt
│           └── res/
│               ├── drawable/
│               ├── mipmap-*/
│               └── values/
├── navigation/
│   ├── build.gradle.kts
│   └── src/
│       ├── androidTest/java/com/bangersoul/aivance/navigation/
│       │   ├── AivanceNavGraphTest.kt
│       │   └── HiltTestRunner.kt
│       └── main/java/com/bangersoul/aivance/navigation/
│           ├── AivanceNavGraph.kt
│           └── Route.kt
├── core/
│   ├── common/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/bangersoul/aivance/core/common/
│   │       ├── result/
│   │       │   └── Result.kt
│   │       └── dispatchers/
│   │           ├── AivanceDispatchers.kt
│   │           └── DispatchersModule.kt
│   ├── database/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── androidTest/java/com/bangersoul/aivance/core/database/dao/
│   │       │   ├── ApplicationDaoTest.kt
│   │       │   └── AtsDaoTest.kt
│   │       └── main/java/com/bangersoul/aivance/core/database/
│   │           ├── AivanceDatabase.kt
│   │           ├── DatabaseModule.kt
│   │           ├── dao/
│   │           │   ├── ApplicationDao.kt
│   │           │   ├── AtsDao.kt
│   │           │   ├── CoverLetterDao.kt
│   │           │   └── RoadmapDao.kt
│   │           └── model/
│   │               ├── ApplicationEntity.kt
│   │               ├── AtsResultEntity.kt
│   │               ├── CoverLetterEntity.kt
│   │               ├── RoadmapEntity.kt
│   │               └── RoadmapStepEntity.kt
│   ├── datastore/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/bangersoul/aivance/core/datastore/
│   │       ├── DataStoreModule.kt
│   │       ├── UserPreferences.kt
│   │       └── UserPreferencesSerializer.kt
│   ├── designsystem/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/bangersoul/aivance/core/designsystem/
│   │       ├── components/
│   │       │   ├── AivanceButtons.kt
│   │       │   ├── AivanceCards.kt
│   │       │   ├── AivanceError.kt
│   │       │   ├── AivanceLoading.kt
│   │       │   ├── AivanceSuccess.kt
│   │       │   ├── AivanceTextFields.kt
│   │       │   └── AnalysisResultCard.kt
│   │       └── theme/
│   │           ├── Color.kt
│   │           ├── Theme.kt
│   │           └── Type.kt
│   ├── network/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/bangersoul/aivance/core/network/
│   │       ├── AiService.kt
│   │       ├── DelegatingAiService.kt
│   │       ├── GeminiAiService.kt
│   │       ├── MockAiService.kt
│   │       ├── NetworkModule.kt
│   │       └── ai/
│   │           ├── AiMessage.kt
│   │           └── AiRole.kt
│   └── util/
│       ├── build.gradle.kts
│       └── src/main/java/com/bangersoul/aivance/core/util/
│           ├── FileUtils.kt
│           └── PdfTextExtractor.kt
└── feature/
    ├── ats/
    │   ├── build.gradle.kts
    │   └── src/main/java/com/bangersoul/aivance/feature/ats/
    │       ├── AtsScreen.kt
    │       ├── AtsViewModel.kt
    │       ├── data/
    │       │   └── AtsRepositoryImpl.kt
    │       ├── di/
    │       │   └── AtsModule.kt
    │       └── domain/
    │           ├── AtsRepository.kt
    │           └── AtsResult.kt
    ├── coverletter/
    │   ├── build.gradle.kts
    │   └── src/main/java/com/bangersoul/aivance/feature/coverletter/
    │       ├── CoverLetterScreen.kt
    │       ├── CoverLetterViewModel.kt
    │       ├── data/repository/
    │       │   └── CoverLetterRepositoryImpl.kt
    │       ├── di/
    │       │   └── CoverLetterModule.kt
    │       └── domain/
    │           ├── model/
    │           │   ├── CoverLetter.kt
    │           │   └── LetterTone.kt
    │           └── repository/
    │               └── CoverLetterRepository.kt
    ├── dashboard/
    │   ├── build.gradle.kts
    │   └── src/
    │       ├── androidTest/java/com/bangersoul/aivance/feature/dashboard/
    │       │   └── DashboardScreenTest.kt
    │       └── main/java/com/bangersoul/aivance/feature/dashboard/
    │           ├── DashboardScreen.kt
    │           ├── DashboardUiState.kt
    │           ├── DashboardViewModel.kt
    │           ├── data/
    │           │   ├── DashboardRepositoryImpl.kt
    │           │   └── FakeDashboardRepository.kt
    │           ├── di/
    │           │   └── DashboardModule.kt
    │           └── domain/
    │               ├── DashboardData.               ├── DashboardRepository.kt
    │               ├── RecentActivity.kt
    │               └── ResumeStatus.kt
    ├── interview/
    │   ├── build.gradle.kts
    │   └── src/main/java/com/bangersoul/aivance/feature/interview/
    │       ├── InterviewScreen.kt
    │       ├── InterviewViewModel.kt
    │       ├── data/
    │       │   └── InterviewRepositoryImpl.kt
    │       ├── di/
    │       │   └── InterviewModule.kt
    │       └── domain/
    │           ├── InterviewFeedback.kt
    │           ├── InterviewMessage.kt
    │           ├── InterviewRepository.kt
    │           └── InterviewSession.kt
    ├── jobs/
    │   ├── build.gradle.kts
    │   └── src/main/java/com/bangersoul/aivance/feature/jobs/
    │       ├── JobsScreen.kt
    │       ├── JobsViewModel.kt
    │       ├── data/
    │       │   └── JobSearchRepositoryImpl.kt
    │       ├── di/
    │       │   └── JobsModule.kt
    │       └── domain/
    │           ├── JobListing.kt
    │           └── JobSearchRepository.kt
    ├── profile/
    │   ├── build.gradle.kts
    │   └── src/main/java/com/bangersoul/aivance/feature/profile/
    │       ├── ProfileScreen.kt
    │       ├── ProfileViewModel.kt
    │       ├── data/
    │       │   └── RoadmapRepositoryImpl.kt
    │       ├── di/
    │       │   └── ProfileModule.kt
    │       └── domain/
    │           ├── CareerRoadmap.kt
    │           ├── RoadmapRepository.kt
    │           └── RoadmapStep.kt
    ├── resume/
    │   ├── build.gradle.kts
    │   └── src/
    │       ├── test/java/com/bangersoul/aivance/feature/resume/
    │       │   ├── ResumeViewModelTest.kt
    │       │   └── data/repository/
    │       │       └── ResumeRepositoryTest.kt
    │       └── main/java/com/bangersoul/aivance/feature/resume/
    │           ├── ResumeScreen.kt
    │           ├── ResumeUiState.kt
    │           ├── ResumeViewModel.kt
    │           ├── data/
    │           │   ├── model/
    │           │   │   └── ResumeAnalysisDto.kt
    │           │   └── repository/
    │           │       └── ResumeRepositoryImpl.kt
    │           ├── di/
    │           │   └── ResumeModule.kt
    │           └── domain/
    │               ├── model/
    │               │   └── ResumeAnalysis.kt
    │               └── repository/
    │                   └── ResumeRepository.kt
    └── tracker/
        ├── build.gradle.kts
        └── src/main/java/com/bangersoul/aivance/feature/tracker/
            ├── TrackerScreen.kt
            ├── TrackerViewModel.kt
            ├── data/
            │   └── JobTrackerRepositoryImpl.kt
            ├── di/
            │   └── TrackerModule.kt
            └── domain/
                ├── ApplicationStatus.kt
                ├── JobApplication.kt
                └── JobTrackerRepository.kt
```

### Module Dependency Graph

```
                           +-------------------+
                           |       :app        |
                           +---------+---------+
                                     |
                                     v
                           +-------------------+
                           |    :navigation    |
                           +---------+---------+
                                     |
    +-----------+------------+-------+----+------------+-----------+
    |           |            |            |            |           |
    v           v            v            v            v           v
:feature:   :feature:   :feature:    :feature:    :feature:   :feature:
dashboard    resume        ats      coverletter    tracker    interview
    |           |            |            |            |           |
    +-----------+------------+-------+----+------------+-----------+
                             |       |
                             v       v
                       :feature:  :feature:
                         jobs     profile
                             |       |
    +------------------------+-------+-------------------------+
    |                        |       |                         |
    v                        v       v                         v
:core:designsystem     :core:network :core:database     :core:datastore
    |                        |       |                         |
    +------------------------+-------+-------------------------+
                             |
                             v
                       :core:common / :core:util
```

---

## SECTION 3: MODULE AUDIT

Below is the exhaustive individual audit for each of the 16 Gradle modules in the codebase.

### 1. `:app`
*   **Purpose:** Application entry point, Hilt initialization, WorkManager setup, and global manifest declaration.
*   **Responsibilities:** Application class lifecycle (`AivanceApp`), Activity initialization (`MainActivity`), background periodic job dispatch (`FollowUpWorker`).
*   **Public API:** `AivanceApp`, `MainActivity`.
*   **Dependencies:** `:navigation`, `:feature:*`, `:core:database`, `:core:datastore`, `:core:network`, `libs.hilt.android`, `libs.androidx.work`.
*   **Dependents:** None (Root node).
*   **Architecture:** Android Application Framework.
*   **Completion %:** 85%
*   **Implementation Quality:** Good. Uses `@HiltAndroidApp` and `Configuration.Provider` for custom WorkManager Hilt injection.
*   **Testing Status:** Minimal (`ExampleUnitTest`, `ExampleInstrumentedTest`).
*   **Problems:** `FollowUpWorker` lacks clear user-facing notification logic (logs statement only).
*   **Missing Implementation:** Deep link intent filter handling in `AndroidManifest.xml`.
*   **Technical Debt:** Target SDK 35 requires explicit notification permissions setup for Android 13+.

### 2. `:navigation`
*   **Purpose:** Centralized screen routing and navigation graph implementation.
*   **Responsibilities:** Defines `Destination` sealed hierarchy, hosts `AivanceNavGraph`, configures `NavigationSuiteScaffold` bottom bar, and wires ViewModels to screens.
*   **Public API:** `AivanceNavGraph()`, `Destination`.
*   **Dependencies:** All `:feature:*` modules, `libs.androidx.navigation3.ui`, `libs.androidx.compose.material3.adaptive.navigation.suite`.
*   **Dependents:** `:app`.
*   **Architecture:** Navigation 3 (Alpha) entry provider architecture.
*   **Completion %:** 90%
*   **Implementation Quality:** Modern but relies on highly experimental Navigation 3 alpha libraries.
*   **Testing Status:** UI Test present (`AivanceNavGraphTest`).
*   **Problems:** `backStack.removeAt(backStack.lastIndex)` manually manipulated for root destinations rather than robust top-level destination state clearing.
*   **Missing Implementation:** Deep link routing parameter support; type-safe argument passing for detailed view routes.

### 3. `:core:common`
*   **Purpose:** Shared models, coroutine dispatchers, and result handling utilities.
*   **Responsibilities:** Coroutine dispatcher qualifiers (`@Dispatcher(AivanceDispatchers.IO)`), `Result<T>` flow extensions.
*   **Public API:** `AivanceDispatchers`, `DispatchersModule`, `Result`.
*   **Dependencies:** `libs.kotlinx.coroutines.android`, `libs.javax.inject`.
*   **Dependents:** `:core:network`, `:core:database`, all `:feature:*` modules.
*   **Architecture:** Core Infrastructure.
*   **Completion %:** 100%
*   **Implementation Quality:** Excellent. Clean Hilt module providing `Dispatchers.IO` and `Dispatchers.Default`.

### 4. `:core:database`
*   **Purpose:** Local persistence layer using Room database.
*   **Responsibilities:** Manages SQLite database (`AivanceDatabase`), DAOs (`ApplicationDao`, `AtsDao`, `CoverLetterDao`, `RoadmapDao`), and entities.
*   **Public API:** `AivanceDatabase`, `DatabaseModule`, DAOs, and Entity models.
*   **Dependencies:** `:core:common`, `libs.androidx.room.runtime`, `libs.androidx.room.ktx`.
*   **Dependents:** `:core:network`, all `:feature:*` modules.
*   **Architecture:** Local Database Data Source.
*   **Completion %:** 80%
*   **Implementation Quality:** Fair.
*   **Problems:** DB Version is 4, but `exportSchema = false` and no migration scripts exist. `fallbackToDestructiveMigration()` is NOT enabled in `DatabaseModule.kt`, causing immediate `IllegalStateException` on schema change.
*   **Testing Status:** `ApplicationDaoTest` and `AtsDaoTest` exist in `androidTest`.

### 5. `:core:datastore`
*   **Purpose:** Persistent key-value storage for user settings and API key storage.
*   **Responsibilities:** Proto DataStore / Preferences DataStore wrapper for `UserPreferences`.
*   **Public API:** `UserPreferences`, `DataStoreModule`, `UserPreferencesSerializer`.
*   **Dependencies:** `libs.androidx.datastore.preferences`, `libs.kotlinx.serialization.json`.
*   **Dependents:** `:core:network`, `:feature:profile`.
*   **Completion %:** 85%
*   **Problems:** Unencrypted plain-text storage of Gemini API Keys in preferences JSON file.

### 6. `:core:network`
*   **Purpose:** Remote network calls and AI service abstraction.
*   **Responsibilities:** Defines `AiService` interface, `GeminiAiService`, `MockAiService`, and `DelegatingAiService`.
*   **Public API:** `AiService`, `DelegatingAiService`, `AiMessage`, `AiRole`.
*   **Dependencies:** `:core:common`, `:core:datastore`, `libs.google.generativeai`, `libs.retrofit`, `libs.okhttp.logging`.
*   **Dependents:** `:feature:resume`, `:feature:coverletter`, `:feature:interview`.
*   **Completion %:** 75%
*   **Problems:** Direct coupling to Google Generative AI SDK without generic AI Provider abstraction.

### 7. `:core:util`
*   **Purpose:** Utility functions for file management and document parsing.
*   **Responsibilities:** File copying, URI reading, PDF text extraction.
*   **Public API:** `FileUtils`, `PdfTextExtractor`.
*   **Dependencies:** `libs.pdfbox.android`.
*   **Dependents:** `:feature:resume`, `:feature:ats`.
*   **Completion %:** 50%
*   **Problems (CRITICAL BUG):** `PdfTextExtractor.kt` calls `PdfRenderer.textContents` which is API 35+ only! On minSdk 26-34 devices, PDF parsing crashes or fails silently.

### 8. `:core:designsystem`
*   **Purpose:** Centralized UI theme and reusable Compose components.
*   **Responsibilities:** Color scheme, typography, shapes, buttons, cards, text fields, loading states, error states, and success indicators.
*   **Public API:** `AivanceTheme`, `AivanceButton`, `AivanceCard`, `AivanceTextField`, `AivanceLoading`, `AivanceError`, `AivanceSuccess`.
*   **Dependencies:** `libs.androidx.compose.material3`.
*   **Dependents:** All `:feature:*` modules, `:navigation`.
*   **Completion %:** 90%

### 9–16. Feature Modules Audit Summary (`:feature:*`)
*   **`:feature:dashboard`:** Displays high-level overview. Completion: 90%. Missing pull-to-refresh.
*   **`:feature:resume`:** Uploads PDF/Text resume, runs AI analysis. Completion: 80%. Blocked by PDF extractor bug.
*   **`:feature:ats`:** Displays historical ATS scan results. Completion: 85%. Missing filter/search options.
*   **`:feature:coverletter`:** Generates cover letter based on selected tone. Completion: 85%. Lacks PDF export option.
*   **`:feature:tracker`:** Manages job applications Kanban/List. Completion: 85%. Lacks reminder notifications.
*   **`:feature:interview`:** AI mock interview chat. Completion: 60%. Structured feedback parser is hardcoded/stubbed.
*   **`:feature:jobs`:** Job search screen. Completion: 20%. Uses 100% hardcoded mock data.
*   **`:feature:profile`:** User profile & career roadmap. Completion: 75%. API key settings entry point missing validation feedback.

---

## SECTION 4: SCREEN SPECIFICATION

### 1. Dashboard Screen
*   **File Path:** `feature/dashboard/src/main/java/com/bangersoul/aivance/feature/dashboard/DashboardScreen.kt`
*   **ViewModel:** `DashboardViewModel` (`uiState: StateFlow<DashboardUiState>`)
*   **Composable Hierarchy:**
    ```
    Scaffold
     └── LazyColumn
           ├── ProfileCompletionHeaderCard
           ├── AtsScoreOverviewCard
           ├── ActiveApplicationsCard
           ├── QuickActionsRow (Resume, Interview, Tracker)
           └── RecentActivityList
    ```
*   **Navigation Entry:** `Destination.Dashboard` (Root destination).
*   **Navigation Exit:** `onNavigateToResume`, `onNavigateToTracker`, `onNavigateToProfile`, `onNavigateToInterview`.
*   **State Matrix:**
    *   *Loading:* Renders `AivanceLoading` centered progress bar.
    *   *Success:* Displays `DashboardData` metrics.
    *   *Empty:* Shows default fallback counts when DB has zero records.
    *   *Error:* Displays `AivanceError` with retry prompt.

### 2. Resume Screen
*   **File Path:** `feature/resume/src/main/java/com/bangersoul/aivance/feature/resume/ResumeScreen.kt`
*   **ViewModel:** `ResumeViewModel` (`uiState: StateFlow<ResumeUiState>`)
*   **Composable Hierarchy:**
    ```
    Scaffold
     └── Column
           ├── TopAppBar
           ├── PDF / Text Input Toggle
           ├── FilePickerButton / OutlinedTextField (Resume Content)
           ├── OutlinedTextField (Job Description)
           ├── PrimaryButton ("Analyze Resume")
           └── AnalysisResultCard (Match score progress indicator, Keyword chips, Tip cards)
    ```
*   **Navigation Entry:** `Destination.Resume`.
*   **Navigation Exit:** `onNavigateToAts`, `onNavigateToCoverLetter`.

### 3. ATS History Screen
*   **File Path:** `feature/ats/src/main/java/com/bangersoul/aivance/feature/ats/AtsScreen.kt`
*   **ViewModel:** `AtsViewModel`
*   **Composable Hierarchy:**
    ```
    Scaffold
     └── LazyColumn
           ├── LatestScoreHeroBanner
           └── AtsResultItemCards (Score badge, Resume Name, Date, Missing Keywords, Feedback Expandable)
    ```

### 4. Cover Letter Screen
*   **File Path:** `feature/coverletter/src/main/java/com/bangersoul/aivance/feature/coverletter/CoverLetterScreen.kt`
*   **ViewModel:** `CoverLetterViewModel`
*   **Composable Hierarchy:**
    ```
    Scaffold
     └── Column
           ├── ToneDropdown / ToneChipRow (Professional, Enthusiastic, Confident)
           ├── Resume & Job Description Inputs
           ├── ActionButton ("Generate Cover Letter")
           └── CoverLetterResultCard (Copy text button, Save to DB button)
    ```

### 5. Job Search Screen
*   **File Path:** `feature/jobs/src/main/java/com/bangersoul/aivance/feature/jobs/JobsScreen.kt`
*   **ViewModel:** `JobsViewModel`
*   **Composable Hierarchy:**
    ```
    Scaffold
     └── Column
           ├── SearchBar (Query input)
           ├── FilterChipRow (Full-time, Remote, Contract)
           └── LazyColumn
                 └── JobListingCard (Company name, Role, Salary, Location, Apply button)
    ```

### 6. Interview Screen
*   **File Path:** `feature/interview/src/main/java/com/bangersoul/aivance/feature/interview/InterviewScreen.kt`
*   **ViewModel:** `InterviewViewModel`
*   **Composable Hierarchy:**
    ```
    Scaffold
     └── Column
           ├── Role & Difficulty Setup Card
           ├── LazyColumn (Chat Message Bubbles: User vs AI)
           ├── ChatInputField & SendButton
           └── FinishSessionButton -> FeedbackDialog
    ```

### 7. Application Tracker Screen
*   **File Path:** `feature/tracker/src/main/java/com/bangersoul/aivance/feature/tracker/TrackerScreen.kt`
*   **ViewModel:** `TrackerViewModel`
*   **Composable Hierarchy:**
    ```
    Scaffold / FloatingActionButton (Add Application)
     └── LazyColumn
           └── ApplicationCard (Status Badge, Role, Company, Date Applied, Salary, Action Menu)
    ```

### 8. Profile & Settings Screen
*   **File Path:** `feature/profile/src/main/java/com/bangersoul/aivance/feature/profile/ProfileScreen.kt`
*   **ViewModel:** `ProfileViewModel`
*   **Composable Hierarchy:**
    ```
    Scaffold
     └── LazyColumn
           ├── UserHeaderCard
           ├── ApiKeyConfigurationSection (Gemini API Key input)
           └── CareerRoadmapTimeline (Step 1..N with completion checkboxes)
    ```

---

## SECTION 5: VIEWMODEL AUDIT

| ViewModel | Module | Injected Dependencies | Exposed State | Coroutine Scope | Dispatched Context | Key Deficiencies / Debt |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `DashboardViewModel` | `:feature:dashboard` | `DashboardRepository` | `StateFlow<DashboardUiState>` | `viewModelScope` | Default / Main | Lacks pull-to-refresh trigger action. |
| `ResumeViewModel` | `:feature:resume` | `ResumeRepository`, `PdfTextExtractor` | `StateFlow<ResumeUiState>` | `viewModelScope` | `Dispatchers.IO` | Exception handling swallows detailed error cause. |
| `AtsViewModel` | `:feature:ats` | `AtsRepository` | `StateFlow<List<AtsResult>>` | `viewModelScope` | Main | Missing delete confirmation state. |
| `CoverLetterViewModel` | `:feature:coverletter` | `CoverLetterRepository` | `StateFlow<CoverLetterUiState>` | `viewModelScope` | `Dispatchers.IO` | No cancellation support for long AI generation runs. |
| `InterviewViewModel` | `:feature:interview` | `InterviewRepository` | `StateFlow<InterviewUiState>` | `viewModelScope` | `Dispatchers.IO` | Feedback action triggers stubbed response. |
| `JobsViewModel` | `:feature:jobs` | `JobSearchRepository` | `StateFlow<JobsUiState>` | `viewModelScope` | Main | Operates entirely on hardcoded mock list filters. |
| `TrackerViewModel` | `:feature:tracker` | `JobTrackerRepository` | `StateFlow<TrackerUiState>` | `viewModelScope` | `Dispatchers.IO` | Status updates do not emit UI events for undo. |
| `ProfileViewModel` | `:feature:profile` | `UserPreferences`, `RoadmapRepository` | `StateFlow<ProfileUiState>` | `viewModelScope` | `Dispatchers.IO` | API Key saving does not validate key syntax prior to writing. |

---

## SECTION 6: REPOSITORY AUDIT

| Repository Interface | Implementation Class | Module | Data Sources | Cache Strategy | Concurrency Safety | Defects / Bypasses |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `DashboardRepository` | `DashboardRepositoryImpl` | `:feature:dashboard` | `ApplicationDao`, `AtsDao` | Reactive Flow mapping | Thread-safe Flow | Combines DAO streams directly without in-memory caching. |
| `ResumeRepository` | `ResumeRepositoryImpl` | `:feature:resume` | `AiService`, `AtsDao` | None | Flow emission | Manual JSON parsing stripping ` ```json ` markers susceptible to malformed AI output. |
| `AtsRepository` | `AtsRepositoryImpl` | `:feature:ats` | `AtsDao` | Local Room persistence | Thread-safe Room | Direct mapping from entity to domain. |
| `CoverLetterRepository` | `CoverLetterRepositoryImpl` | `:feature:coverletter` | `AiService`, `CoverLetterDao` | Local Room persistence | Flow emission | Text prompt formatted inline without template management. |
| `InterviewRepository` | `InterviewRepositoryImpl` | `:feature:interview` | `AiService` | In-memory `MutableStateFlow` | Thread-safe `StateFlow` | **STUBBED:** `parseFeedback()` ignores AI response and returns hardcoded mock. |
| `JobSearchRepository` | `JobSearchRepositoryImpl` | `:feature:jobs` | **Hardcoded Mocks** | None | Non-suspending inline filter | **FAKE:** Complete mock implementation; no real search engine or network client. |
| `JobTrackerRepository` | `JobTrackerRepositoryImpl` | `:feature:tracker` | `ApplicationDao` | Local Room persistence | Thread-safe Room | None. Clean Room integration. |
| `RoadmapRepository` | `RoadmapRepositoryImpl` | `:feature:profile` | `RoadmapDao` | Local Room persistence | Thread-safe Room | Pre-populates default roadmap steps if DB empty. |

---

## SECTION 7: DATABASE AUDIT

### Database Class
*   `com.bangersoul.aivance.core.database.AivanceDatabase`
*   **Version:** 4
*   **Export Schema:** `false`
*   **Entities:** `AivanceEntity`, `ApplicationEntity`, `AtsResultEntity`, `CoverLetterEntity`, `RoadmapEntity`, `RoadmapStepEntity`.

### Entities & Field Specifications
1.  **`ApplicationEntity`** (`applications` table):
    *   `id`: `Long` (Primary Key, AutoGenerate)
    *   `company`: `String`
    *   `role`: `String`
    *   `status`: `String`
    *   `dateApplied`: `Long` (Epoch Millis)
    *   `salaryRange`: `String`
    *   `notes`: `String`
    *   `lastModified`: `Long` (Epoch Millis)
2.  **`AtsResultEntity`** (`ats_results` table):
    *   `id`: `Long` (Primary Key, AutoGenerate)
    *   `score`: `Int`
    *   `date`: `Long`
    *   `resumeName`: `String`
    *   `missingKeywords`: `String` (Comma-separated text)
    *   `feedback`: `String`
3.  **`CoverLetterEntity`** (`cover_letters` table):
    *   `id`: `Long` (Primary Key, AutoGenerate)
    *   `company`: `String`
    *   `role`: `String`
    *   `content`: `String`
    *   `dateCreated`: `Long`
    *   `tone`: `String`
4.  **`RoadmapEntity`** & **`RoadmapStepEntity`**:
    *   `RoadmapEntity`: `id`, `title`, `description`.
    *   `RoadmapStepEntity`: `id`, `roadmapId` (Foreign key), `title`, `isCompleted`, `stepOrder`.

### Database Architectural Defects
1.  **Missing Indexes:** No explicit `@Index` declarations on foreign keys (`roadmapId`) or queried filtering columns (`status`, `dateApplied`).
2.  **Schema Export Disabled:** `exportSchema = false` prevents auto-generating migration verification JSONs.
3.  **Destructive Migration Vulnerability:** No `Migration` implementations provided. Any future entity alteration will crash app installs on upgrade.

---

## SECTION 8: NETWORK AUDIT

### Architecture & Service Abstraction
*   **Core Contract:** `com.bangersoul.aivance.core.network.AiService`
*   **Implementations:**
    *   `GeminiAiService`: Uses `com.google.ai.client.generativeai.GenerativeModel` (Model: `"gemini-1.5-flash"`).
    *   `MockAiService`: Returns simulated responses for testing.
    *   `DelegatingAiService`: Dynamic wrapper injected into repositories. Checks `UserPreferences.geminiApiKey`; if non-empty, delegates calls to an dynamically instantiated `GeminiAiService`, otherwise falls back to `MockAiService`.

### Code Implementation (`DelegatingAiService.kt`)
```kotlin
@Singleton
class DelegatingAiService @Inject constructor(
    private val userPreferences: UserPreferences,
    private val mockAiService: MockAiService
) : AiService {
    override suspend fun analyzeText(prompt: String): Result<String> {
        val apiKey = userPreferences.geminiApiKey.first()
        return if (apiKey.isNotBlank()) {
            GeminiAiService(apiKey).analyzeText(prompt)
        } else {
            mockAiService.analyzeText(prompt)
        }
    }
}
```

### Network Defects & Production Risks
1.  **Direct Class Instantiation:** `DelegatingAiService` instantiates `GeminiAiService(apiKey)` directly on every invocation, bypassing dependency injection object pooling.
2.  **No OkHttp Interceptor Configuration:** Custom timeout handling, certificate pinning, and network logging interceptors are omitted.
3.  **Lack of Retry / Backoff Strategy:** Network glitches or HTTP 429 Rate Limit errors immediately fail without exponential backoff retries.

---

## SECTION 9: AI SYSTEM AUDIT

### Current AI Pipeline Overview
*   **Model:** `gemini-1.5-flash`
*   **Prompt Construction:** Hand-crafted String formatting in repository layers (`ResumeRepositoryImpl`, `CoverLetterRepositoryImpl`, `InterviewRepositoryImpl`).
*   **Structured Output:** Prompts request raw JSON strings. Repositories manually strip markdown tags (e.g. ````json ... ````) and decode via `kotlinx.serialization.json.Json`.

### Detailed AI Weaknesses
1.  **Fragile JSON Parsing:** If the AI model adds conversational prefix text prior to JSON output, string stripping fails and throws a runtime exception.
2.  **Single Provider Lock-In:** Hard-coded against Google Gemini SDK. No abstraction for OpenAI, Groq, Anthropic, or local LLMs.
3.  **No Token Usage or Cost Tracking:** Tokens spent during resume analysis or interview practice are not calculated or logged.
4.  **No Streaming Support:** Responses are returned as full block strings, producing notable UI latency during generation.

---

## SECTION 10: PROPOSED AI PROVIDER ARCHITECTURE

To support seamless runtime switching between Gemini, Groq, OpenAI, OpenRouter, and Ollama, the following provider-independent AI architecture is designed.

```
                  +--------------------------+
                  |        AiManager         |
                  +------------+-------------+
                               |
            +------------------+------------------+
            |                                     |
            v                                     v
+-----------------------+             +-----------------------+
|   ProviderRegistry    |             |  CapabilityRegistry   |
+-----------+-----------+             +-----------------------+
            |
    +-------+-------+-------------------+-------------------+
    |               |                   |                   |
    v               v                   v                   v
+-------+     +-----------+     +---------------+     +-----------+
|Gemini |     | OpenAI    |     |    Groq       |     |  Ollama   |
|Provider     | Provider  |     |   Provider    |     | Provider  |
+-------+     +-----------+     +---------------+     +-----------+
```

### Technical Interface Contracts

```kotlin
interface AiProvider {
    val id: ProviderId // GEMINI, OPENAI, GROQ, OPENROUTER, OLLAMA
    val capabilities: Set<AiCapability> // STREAMING, REASONING, SYSTEM_PROMPT, JSON_MODE
    
    suspend fun generateText(
        prompt: String,
        config: AiConfiguration
    ): Result<String>
    
    fun streamText(
        prompt: String,
        config: AiConfiguration
    ): Flow<String>
    
    suspend fun chat(
        messages: List<AiMessage>,
        config: AiConfiguration
    ): Result<String>
    
    suspend fun validateCredentials(apiKey: String, baseUrl: String?): Boolean
}

data class AiConfiguration(
    val modelName: String,
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val maxTokens: Int = 2048,
    val responseFormat: ResponseFormat = ResponseFormat.TEXT
)

enum class ResponseFormat { TEXT, JSON }
```

---

## SECTION 11: JOB SEARCH & SCRAPING AUDIT

### Current Implementation Audit
*   **State:** The current job search implementation in `:feature:jobs` is **100% Mocked**.
*   **Source File:** `feature/jobs/src/main/java/com/bangersoul/aivance/feature/jobs/data/JobSearchRepositoryImpl.kt`
*   **Defect:** `searchJobs()` performs inline string matches on an in-memory `listOf(JobListing(...))`. No network scraping, web API, or database caching is executed.

---

## SECTION 12: PROPOSED JOB PROVIDER ARCHITECTURE

To deliver real-world job scraping capabilities via Apify, LinkedIn, Indeed, and Google Jobs actors, the following provider architecture is designed.

```kotlin
interface JobProvider {
    val providerId: String
    suspend fun searchJobs(query: JobSearchQuery): Result<List<JobListing>>
    suspend fun fetchJobDetails(jobId: String): Result<JobListing>
}

data class JobSearchQuery(
    val keywords: String,
    val location: String,
    val isRemoteOnly: Boolean,
    val page: Int = 1,
    val limit: Int = 20
)

class ApifyJobProvider @Inject constructor(
    private val apifyApi: ApifyApiService,
    private val actorRegistry: ApifyActorRegistry
) : JobProvider {
    override val providerId = "APIFY"
    override suspend fun searchJobs(query: JobSearchQuery): Result<List<JobListing>> {
        // Dispatches scraping task to configured Apify Actor (e.g. LinkedIn / Indeed Scraper)
        val actorId = actorRegistry.getActorForTarget(query)
        val runResult = apifyApi.runActor(actorId, query)
        return runResult.map { it.toJobListings() }
    }
}
```

---

## SECTION 13: SETTINGS MODULE SPECIFICATION

A dedicated `:feature:settings` module must be created to centralize application preferences, replacing the current fragmented setup inside `:feature:profile`.

### Settings Layout & Sub-sections
1.  **General & Appearance:** Dark Mode toggle (System, Light, Dark), Dynamic Color (Material You).
2.  **AI Providers Config:** Active provider selection, API Key management, Model picker, Temperature slider, System Prompt editor, Connection Test button.
3.  **Job Search Config:** Active scraper provider (Apify / Direct API), Apify API Token, Selected Scraper Actor ID, Sync Interval, Cache retention period.
4.  **Database & Storage:** Cache clear, Export Database Backup, Restore Backup, Database stats.
5.  **Security:** Encrypted Storage toggle (EncryptedSharedPreferences / Tink), Biometric Lock toggle.

---

## SECTION 14: AI SETTINGS DESIGN SPECIFICATION

### Composable Configuration Wireframe
*   **Provider Dropdown:** `[ Google Gemini | OpenAI | Groq | OpenRouter | Ollama (Local) ]`
*   **Dynamic Model Selector:** Options auto-populate based on selected provider (e.g., `gemini-1.5-flash`, `gpt-4o-mini`, `llama-3.3-70b-versatile`).
*   **Credential Input:** `OutlinedTextField` with trailing password visibility icon for API Key entry.
*   **Parameter Controls:**
    *   *Temperature:* Slider (`0.0` to `1.0`, step `0.05`).
    *   *Max Tokens:* Number input field (`256` to `8192`).
*   **Action Row:** `[ Test Connection Button ]` -> Triggers `validateCredentials()`, showing status badge (Success: Green Check, Failure: Red Error with HTTP response code).

---

## SECTION 15: JOB SETTINGS DESIGN SPECIFICATION

### Composable Configuration Wireframe
*   **Provider Dropdown:** `[ Apify Scraper Engine | Direct Web API ]`
*   **Apify API Key Input:** Encrypted storage field for Apify token.
*   **Actor Selection Grid:**
    *   `[X] LinkedIn Job Scraper` (`actor_id: hMSc3Iq0A2R`)
    *   `[ ] Indeed Scraper` (`actor_id: mis3x~indeed`)
    *   `[ ] Glassdoor Scraper`
    *   `[ ] Custom Actor ID Input`
*   **Sync Settings:**
    *   *Sync Interval:* Dropdown (`Manual`, `Every 6 Hours`, `Daily`, `Weekly`).
    *   *Cache Retention:* Slider (`1 Day` to `30 Days`).

---

## SECTION 16: DESIGN SYSTEM AUDIT

### Component Audit Matrix

| Component | Class / File | Completeness | Dark Mode Support | Accessibility (TalkBack) | Issues / Enhancements |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Buttons** | `AivanceButtons.kt` | 90% | Yes | Partial | Lacks explicit minimum click target size (`48dp`) on custom small buttons. |
| **Cards** | `AivanceCards.kt` | 85% | Yes | Yes | Elevation hardcoded rather than using `CardDefaults.cardElevation()`. |
| **Text Fields** | `AivanceTextFields.kt` | 90% | Yes | Yes | Missing helper text parameter and character count counter. |
| **Loading** | `AivanceLoading.kt` | 100% | Yes | Yes | Centered `CircularProgressIndicator`. Fully functional. |
| **Error** | `AivanceError.kt` | 100% | Yes | Yes | Includes retry button callback. |
| **Success** | `AivanceSuccess.kt` | 100% | Yes | Yes | Displays success vector icon and text message. |
| **Analysis Card** | `AnalysisResultCard.kt` | 95% | Yes | Yes | Score ring progress visual styling cleanly formatted. |

---

## SECTION 17: COMPOSE AUDIT

### Codebase Compose Performance & Best Practices Audit
1.  **State Hoisting:** ViewModels expose read-only `StateFlow`. Composables pass events up via lambda parameters. **Grade: PASS**.
2.  **Unstable Data Class Re-compositions:** Models like `DashboardData` contain `List<RecentActivity>`. In Compose without `@Immutable` or `ImmutableList` (Kotlinx Collections), any list parameter causes the composable to be marked unstable, triggering unnecessary re-compositions.
3.  **Missing `derivedStateOf`:** In `InterviewScreen.kt`, list scrolling state calculations do not wrap derived comparisons in `remember { derivedStateOf { ... } }`, causing extra layout passes.
4.  **Preview Coverage:** Preview functions `@Preview` exist for basic design system components, but feature screens (`ResumeScreen`, `InterviewScreen`) lack `@PreviewParameter` mock providers.

---

## SECTION 18: SECURITY AUDIT

### Security Vulnerabilities & Production Risks
1.  **API Key Exposure in VCS:** `local.properties` or fallback strings in `GeminiAiService.kt` risk leaking secret keys into public source repositories.
2.  **Unencrypted Preferences DataStore:** `UserPreferences` writes keys to standard unencrypted JSON files in `/data/data/com.bangersoul.aivance/files/datastore/`. Rooted devices can read API keys directly.
3.  **Cleartext Traffic & Network Security Config:** No custom `res/xml/network_security_config.xml` is defined to restrict non-HTTPS cleartext traffic or enforce certificate pinning.
4.  **PDF Parser File Traversal Risk:** `FileUtils.kt` copies Uri streams without validating target path sandbox isolation, presenting a potential path traversal risk if malicious file URIs are supplied.

---

## SECTION 19: PERFORMANCE AUDIT

### Performance Analysis
1.  **PDF Parsing Main Thread Blocking:** Text extraction from PDF documents in `PdfTextExtractor.kt` executes file IO operations that must be strictly dispatched to `Dispatchers.IO` to prevent UI thread frame drops.
2.  **Database Query Optimization:** Room queries in `ApplicationDao` lack indexes on filtering columns, resulting in full table scans when searching tracked applications.
3.  **Baseline Profiles:** The project lacks a `:baselineprofile` module. Cold startup time is unoptimized for Jetpack Compose runtime initialization.

---

## SECTION 20: TESTING AUDIT

### Existing Test Suite Breakdown
*   **`:app`:** `ExampleUnitTest`, `ExampleInstrumentedTest`. (Basic stubs).
*   **`:core:database`:** `ApplicationDaoTest` (Tests insert, query, update, delete), `AtsDaoTest` (Tests insert and query latest score). Runs on Android Test runner / Robolectric.
*   **`:feature:resume`:** `ResumeViewModelTest` (Tests initial state, loading, success state, error handling), `ResumeRepositoryTest` (Mocks `AiService` and verifies Flow emissions).
*   **`:feature:dashboard`:** `DashboardScreenTest` (Compose UI Test verifying dashboard card rendering).
*   **`:navigation`:** `AivanceNavGraphTest` (Hilt Compose Test verifying tab switching).

### Coverage Gaps
*   No Unit Tests for `:feature:interview`, `:feature:coverletter`, `:feature:tracker`, `:feature:jobs`, or `:feature:profile`.
*   Zero tests for `:core:network` (`DelegatingAiService`).
*   Zero tests for `:core:util` (`PdfTextExtractor`).

---

## SECTION 21: BUG REGISTER

| Bug ID | Severity | Category | Affected Module | File(s) Affected | Root Cause | Current Behaviour | Expected Behaviour | Suggested Fix |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **BUG-001** | **CRITICAL** | Runtime Crash / API | `:core:util` | `PdfTextExtractor.kt` | Calls `PdfRenderer.textContents` introduced in API 35 (Android 15). | Throws `NoSuchMethodError` on Android 8.0–14 devices (API 26–34). | Parses PDF text safely across all supported minSdk 26+ devices. | Replace API 35 method with PDFBox-Android (`PDFTextStripper`) fallback. |
| **BUG-002** | **HIGH** | Database | `:core:database` | `AivanceDatabase.kt`, `DatabaseModule.kt` | Database version is 4, `exportSchema = false`, no `Migration` classes provided, `fallbackToDestructiveMigration()` omitted. | App crashes with `IllegalStateException` upon DB schema upgrade. | Database migrates preserved tables or safely falls back without crashing. | Enable schema export, define explicit Room Migrations, and configure fallback policy. |
| **BUG-003** | **HIGH** | Functionality | `:feature:jobs` | `JobSearchRepositoryImpl.kt` | Repository returns hardcoded static list; no network call or scraper connected. | Job Search displays 5 fake jobs regardless of real world query. | Queries real job search API / Apify scraper service. | Implement real network data source and `ApifyJobProvider`. |
| **BUG-004** | **MEDIUM** | Functionality | `:feature:interview` | `InterviewRepositoryImpl.kt` | `parseFeedback()` ignores AI response string and returns hardcoded fake string list. | AI Interview feedback always outputs "Good overall performance." | Parses real structured JSON feedback emitted by AI model. | Parse JSON via `kotlinx.serialization.json.Json` into `InterviewFeedback`. |
| **BUG-005** | **MEDIUM** | Security | `:core:datastore` | `UserPreferencesSerializer.kt`, `DataStoreModule.kt` | Writes API keys in plain-text JSON files. | API Keys readable by local root inspection. | Credentials stored in EncryptedSharedPreferences or Encrypted DataStore. | Use Android KeyStore EncryptedDataStore wrapper. |

---

## SECTION 22: TECHNICAL DEBT REGISTER

| Debt ID | Priority | Category | Implementation Effort | Risk Level | Description | Recommended Resolution |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **DEBT-001** | High | Architecture | Medium | High | Direct class instantiation in `DelegatingAiService.kt` bypassing Hilt object factory. | Refactor to `AiProviderFactory` managed by Hilt DI. |
| **DEBT-002** | High | Testing | High | Medium | Absence of unit test coverage across 5 out of 8 feature modules. | Write ViewModel & Repository test suites using Turbine & MockK. |
| **DEBT-003** | Medium | Navigation | Medium | Medium | Reliance on experimental `androidx.navigation3` v1.0.0-alpha01. | Isolate navigation contracts or pin stable alpha wrappers. |
| **DEBT-004** | Medium | Performance | Low | Low | Unstable list parameters in UI State models causing Compose re-compositions. | Annotate data classes with `@Immutable` or use `ImmutableList`. |
| **DEBT-005** | Low | Security | Low | Low | Missing custom `network_security_config.xml` and TLS certificate pinning. | Add XML network security config enforcing HTTPS only. |

---

## SECTION 23: FILES REQUIRING MODIFICATION

| File Path | Module | Reason for Modification | Priority | Estimated Effort | Dependencies | Owner |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `core/util/.../PdfTextExtractor.kt` | `:core:util` | Fix API 35 compatibility crash by replacing `PdfRenderer.textContents` with PDFBox. | **P0 (Critical)** | 2 Hours | `libs.pdfbox.android` | Lead Core Engineer |
| `core/database/.../AivanceDatabase.kt` | `:core:database` | Enable schema export, add migration definitions. | **P0 (Critical)** | 3 Hours | Room KSP | Database Architect |
| `feature/interview/.../InterviewRepositoryImpl.kt` | `:feature:interview` | Implement real AI feedback JSON parsing. | **P1 (High)** | 3 Hours | `kotlinx.serialization` | AI Feature Lead |
| `feature/jobs/.../JobSearchRepositoryImpl.kt` | `:feature:jobs` | Replace hardcoded mock jobs with real Apify scraper client. | **P1 (High)** | 8 Hours | Apify SDK / Retrofit | Feature Lead |
| `core/datastore/.../DataStoreModule.kt` | `:core:datastore` | Encrypt API keys stored in preferences. | **P1 (High)** | 4 Hours | Android KeyStore | Security Lead |
| `core/network/.../DelegatingAiService.kt` | `:core:network` | Refactor to generic provider factory framework. | **P2 (Medium)** | 6 Hours | Hilt | AI Architect |

---

## SECTION 24: MASTER IMPLEMENTATION ROADMAP

### Phase 1: Critical Production Blockers (Immediate Fixes)
*   **Objective:** Eliminate app-crashing bugs and security vulnerabilities.
*   **Tasks:**
    1.  Fix `PdfTextExtractor.kt` API 35 `NoSuchMethodError` by replacing native `textContents` with PDFBox Android text extraction.
    2.  Configure Room database schema export and fallback/migration handling in `:core:database`.
    3.  Encrypt API keys in `:core:datastore` using Android KeyStore / EncryptedSharedPreferences.
*   **Success Criteria:** Zero crashes on minSdk 26 devices during PDF upload; schema changes do not crash DB.
*   **Estimated Effort:** 1.5 Days.

### Phase 2: Feature & Repository Completion
*   **Objective:** Replace mock implementations with production-ready real services.
*   **Tasks:**
    1.  Implement JSON feedback parsing in `InterviewRepositoryImpl.kt`.
    2.  Build network API data source for `:feature:jobs` to fetch real job listings.
    3.  Add PDF cover letter export capability to `:feature:coverletter`.
*   **Success Criteria:** Interview feedback dynamically reflects chat conversation; Job search returns actual web listings.
*   **Estimated Effort:** 4 Days.

### Phase 3: AI & Job Provider Abstraction Architecture
*   **Objective:** Introduce pluggable provider architecture for multi-LLM and multi-scraper support.
*   **Tasks:**
    1.  Implement `AiProvider`, `ProviderRegistry`, and `AiProviderFactory` supporting Gemini, OpenAI, Groq, OpenRouter, and Ollama.
    2.  Implement `JobProvider` and `ApifyJobProvider` for multi-actor job scraping.
    3.  Create centralized `:feature:settings` module for provider switching and API key management.
*   **Success Criteria:** Users can switch between Gemini, Groq, or local Ollama models in Settings at runtime.
*   **Estimated Effort:** 5 Days.

### Phase 4: UI/UX, Performance & Production Hardening
*   **Objective:** Polish UI design, optimize Compose performance, and establish full test coverage.
*   **Tasks:**
    1.  Add Baseline Profile module (`:baselineprofile`) to optimize app startup.
    2.  Mark state data classes with `@Immutable` to eliminate unnecessary Compose re-compositions.
    3.  Expand Unit & Integration test coverage across all feature modules to >80%.
*   **Success Criteria:** Cold startup < 1.5s; test coverage > 80%; smooth 60fps Compose scrolling.
*   **Estimated Effort:** 4 Days.

---
*End of Master Engineering Specification for Aviance.*
