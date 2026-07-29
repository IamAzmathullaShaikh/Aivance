# AVIANCE - MASTER ENGINEERING PLAN & PRODUCTION ROADMAP

**Document Type:** Master Engineering Implementation Blueprint, Technical Architecture Plan, and Release Roadmap  
**Target Repository:** Aviance (Android Application)  
**Package Root:** `com.bangersoul.aivance`  
**Authors:** Chief Software Architect, Principal Android Engineer, Staff UI/UX Architect, Principal QA Engineer, Staff Security Engineer, Staff Performance Engineer, Lead Technical Program Manager  
**Status:** Approved Master Implementation Plan / Executable Baseline  
**Reference Document:** `Audit.md` (Master Engineering Specification & Architectural Audit)  

---

## SECTION 1: EXECUTIVE ENGINEERING SUMMARY

### Purpose & Objective
This Master Engineering Plan translates the technical deficiencies, architectural bottlenecks, and security vulnerabilities identified in `Audit.md` into an actionable, production-grade engineering roadmap for the Aviance Android application. The core strategic objective is to maximize systemic utility, performance, and long-term maintainability while minimizing operational risk, user friction, and engineering effort.

---

### Maturity & Readiness Evaluation

| Dimension | Current Rating | Baseline Audit Finding (`Audit.md`) | Target Production Standard | Gap Analysis & Strategic Remedy |
| :--- | :--- | :--- | :--- | :--- |
| **Current Maturity** | **MVP / Alpha** | Multi-module structure exists (~65 Kotlin files across 16 Gradle modules), but key features rely on hardcoded static mocks or stubbed parsers. | Production-grade, fully connected, robust Android application. | Transition mock repositories to live network/scraping data sources and implement missing core capabilities. |
| **Architecture Maturity** | **Moderate** | Clean Architecture and Unidirectional Data Flow (UDF) are established; however, tight coupling to Google Gemini SDK and experimental `androidx.navigation3` v1.0.0-alpha01 introduces instability. | Fully decoupled architecture using Pluggable Provider pattern for AI and Job engines with encapsulated routing contracts. | Introduce `AiProvider` and `JobProvider` interfaces with Hilt provider registries and dynamic factories. |
| **Feature Maturity** | **Partial (60%)** | UI screens are implemented for 8 features, but Job Search is 100% hardcoded mock data, Interview feedback parser returns static strings, and Cover Letter lacks PDF export. | 100% functional features with real-time AI streaming, dynamic job scraping, and document generation. | Complete backend data sources, replace mock implementations, and introduce a dedicated Settings feature module. |
| **Production Readiness** | **NO-GO** | **P0 Critical Bug:** `PdfTextExtractor.kt` calls `PdfRenderer.textContents` (API 35+ only), crashing PDF upload on Android 8.0–14 (API 26–34, ~95% of active devices). Plain-text API key storage in DataStore. | 0% crash rate on supported SDKs (26–35); hardware-backed key encryption; robust schema migrations. | Execute P0 production blocker fixes immediately prior to feature enhancements. |
| **Technical Debt** | **High** | Direct class instantiation in `DelegatingAiService.kt`, unstable Compose list parameters, unhandled Room schema export, missing network interceptors. | Minimal debt with strict linting, zero direct instantiations, and standardized network stack. | Implement `AiProviderFactory`, apply `@Immutable` annotations, and enforce Room schema migrations. |
| **Testing Maturity** | **Low (< 25%)** | Unit test coverage is sparse; missing tests across 5 out of 8 feature modules (`:feature:interview`, `:feature:coverletter`, `:feature:tracker`, `:feature:jobs`, `:feature:profile`). Zero network/util tests. | > 80% total codebase coverage; > 90% core module coverage with automated CI pipeline. | Build comprehensive unit, DAO, migration, Compose UI, and integration test suites using Turbine and MockK. |
| **Security Maturity** | **Vulnerable** | API keys stored in plain-text JSON via DataStore; fallback keys in code; missing custom `network_security_config.xml` and TLS pinning. | Hardware-backed Android Keystore encryption; zero plain-text secrets; enforced HTTPS and certificate pinning. | Implement Encrypted DataStore with Tink/Keystore, sanitize VCS from fallback secrets, and enforce network security config. |
| **Performance Maturity** | **Unoptimized** | Main-thread PDF I/O potential, recomposition overhead from unannotated list parameters, missing `:baselineprofile` startup pre-compilation. | Cold startup < 1.2s; 60 FPS Compose rendering; zero main-thread blocking file operations. | Add Baseline Profiles module, optimize Compose state stability, and strictly enforce `@Dispatcher(AivanceDispatchers.IO)`. |
| **Maintainability** | **Moderate** | Good module boundaries, but fragile string-stripping JSON parsers and inline prompt construction create high maintenance friction. | Standardized prompt template engine, type-safe JSON schema enforcement, and modular feature separation. | Move prompts to template managers and adopt structured output parsing with fallback handlers. |
| **Scalability** | **Limited** | Locked into single LLM vendor (Gemini) and unable to ingest live job postings dynamically from diverse web sources. | Multi-LLM runtime switching (Gemini, OpenAI, Groq, Ollama) and multi-actor scraper pipeline (Apify). | Deploy provider-independent AI and Job architectures with dynamic runtime selection. |

---

### Overall System Risk Assessment & Go/No-Go Decision

*   **Current Risk Level:** **CRITICAL HIGH**
    *   *Primary Risk Vector 1:* Fatal runtime crash (`NoSuchMethodError`) during PDF resume uploads on API 26–34 devices due to `PdfRenderer.textContents` call in `PdfTextExtractor.kt`.
    *   *Primary Risk Vector 2:* Security exposure of user API credentials due to plain-text DataStore JSON serialization in `/data/data/com.bangersoul.aivance/files/datastore/`.
    *   *Primary Risk Vector 3:* Operational failure during database schema upgrades due to missing Room migration handling (`exportSchema = false`, no `Migration` objects defined, `fallbackToDestructiveMigration()` omitted).
*   **Executive Release Recommendation:** **NO-GO FOR PRODUCTION RELEASE IN CURRENT STATE**
    *   *Conditional Approval:* Immediate authorization granted for execution of **Milestone 1 (Critical Production Blockers)** followed by sequential execution of Milestones 2 through 8 as detailed in this plan.

---

## SECTION 2: ENGINEERING PRIORITIES

### Priority Classification Matrix

To maximize engineering ROI and system utility, tasks are prioritized into five strict tiers:

*   **P0: Production Blockers** — Immediate app-crashing bugs, security vulnerabilities, or catastrophic data loss risks.
*   **P1: Critical Functionality** — Core user flows that are non-functional, mocked, or severely broken.
*   **P2: Architecture & Scalability** — Core modularization, provider abstractions, and settings management.
*   **P3: UX & Component Polish** — Accessibility, design system standardization, and usability enhancements.
*   **P4: Enhancements & Performance** — Startup pre-compilation, advanced telemetry, and non-critical optimizations.

```
+-----------------------------------------------------------------------------------+
| PRIORITY MATRIX                                                                   |
+-----------------------------------------------------------------------------------+
| P0: Production Blockers    --> PDF Parser Fix, Encrypted Keys, Room Migrations   |
| P1: Critical Functionality --> Real Job Scraper, AI Feedback Parser, Network Stack|
| P2: Architecture          --> AI Provider Framework, Job Platform, Settings Module|
| P3: UX & Polish           --> PDF Export, Touch Targets, Pull-to-Refresh          |
| P4: Enhancements          --> Baseline Profiles, Advanced Telemetry, Cost Tracking|
+-----------------------------------------------------------------------------------+
```

---

### Priority Itemized Task Breakdown

| Task ID | Priority | Task Title | Reason & Impact | Dependencies | Risk | Estimated Effort | Business & Utility Impact |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TSK-P0-01** | **P0** | Fix `PdfTextExtractor` API Level 35 Incompatibility | `PdfRenderer.textContents` crashes API 26–34 devices with `NoSuchMethodError`. Replaces with PDFBox-Android (`PDFTextStripper`). | `libs.pdfbox.android` | Low | 3 Hours | Eliminates app crashes for ~95% of target user base during resume upload. |
| **TSK-P0-02** | **P0** | Implement Encrypted DataStore for API Keys | Plain-text JSON preference storage exposes Gemini API keys on rooted devices or via ADB backup. | Android Keystore / Tink | Medium | 5 Hours | Prevents credential theft, liability, and API key quota abuse. |
| **TSK-P0-03** | **P0** | Room Schema Export & Migration Management | `exportSchema = false` and missing `Migration` definitions cause app crash (`IllegalStateException`) on schema upgrade. | Room KSP | Medium | 4 Hours | Prevents user data loss and app crashes upon updating the application. |
| **TSK-P1-01** | **P1** | Implement Real AI Feedback Parsing in Interview | `InterviewRepositoryImpl.kt` ignores AI response and outputs static mock strings. Must parse structured JSON. | `:core:network`, `kotlinx.serialization` | Low | 4 Hours | Transforms interview practice from static demo into value-delivering AI simulator. |
| **TSK-P1-02** | **P1** | Replace Mock Job Search with Real Apify Scraper | `JobSearchRepositoryImpl.kt` returns 5 static hardcoded jobs. Needs live web scraping via Apify REST API. | Retrofit, Apify API Token | High | 12 Hours | Unlocks core product value proposition: real-world job discovery. |
| **TSK-P1-03** | **P1** | Network Stack Hardening (OkHttp, Retry, Logging) | No retry backoff, custom timeouts, or connection pooling; network drops fail immediately without recovery. | OkHttp 4.12.0, Retrofit | Low | 6 Hours | Ensures network resilience over unstable mobile cellular connections. |
| **TSK-P2-01** | **P2** | Pluggable AI Provider Architecture | Single vendor lock-in to Gemini. Implement `AiProvider`, `ProviderRegistry`, and dynamic runtime switching. | `:core:network`, `:core:datastore` | Medium | 14 Hours | Enables multi-LLM support (Gemini, OpenAI, Groq, Ollama), reducing downtime risks. |
| **TSK-P2-02** | **P2** | Job Platform Scraper Architecture | Standardize job scraping pipeline with normalization, deduplication, ranking, and Room caching. | `:core:database`, `:feature:jobs` | High | 16 Hours | Provides scalable infrastructure for ingesting jobs from multiple job boards. |
| **TSK-P2-03** | **P2** | Build Dedicated `:feature:settings` Module | Settings options are fragmented or missing. Build modular settings UI across 12 sub-pages. | `:core:designsystem`, `:navigation` | Medium | 16 Hours | Centralizes configuration, improving user control and app maintainability. |
| **TSK-P2-04** | **P2** | Encapsulate Experimental Navigation 3 Contracts | Direct usage of `androidx.navigation3` alpha exposes UI to breaking library changes. | `:navigation` | Medium | 8 Hours | Insulates codebase against upstream Jetpack Compose alpha API shifts. |
| **TSK-P3-01** | **P3** | Cover Letter Export to PDF & Clipboard | Cover letters generated in UI cannot be exported to standard PDF format for job applications. | PDFBox Android, Android Printing | Low | 6 Hours | Completes cover letter creation workflow for end users. |
| **TSK-P3-02** | **P3** | Design System Accessibility & Target Sizes | Custom small buttons lack 48dp minimum touch targets; text fields missing helper text/counters. | `:core:designsystem` | Low | 5 Hours | Ensures WCAG accessibility compliance and improves touch accuracy. |
| **TSK-P3-03** | **P3** | Dashboard Pull-To-Refresh & State Sync | Dashboard lacks pull-to-refresh pull trigger to re-fetch database counts and recent activities. | Compose Material3 | Low | 3 Hours | Enhances perceived app responsiveness and user control over state updates. |
| **TSK-P4-01** | **P4** | Baseline Profiles Setup (`:baselineprofile`) | Cold startup and initial Compose layout passes are unoptimized. | Macrobenchmark | Medium | 8 Hours | Reduces cold app startup time by 30–40% and eliminates frame drops on launch. |
| **TSK-P4-02** | **P4** | Compose State Stability Optimization | Unstable list parameters in domain state classes trigger unnecessary recompositions during scroll. | Kotlinx Immutable Collections | Low | 6 Hours | Reduces CPU utilization and battery drain during scroll-heavy list views. |
| **TSK-P4-03** | **P4** | AI Cost Tracking & Telemetry Dashboard | Users have no visibility into token usage, request latency, or estimated LLM operational costs. | `:core:datastore`, `:feature:settings` | Low | 6 Hours | Empowers power users to monitor and optimize AI API token consumption. |

---

## SECTION 3: FEATURE IMPLEMENTATION MATRIX

The following feature implementation matrix specifies the current state, target state, required architectural changes, and acceptance criteria for all 9 application features.

---

### 1. Dashboard Feature (`:feature:dashboard`)
*   **Current Completion:** 90%
*   **Target Completion:** 100%
*   **Missing Implementation:** Pull-to-refresh action trigger; dynamic profile completion percentage calculation based on actual DB records; explicit loading state retry handlers.
*   **Dependencies:** `:core:database`, `:core:designsystem`, `:core:common`
*   **Repository Changes:** Update `DashboardRepositoryImpl` to combine `ApplicationDao`, `AtsDao`, `CoverLetterDao`, and `RoadmapDao` reactive flows into a single unified `DashboardData` stream with error handling.
*   **Database Changes:** Add reactive counting queries in `AtsDao` and `CoverLetterDao` (`fun getCount(): Flow<Int>`).
*   **Network Changes:** None.
*   **AI Changes:** None.
*   **UI Changes:** Implement `PullToRefreshBox` wrapping `LazyColumn` in `DashboardScreen.kt`; add shimmer placeholders during initial fetch.
*   **Testing Required:** `DashboardViewModelTest` (verifying flow combination), `DashboardScreenTest` (verifying UI state transitions: Loading, Content, Empty, Error).
*   **Acceptance Criteria:** Pull-to-refresh correctly re-queries underlying DAOs; profile completion percentage updates dynamically as user adds resume/cover letter records; zero UI lag during stream emission.

---

### 2. Resume Feature (`:feature:resume`)
*   **Current Completion:** 80%
*   **Target Completion:** 100%
*   **Missing Implementation:** Robust PDF text extraction across API 26–34; cancellation of long-running AI analysis tasks; file size validation prior to processing.
*   **Dependencies:** `:core:util`, `:core:network`, `:core:database`, `libs.pdfbox.android`
*   **Repository Changes:** Update `ResumeRepositoryImpl` to sanitize AI markdown tags (e.g., ````json ... ````) robustly using Regex fallback parsing and store parsed `AtsResultEntity` into Room.
*   **Database Changes:** Ensure `AtsResultEntity` supports storing raw job description text alongside score results.
*   **Network Changes:** Wire request to active `AiProvider` with retry backoff for HTTP 429 rate limits.
*   **AI Changes:** Enforce structured JSON schema prompt output (`score`, `matchingKeywords`, `missingKeywords`, `formattingIssues`, `actionableTips`).
*   **UI Changes:** Display progress percentage indicator during multi-second AI parsing; add clear file detachment button; show character count warning for text input mode.
*   **Testing Required:** `ResumeViewModelTest` (testing upload success/failure), `ResumeRepositoryTest` (testing JSON parsing with malformed AI responses), `PdfTextExtractorTest` (testing PDF extraction on sample documents).
*   **Acceptance Criteria:** PDF text extraction succeeds on API 26 through 35 without `NoSuchMethodError`; malformed AI responses do not crash the app and show structured error UI; analysis results persist in database.

---

### 3. ATS History Feature (`:feature:ats`)
*   **Current Completion:** 85%
*   **Target Completion:** 100%
*   **Missing Implementation:** Search query filter for past scans; swipe-to-delete ATS record with undo snackbar; detailed breakdown dialog for historical scans.
*   **Dependencies:** `:core:database`, `:core:designsystem`
*   **Repository Changes:** Add `deleteAtsResult(id: Long)` and `searchAtsResults(query: String)` in `AtsRepositoryImpl`.
*   **Database Changes:** Add `@Query("DELETE FROM ats_results WHERE id = :id")` and search query in `AtsDao`. Add index on `date` column.
*   **Network Changes:** None.
*   **AI Changes:** None.
*   **UI Changes:** Add `OutlinedTextField` search bar at top of `AtsScreen.kt`; implement swipe-to-dismiss gesture on item cards with `SnackbarHostState` undo trigger.
*   **Testing Required:** `AtsDaoTest` (testing deletion and search queries), `AtsViewModelTest` (testing search state filtering and undo logic).
*   **Acceptance Criteria:** Users can search historical scan results by resume name or keyword; deleting a scan record updates Room immediately and allows undo within 4 seconds.

---

### 4. Cover Letter Feature (`:feature:coverletter`)
*   **Current Completion:** 85%
*   **Target Completion:** 100%
*   **Missing Implementation:** Export cover letter to PDF document; copy-to-clipboard feedback confirmation; template prompt management for letter tones.
*   **Dependencies:** `:core:network`, `:core:database`, `:core:util`
*   **Repository Changes:** Update `CoverLetterRepositoryImpl` to support template-based prompt generation for custom tone parameters (`Professional`, `Enthusiastic`, `Executive`, `Creative`).
*   **Database Changes:** Add `tone` and `jobTitle` explicit columns in `CoverLetterEntity` if missing, with database migration script.
*   **Network Changes:** Wire generation to `AiProvider.generateText()` or `AiProvider.streamText()` for real-time text streaming.
*   **AI Changes:** Add system prompt instructions enforcing professional business letter formatting standards.
*   **UI Changes:** Add "Export PDF" icon button on generated letter result card; implement streaming text display as tokens arrive from AI service; show copy feedback toast.
*   **Testing Required:** `CoverLetterViewModelTest` (testing generation flow and streaming), `CoverLetterRepositoryTest` (verifying prompt formatting and DAO persistence).
*   **Acceptance Criteria:** Generated cover letters stream in real-time onto screen; PDF export generates a clean formatted `.pdf` document saved to local device storage.

---

### 5. Interview Feature (`:feature:interview`)
*   **Current Completion:** 60%
*   **Target Completion:** 100%
*   **Missing Implementation:** Real AI structured JSON feedback parser; session history persistence; audio speech-to-text input setup hooks.
*   **Dependencies:** `:core:network`, `:core:database`
*   **Repository Changes:** Replace stubbed `parseFeedback()` in `InterviewRepositoryImpl.kt` with actual `kotlinx.serialization` decoder mapping AI JSON response into `InterviewFeedback(overallScore, strengths, improvements, detailedSummary)`.
*   **Database Changes:** Create `InterviewSessionEntity` and `InterviewMessageEntity` tables to persist chat history across app restarts.
*   **Network Changes:** Utilize multi-turn `AiProvider.chat()` API with full conversation context buffer management.
*   **AI Changes:** Define strict system instructions for mock interviewer persona (Technical Lead, HR Manager, Behavioral Evaluator).
*   **UI Changes:** Upgrade feedback modal to render score progress gauge, bulleted strengths/improvements chips, and actionable recommendations.
*   **Testing Required:** `InterviewRepositoryTest` (testing real AI feedback JSON parsing vs edge cases), `InterviewViewModelTest` (testing chat message append and session completion).
*   **Acceptance Criteria:** Feedback summary displays real AI analysis derived from the actual conversation history; past chat sessions are saved and reviewable from history list.

---

### 6. Jobs Feature (`:feature:jobs`)
*   **Current Completion:** 20%
*   **Target Completion:** 100%
*   **Missing Implementation:** **COMPLETE OVERHAUL.** Replace 100% static hardcoded list (`JobSearchRepositoryImpl`) with live network scraper client via Apify REST API, data normalization engine, and Room caching.
*   **Dependencies:** `:core:network`, `:core:database`, `:core:datastore`, `Apify API`
*   **Repository Changes:** Rebuild `JobSearchRepositoryImpl` to query `JobProvider` (`ApifyJobProvider`), execute scraper actor tasks, poll dataset results, normalize schemas, and cache into Room.
*   **Database Changes:** Create `JobListingEntity` table with columns (`id`, `title`, `company`, `location`, `salary`, `description`, `url`, `isRemote`, `postedDate`, `cachedTimestamp`). Add FTS4 full-text search index.
*   **Network Changes:** Create `ApifyApiService` Retrofit interface (`/v2/acts/{actorId}/runs`, `/v2/datasets/{datasetId}/items`).
*   **AI Changes:** Implement optional AI match relevance scoring comparing user profile against job listings.
*   **UI Changes:** Add filter chips (Remote, Full-time, Salary range), search bar query input, pagination scroll listener (`LazyColumn`), and external application link launcher.
*   **Testing Required:** `ApifyJobProviderTest` (mocking Apify REST responses), `JobSearchRepositoryTest` (testing network-to-database cache sync and deduplication), `JobsViewModelTest`.
*   **Acceptance Criteria:** Job search queries real web listings via Apify scraper; results are cached locally in Room for offline access; duplicate postings are eliminated automatically.

---

### 7. Application Tracker Feature (`:feature:tracker`)
*   **Current Completion:** 85%
*   **Target Completion:** 100%
*   **Missing Implementation:** Follow-up reminder notification dispatch via WorkManager (`FollowUpWorker`); application status transition history; interview date picker.
*   **Dependencies:** `:core:database`, `:app` (WorkManager)
*   **Repository Changes:** Expand `JobTrackerRepositoryImpl` to schedule/cancel WorkManager follow-up notification alerts when application status changes to `APPLIED` or `INTERVIEWING`.
*   **Database Changes:** Add `followUpDate: Long?` and `interviewDate: Long?` columns to `ApplicationEntity`.
*   **Network Changes:** None.
*   **AI Changes:** None.
*   **UI Changes:** Add DatePicker modal for setting follow-up reminders; add filter tabs (`All`, `Applied`, `Interviewing`, `Offered`, `Rejected`); add swipe-to-update status action.
*   **Testing Required:** `JobTrackerRepositoryTest` (verifying WorkManager job scheduling), `TrackerViewModelTest` (verifying filtering and state updates), `ApplicationDaoTest`.
*   **Acceptance Criteria:** Users can set interview/follow-up dates; WorkManager dispatches system notifications on target dates; filter tabs correctly segregate applications by status.

---

### 8. Profile Feature (`:feature:profile`)
*   **Current Completion:** 75%
*   **Target Completion:** 100%
*   **Missing Implementation:** API key validation prior to saving; user resume profile data editing (skills, experience summary, target roles); navigation link to new Settings module.
*   **Dependencies:** `:core:datastore`, `:core:database`
*   **Repository Changes:** Update `RoadmapRepositoryImpl` to support dynamic AI-generated career roadmaps based on user profile skills and target role.
*   **Database Changes:** Ensure `RoadmapEntity` and `RoadmapStepEntity` properly handle foreign key cascading deletes (`onDelete = ForeignKey.CASCADE`).
*   **Network Changes:** Wire profile validation to `AiProvider.validateCredentials()`.
*   **AI Changes:** Add roadmap generation prompt producing structured multi-step career progression milestones.
*   **UI Changes:** Add edit profile dialog (Target Role, Core Skills, Years Experience); add "Settings" gear icon in top app bar navigating to `:feature:settings`; add validation status indicator for API key input.
*   **Testing Required:** `ProfileViewModelTest` (testing key validation and profile updates), `RoadmapDaoTest` (testing cascade deletion of roadmap steps).
*   **Acceptance Criteria:** API keys are validated against remote endpoints before saving; updating user target role dynamically regenerates customized career roadmap milestones.

---

### 9. Settings Feature (`:feature:settings`) — **NEW MODULE**
*   **Current Completion:** 0%
*   **Target Completion:** 100%
*   **Missing Implementation:** Dedicated settings feature module (`:feature:settings`) providing central management for AI providers, scraper tokens, database backup/restore, security policies, and application preferences.
*   **Dependencies:** `:core:designsystem`, `:core:datastore`, `:core:database`, `:core:network`
*   **Repository Changes:** Create `SettingsRepositoryImpl` exposing flow streams for dark mode, AI provider credentials, scraper settings, and database management.
*   **Database Changes:** Provide database backup dump (`AivanceDatabase.close()`, file copy) and restore validation.
*   **Network Changes:** Test connection endpoints for Gemini, OpenAI, Groq, Ollama, and Apify.
*   **AI Changes:** Allow custom system prompt overrides and default parameter adjustments (temperature, max tokens).
*   **UI Changes:** Build 12 modular sub-pages (`General`, `Appearance`, `Notifications`, `AI Providers`, `Job Providers`, `Storage`, `Privacy`, `Security`, `Developer`, `About`, `Backup`, `Restore`).
*   **Testing Required:** `SettingsViewModelTest`, `SettingsRepositoryTest`, `EncryptedDataStoreTest`.
*   **Acceptance Criteria:** Centralized control over all app configurations; dynamic AI provider switching takes effect immediately without app restart; database backup/restore successfully exports/imports encrypted archive.

---

## SECTION 4: ARCHITECTURE MODERNIZATION PLAN

### Current Architecture & Identified Flaws

The current architecture follows a multi-module design (`:app`, `:navigation`, `:core:*`, `:feature:*`). However, an in-depth code evaluation reveals critical structural weaknesses:

1.  **Direct Class Instantiation in Core Services:** `DelegatingAiService.kt` directly instantiates `GeminiAiService(apiKey)` on every execution, bypassing Hilt dependency injection, breaking instance pooling, and preventing unit testing isolation.
2.  **Hardcoded Data Sources:** `JobSearchRepositoryImpl.kt` returns inline static lists, violating clean architecture data source boundaries.
3.  **Fragile Custom Navigation Alpha:** Dependency on `androidx.navigation3:navigation3-ui:1.0.0-alpha01` uses direct `backStack` list manipulation (`backStack.removeAt(...)`), risking runtime index out-of-bound crashes during rapid tab navigation.
4.  **DataStore Null-Safety & Plain-Text Risks:** `UserPreferencesSerializer.kt` reads unencrypted JSON from disk without fallback error recovery routines.

```
CURRENT ARCHITECTURE (Fragile & Tightly Coupled)
[Composables] --> [ViewModels] --> [DelegatingAiService] ==(Direct Instantiation)==> [GeminiAiService]
                               --> [JobSearchRepo]       ==(Hardcoded Mock List)==> [Static Jobs Data]

TARGET ARCHITECTURE (Decoupled, Pluggable & Production-Grade)
[Composables] --> [ViewModels] --> [Domain Use Cases] --> [AiProviderManager]  ==> [ProviderRegistry] --> [Gemini / OpenAI / Groq / Ollama]
                                                       --> [JobProviderManager] ==> [ActorRegistry]    --> [Apify Scraper / REST Client]
```

---

### Target Production Architecture

The modernized architecture enforces strict decoupling using the **Pluggable Provider Pattern**, Hilt Dependency Injection object factories, Encrypted DataStore persistence, and encapsulated navigation contracts.

*   **Core Abstraction Layer:** Define `AiProvider` and `JobProvider` interface contracts in `:core:network`.
*   **Registry & Factory Pattern:** Implement `AiProviderRegistry` and `JobProviderRegistry` managed by `@Singleton` Hilt modules, allowing dynamic runtime registration and resolution.
*   **Security Wrapper:** All sensitive key-value preferences wrap Android Keystore Tink AEAD encryption before disk persistence.
*   **Database Safety Layer:** Explicit Room schema exports (`exportSchema = true`) and strict `Migration` paths replacing destructive fallback strategies.

---

### Migration & Rollback Strategy

```
MIGRATION PHASES
Phase 1: Abstract Network Contracts  --> Phase 2: Implement Provider Registries --> Phase 3: Update Repositories --> Phase 4: Modernize Navigation
```

1.  **Phase 1 (Non-Breaking Abstraction):** Introduce `AiProvider` and `JobProvider` interfaces alongside existing services.
2.  **Phase 2 (Registry & DI Wiring):** Bind `GeminiAiProvider`, `OpenAiProvider`, `ApifyJobProvider`, and `MockJobProvider` into Hilt registries.
3.  **Phase 3 (Repository Migration):** Update `ResumeRepositoryImpl`, `InterviewRepositoryImpl`, and `JobSearchRepositoryImpl` to inject provider managers instead of direct services.
4.  **Phase 4 (Encapsulation & Navigation Stabilization):** Wrap `androidx.navigation3` backstack operations inside a type-safe `AivanceNavigator` class.

*   **Risk Assessment:** Medium risk of preference deserialization failure during DataStore encryption migration.
*   **Rollback Strategy:** Implement automatic backup copy of `user_preferences.json` prior to encryption migration; if Tink decryption fails, fall back to cleartext reader, re-encrypt, and log non-fatal error to telemetry.

---

### Affected Files & Acceptance Criteria

*   **Files Affected:**
    *   `core/network/src/.../AiService.kt` -> Refactor to `AiProvider.kt`
    *   `core/network/src/.../DelegatingAiService.kt` -> Replace with `AiProviderManager.kt`
    *   `feature/jobs/src/.../JobSearchRepositoryImpl.kt` -> Rebuild with `JobProvider`
    *   `navigation/src/.../AivanceNavGraph.kt` -> Encapsulate backstack operations
    *   `core/datastore/src/.../DataStoreModule.kt` -> Inject Encrypted DataStore
*   **Acceptance Criteria:** Zero direct class instantiations of network services in feature modules; 100% DI coverage via Hilt; navigation backstack operations executed through type-safe navigator; zero preference loss during encryption migration.

---

## SECTION 5: AI PLATFORM IMPLEMENTATION PLAN

### Platform Architecture & Components

The modernized AI Platform provides vendor-agnostic LLM integration supporting Google Gemini, OpenAI, Groq, OpenRouter, and local Ollama instances.

```
+-----------------------------------------------------------------------------------+
| AI PLATFORM ARCHITECTURE                                                          |
+-----------------------------------------------------------------------------------+
|                                 [AiProviderManager]                               |
|                                          |                                        |
|             +----------------------------+----------------------------+           |
|             |                                                         |           |
|  [ProviderRegistry]                                       [CapabilityRegistry]    |
|             |                                                         |           |
|   +---------+---------+---------+---------+               (STREAMING, REASONING,  |
|   |         |         |         |         |                JSON_MODE, VISION)     |
| [Gemini] [OpenAI]   [Groq] [OpenRouter] [Ollama]                                  |
+-----------------------------------------------------------------------------------+
```

---

### Technical Specification & Interface Contracts

```kotlin
// Core Contract in :core:network
interface AiProvider {
    val id: ProviderId // GEMINI, OPENAI, GROQ, OPENROUTER, OLLAMA
    val capabilities: Set<AiCapability> // STREAMING, REASONING, JSON_MODE, VISION
    
    suspend fun generateText(prompt: String, config: AiConfiguration): Result<String>
    fun streamText(prompt: String, config: AiConfiguration): Flow<String>
    suspend fun chat(messages: List<AiMessage>, config: AiConfiguration): Result<String>
    suspend fun validateCredentials(apiKey: String, baseUrl: String?): Boolean
}

enum class ProviderId { GEMINI, OPENAI, GROQ, OPENROUTER, OLLAMA }

enum class AiCapability { STREAMING, REASONING, JSON_MODE, VISION }

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

### Implementation Sub-Systems

1.  **Provider Registry & Factory:** `AiProviderRegistry` holds a thread-safe map `Map<ProviderId, @JvmSuppressWildcards AiProvider>`. `AiProviderFactory` resolves the requested provider dynamically based on user preferences.
2.  **Provider Manager (`AiProviderManager`):** Acts as the primary facade for repositories. Handles provider selection, fallback execution, retry logic, cost tracking, and telemetry logging.
3.  **Model Registry:** Maintains available models per provider (e.g., Gemini: `gemini-1.5-flash`, `gemini-1.5-pro`; OpenAI: `gpt-4o-mini`, `gpt-4o`; Groq: `llama-3.3-70b-versatile`; Ollama: `llama3`, `mistral`).
4.  **Conversation Manager:** Manages multi-turn chat history for `:feature:interview`, automatically trimming context when token limits exceed `maxTokens`.
5.  **Prompt Manager:** Centralized repository of parameterized prompt templates (`ResumePrompts`, `CoverLetterPrompts`, `InterviewPrompts`), separating raw strings from feature code.
6.  **Health Monitor & Failover:** Periodically pings active provider endpoint. If the primary provider returns consecutive HTTP 5xx or connection timeouts, `AiProviderManager` automatically routes requests to a configured fallback provider (e.g., Gemini -> Groq).
7.  **Credential Manager:** Interacts with `EncryptedDataStore` to securely read/write API keys without exposing secrets in memory logs.
8.  **Streaming Engine:** Wraps SSE (Server-Sent Events) or OkHttp WebSocket streams into Kotlin `Flow<String>`, emitting chunks directly to Compose UI.
9.  **Cost Tracking & Telemetry:** Calculates estimated cost based on input/output token counts per model and logs usage metrics to local Room database for user display in Settings.

---

### Testing & Migration Plan

*   **Testing Strategy:** Create `FakeAiProvider` for unit testing repository layers. Write contract tests for `GeminiAiProvider`, `OpenAiProvider`, and `GroqAiProvider` using MockWebServer to verify HTTP header handling, streaming SSE parsing, and error code mapping.
*   **Migration Plan:** Deprecate `DelegatingAiService.kt`. Re-route all calls in `ResumeRepositoryImpl`, `CoverLetterRepositoryImpl`, and `InterviewRepositoryImpl` to `AiProviderManager`.

---

## SECTION 6: JOB PLATFORM IMPLEMENTATION PLAN

### Scraper Engine Architecture

The Job Platform transitions the application from fake static lists to a real-world job discovery engine powered by Apify actors, direct REST web clients, data normalization, deduplication, and Room caching.

```
+-----------------------------------------------------------------------------------+
| JOB PLATFORM PIPELINE                                                             |
+-----------------------------------------------------------------------------------+
| [User Query] --> [Search Pipeline] --> [Apify Scraper Engine / REST Client]        |
|                                                |                                  |
|                                                v                                  |
| [Room Entity Cache] <-- [Deduplication & Rank] <-- [Normalization Engine]          |
|         |                                                                         |
|         v                                                                         |
| [JobsScreen UI]                                                                   |
+-----------------------------------------------------------------------------------+
```

---

### Technical Specification & Interface Contracts

```kotlin
// Core Contract in :core:network
interface JobProvider {
    val providerId: String
    suspend fun searchJobs(query: JobSearchQuery): Result<List<JobListing>>
    suspend fun fetchJobDetails(jobId: String): Result<JobListing>
}

data class JobSearchQuery(
    val keywords: String,
    val location: String,
    val isRemoteOnly: Boolean = false,
    val page: Int = 1,
    val limit: Int = 20
)

data class JobListing(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val salaryRange: String?,
    val description: String,
    val url: String,
    val isRemote: Boolean,
    val postedDateMillis: Long,
    val providerSource: String
)
```

---

### Pipeline Components & Processing Phases

1.  **Apify Actor Integration:** Implement `ApifyJobProvider` calling Apify REST API v2 (`https://api.apify.com/v2/`). Executes designated actor tasks (e.g., LinkedIn Job Scraper `hMSc3Iq0A2R`, Indeed Scraper `mis3x~indeed`), polls run status, and downloads dataset JSON.
2.  **Actor Registry (`ApifyActorRegistry`):** Maps target job search queries to specific optimal Apify actors based on geography and industry filters.
3.  **Normalization Engine:** Unifies disparate JSON response schemas from different scrapers (LinkedIn, Indeed, Glassdoor) into canonical `JobListing` domain models.
4.  **Deduplication & Merging:** Applies MD5/SHA-256 hash matching on composite key (`normalizedTitle + company + location`) to eliminate duplicate listings scraped across multiple sources.
5.  **Ranking & Relevance Scoring:** Ranks job listings based on query keyword density, recency, and remote work user preferences.
6.  **Caching & Offline Sync:** Writes normalized listings to Room `JobListingEntity` table with a configurable TTL (Time-To-Live, default 24 hours). Offline queries serve cached database listings seamlessly.
7.  **Background Sync Engine:** Registers `JobSyncWorker` with WorkManager to execute periodic background scraping runs matching user profile career keywords, emitting notifications when high-match roles are discovered.

---

### Testing & Migration Plan

*   **Testing Strategy:** Mock Apify execution runs using MockWebServer. Verify dataset schema transformations, hash deduplication algorithms, and Room TTL cache purge routines.
*   **Migration Plan:** Remove `JobSearchRepositoryImpl.kt` static list; wire `:feature:jobs` ViewModel directly to `JobSearchRepositoryImpl` backed by `JobProviderManager`.

---

## SECTION 7: SETTINGS MODULE IMPLEMENTATION PLAN

A dedicated `:feature:settings` Gradle module must be introduced to centralize application configurations across 12 distinct sub-pages.

---

### Settings Sub-Pages Technical Specification

```
+-----------------------------------------------------------------------------------+
| :feature:settings MODULE LAYOUT                                                   |
+-----------------------------------------------------------------------------------+
| 1. General         2. Appearance       3. Notifications    4. AI Providers        |
| 5. Job Providers   6. Storage          7. Privacy          8. Security            |
| 9. Developer       10. About           11. Backup          12. Restore            |
+-----------------------------------------------------------------------------------+
```

#### Page 1: General Settings
*   **Purpose:** App language, default start destination, and general user preferences.
*   **State:** `GeneralSettingsUiState(language: String, startDestination: String, autoSave: Boolean)`
*   **ViewModel / Repository:** `GeneralSettingsViewModel` -> `SettingsRepositoryImpl`
*   **DataStore Keys:** `pref_language`, `pref_start_dest`, `pref_auto_save`
*   **Validation:** Language code must match supported ISO list (`en`, `es`, `fr`, `de`).
*   **Acceptance Criteria:** Changing start destination updates app launch navigation route.

#### Page 2: Appearance Settings
*   **Purpose:** Theme selection (System, Light, Dark) and Dynamic Color (Material You) controls.
*   **State:** `AppearanceSettingsUiState(themeMode: ThemeMode, dynamicColorEnabled: Boolean)`
*   **ViewModel / Repository:** `AppearanceSettingsViewModel` -> `SettingsRepositoryImpl`
*   **DataStore Keys:** `pref_theme_mode`, `pref_dynamic_color`
*   **Validation:** None.
*   **Acceptance Criteria:** Dynamic color toggling immediately redraws `AivanceTheme` UI palette.

#### Page 3: Notifications Settings
*   **Purpose:** Configure job alert notifications, follow-up application reminders, and sound/vibration alerts.
*   **State:** `NotificationSettingsUiState(jobAlertsEnabled: Boolean, followUpRemindersEnabled: Boolean, quietHoursEnabled: Boolean)`
*   **ViewModel / Repository:** `NotificationSettingsViewModel` -> `SettingsRepositoryImpl`
*   **DataStore Keys:** `pref_job_alerts`, `pref_followup_reminders`, `pref_quiet_hours`
*   **Validation:** Requires Android 13+ `POST_NOTIFICATIONS` runtime permission check.
*   **Acceptance Criteria:** Disabling follow-up reminders cancels scheduled WorkManager jobs.

#### Page 4: AI Providers Settings
*   **Purpose:** Configure AI LLM providers (Gemini, OpenAI, Groq, OpenRouter, Ollama), manage API keys, select models, adjust temperature sliders, and test connections.
*   **State:** `AiSettingsUiState(selectedProvider: ProviderId, apiKey: String, selectedModel: String, temperature: Float, connectionStatus: ConnectionStatus)`
*   **ViewModel / Repository:** `AiSettingsViewModel` -> `SettingsRepositoryImpl` -> `EncryptedDataStore`
*   **DataStore Keys:** `encrypted_ai_provider`, `encrypted_api_key_[provider]`, `pref_ai_temp`
*   **Validation:** API key non-empty regex validation; "Test Connection" button calls `AiProvider.validateCredentials()`.
*   **Acceptance Criteria:** Successful credential test shows green checkmark; API key saved in EncryptedDataStore.

#### Page 5: Job Providers Settings
*   **Purpose:** Configure Apify scraper token, select active scraper actors (LinkedIn, Indeed, Glassdoor), and set sync intervals.
*   **State:** `JobSettingsUiState(apifyToken: String, activeActors: Set<String>, syncIntervalHours: Int, cacheRetentionDays: Int)`
*   **ViewModel / Repository:** `JobSettingsViewModel` -> `SettingsRepositoryImpl` -> `EncryptedDataStore`
*   **DataStore Keys:** `encrypted_apify_token`, `pref_active_actors`, `pref_sync_interval`
*   **Validation:** Apify token syntax check (`apify_api_...`).
*   **Acceptance Criteria:** Updating sync interval reschedules periodic `JobSyncWorker` in WorkManager.

#### Page 6: Storage Settings
*   **Purpose:** View local database storage usage, clear job listing cache, and manage stored resume PDF files.
*   **State:** `StorageSettingsUiState(dbSizeBytes: Long, cacheSizeBytes: Long, pdfCount: Int)`
*   **ViewModel / Repository:** `StorageSettingsViewModel` -> `SettingsRepositoryImpl`
*   **DataStore Keys:** None (Direct File/Room query).
*   **Validation:** Confirmation dialog prior to clearing cache or deleting files.
*   **Acceptance Criteria:** "Clear Cache" button purges `JobListingEntity` table and recalculates storage metrics.

#### Page 7: Privacy Settings
*   **Purpose:** User telemetry consent toggles, local-only AI data processing mode, and data usage transparency disclosures.
*   **State:** `PrivacySettingsUiState(analyticsEnabled: Boolean, localProcessingOnly: Boolean)`
*   **ViewModel / Repository:** `PrivacySettingsViewModel` -> `SettingsRepositoryImpl`
*   **DataStore Keys:** `pref_analytics_enabled`, `pref_local_only`
*   **Validation:** None.
*   **Acceptance Criteria:** Toggling analytics off immediately stops telemetry event logging.

#### Page 8: Security Settings
*   **Purpose:** Configure Encrypted Storage, Biometric App Lock (Fingerprint/Face), and key rotation policies.
*   **State:** `SecuritySettingsUiState(biometricLockEnabled: Boolean, storageEncrypted: Boolean)`
*   **ViewModel / Repository:** `SecuritySettingsViewModel` -> `SettingsRepositoryImpl`
*   **DataStore Keys:** `pref_biometric_enabled`, `pref_storage_encrypted`
*   **Validation:** Device must support `BiometricManager.BIOMETRIC_SUCCESS`.
*   **Acceptance Criteria:** Enabling biometric lock prompts for fingerprint authentication upon launching app.

#### Page 9: Developer Settings
*   **Purpose:** Debug logging toggles, mock data reset, AI response latency simulator, and raw database query runner.
*   **State:** `DeveloperSettingsUiState(verboseLogging: Boolean, useMockServices: Boolean, simulatedLatencyMs: Long)`
*   **ViewModel / Repository:** `DeveloperSettingsViewModel` -> `SettingsRepositoryImpl`
*   **DataStore Keys:** `pref_verbose_logging`, `pref_use_mocks`, `pref_sim_latency`
*   **Validation:** Hidden behind 7-tap build version gesture in About screen.
*   **Acceptance Criteria:** Enabling mock services routes network layer to `MockAiProvider` and `MockJobProvider`.

#### Page 10: About Settings
*   **Purpose:** Display app version, build number, commit hash, open-source licenses, and terms of service.
*   **State:** `AboutSettingsUiState(versionName: String, versionCode: Long, licenses: List<License>)`
*   **ViewModel / Repository:** `AboutSettingsViewModel` -> `SettingsRepositoryImpl`
*   **DataStore Keys:** None.
*   **Validation:** None.
*   **Acceptance Criteria:** Tapping build version 7 times unlocks Developer Settings menu item.

#### Page 11: Backup Settings
*   **Purpose:** Export user database, tracked job applications, cover letters, and settings into an encrypted `.aivance` backup archive.
*   **State:** `BackupSettingsUiState(lastBackupTimestamp: Long?, isExporting: Boolean, exportPath: String?)`
*   **ViewModel / Repository:** `BackupSettingsViewModel` -> `SettingsRepositoryImpl`
*   **DataStore Keys:** `pref_last_backup_time`
*   **Validation:** Export requires user password or auto-generated KeyStore key encryption.
*   **Acceptance Criteria:** Backup action exports valid encrypted ZIP file to user-selected Uri destination via SAF (Storage Access Framework).

#### Page 12: Restore Settings
*   **Purpose:** Import and validate `.aivance` backup files, restoring user database state safely.
*   **State:** `RestoreSettingsUiState(isImporting: Boolean, restoreSuccess: Boolean?, errorMessage: String?)`
*   **ViewModel / Repository:** `RestoreSettingsViewModel` -> `SettingsRepositoryImpl`
*   **DataStore Keys:** None.
*   **Validation:** Validates header magic bytes, schema version compatibility, and archive checksum before overwriting database.
*   **Acceptance Criteria:** Restoring a valid backup restores all user applications, ATS scores, and roadmaps without corruption.

---

## SECTION 8: DATABASE MODERNIZATION

### Database Schema Modernization & Indexes

To prevent full table scans and optimize query execution on large datasets, the following `@Index` annotations must be declared across Room entities:

```kotlin
// Index Specification in :core:database
@Entity(
    tableName = "applications",
    indices = [
        Index(value = ["status"]),
        Index(value = ["dateApplied"]),
        Index(value = ["company", "role"])
    ]
)
data class ApplicationEntity(...)

@Entity(
    tableName = "ats_results",
    indices = [Index(value = ["date"]), Index(value = ["score"])]
)
data class AtsResultEntity(...)

@Entity(
    tableName = "roadmap_steps",
    indices = [Index(value = ["roadmapId"])],
    foreignKeys = [
        ForeignKey(
            entity = RoadmapEntity::class,
            parentColumns = ["id"],
            childColumns = ["roadmapId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RoadmapStepEntity(...)
```

---

### Migration Implementation & Export Strategy

1.  **Schema Export Configuration:** Enable `exportSchema = true` in `AivanceDatabase` and configure `ksp { arg("room.schemaLocation", "$projectDir/schemas") }` in `core/database/build.gradle.kts`. Commit generated schema JSONs to VCS.
2.  **Explicit Room Migration Definitions:** Replace unhandled version jumps with strict `Migration` paths:

```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_applications_status` ON `applications` (`status`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ats_results_score` ON `ats_results` (`score`)")
        
        // Add follow-up notification columns
        db.execSQL("ALTER TABLE `applications` ADD COLUMN `followUpDate` INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE `applications` ADD COLUMN `interviewDate` INTEGER DEFAULT NULL")
    }
}
```

3.  **Transaction Safety:** Annotate multi-dao operations in repositories or DAOs with `@Transaction`.
4.  **Backup & Restore Engine:** Implement atomic file copy using `SupportSQLiteDatabase.close()` to ensure database integrity during backup generation.
5.  **Testing Strategy:** Implement `MigrationTestRule` in `:core:database` `androidTest` to verify schema upgrades from version 1 through 5 without data loss.

---

## SECTION 9: NETWORK MODERNIZATION

### Stack Modernization Specifications

The network layer in `:core:network` must be modernized to provide robust connection pooling, automatic retries, logging, TLS enforcement, and certificate pinning.

```
+-----------------------------------------------------------------------------------+
| NETWORK STACK ARCHITECTURE                                                        |
+-----------------------------------------------------------------------------------+
| [Retrofit Service] --> [OkHttp Client]                                            |
|                             |-- [AuthInterceptor] (API Key Injection)             |
|                             |-- [RetryInterceptor] (Exponential Backoff + Jitter) |
|                             |-- [CacheInterceptor] (HTTP Cache Management)        |
|                             |-- [LoggingInterceptor] (BODY in debug, NONE in prod)|
|                             |-- [ConnectionSpec.RESTRICTED_TLS] (TLS 1.3/1.2)    |
|                             |-- [CertificatePinner] (Pinned API domains)          |
+-----------------------------------------------------------------------------------+
```

---

### Implementation Details & Interceptor Configuration

1.  **OkHttp Client Configuration:**
    *   *Connect Timeout:* 15 Seconds
    *   *Read Timeout:* 60 Seconds (to support long AI streaming responses)
    *   *Write Timeout:* 30 Seconds
    *   *Connection Pool:* 5 idle connections, 5-minute keep-alive duration
2.  **Retry Interceptor (`RetryInterceptor`):** Automatically retries failed requests on HTTP status codes 429 (Too Many Requests), 500, 502, 503, and 504 using exponential backoff (`delay = initialDelay * (2 ^ attempt) + jitter`) up to 3 max attempts.
3.  **Auth Interceptor (`AuthInterceptor`):** Dynamically injects authorization headers (`Authorization: Bearer <token>` or `x-goog-api-key: <key>`) fetched from `EncryptedDataStore`.
4.  **TLS Enforcement & Certificate Pinning:**
    *   Enforce `ConnectionSpec.RESTRICTED_TLS` restricting ciphers to TLS 1.3 and TLS 1.2.
    *   Configure `CertificatePinner` for production endpoints (`api.apify.com`, `generativelanguage.googleapis.com`, `api.openai.com`).
5.  **Error Handling Facade:** Map OkHttp/Retrofit exceptions (`SocketTimeoutException`, `UnknownHostException`, `HttpException`) into typed `Result.Error(NetworkException)` domain objects.

---

## SECTION 10: SECURITY HARDENING

### Security Risk Mitigation Plan

| Risk Domain | Vulnerability Identified in Audit | Modernization & Hardening Remedy | Target Security Standard |
| :--- | :--- | :--- | :--- |
| **API Keys Storage** | Plain-text storage in DataStore JSON files (`UserPreferencesSerializer`). | Encrypt all preference files using Android Keystore Tink AEAD (`MasterKey.DEFAULT_AES256_GCM_SPEC`). | AES-256-GCM hardware-backed encryption. |
| **Code Leakage** | Fallback key strings embedded in code or `local.properties`. | Remove hardcoded strings; inject dev keys via `BuildConfig` sourced strictly from environment variables; obfuscate via ProGuard/R8. | Zero hardcoded fallback secrets in app binary. |
| **Cleartext Traffic** | Missing custom `network_security_config.xml`. | Add `res/xml/network_security_config.xml` enforcing `cleartextTrafficPermitted="false"` across all domains. | HTTPS/TLS 1.2+ mandatory for 100% of calls. |
| **Permissions** | Missing Android 13 notification permission setup. | Declare `POST_NOTIFICATIONS` in `AndroidManifest.xml` and request runtime permission prior to WorkManager dispatch. | Principle of least privilege compliance. |
| **Play Integrity** | No application binary or environment verification. | Integrate Play Integrity API to detect tampered app builds or dangerous emulators before processing API keys. | Verified genuine app binary execution. |
| **Backup Policy** | Default backup policy risks exporting API keys to Android Cloud Backup. | Configure `res/xml/backup_rules.xml` excluding encrypted DataStore files and internal databases from cloud backup. | Zero cloud exposure of encrypted credential files. |

---

## SECTION 11: PERFORMANCE OPTIMIZATION

### Jetpack Compose Stability & Recomposition Optimization

1.  **State Class Annotations:** Annotate all UI State data classes with `@Immutable` or `@Stable` to prevent Compose compiler from marking composables as unstable due to `List<T>` parameters.
2.  **Immutable Collections:** Replace raw Kotlin `List<T>` parameters in UI state data classes with `ImmutableList<T>` from `kotlinx.collections.immutable`.
3.  **Derived State:** Wrap scroll position calculations and filtered item views in `remember { derivedStateOf { ... } }`.
4.  **Lazy Layout Keys:** Enforce explicit `key = { item.id }` parameters across all `LazyColumn` and `LazyRow` items to optimize list re-ordering and item reuse.

---

### Startup & Baseline Profiles Integration

1.  **Baseline Profiles Module (`:baselineprofile`):** Create Macrobenchmark module using `BaselineProfileRule`. Capture critical user journeys:
    *   App Cold Launch (`MainActivity` creation)
    *   Navigation tab switching (Dashboard -> Jobs -> Resume -> Tracker -> Settings)
    *   PDF Upload and Resume result rendering
2.  **Startup Initialization:** Defer non-critical Hilt service initializations out of `AivanceApp.onCreate()` using AndroidX App Startup library.

---

### Resource & Memory Management

1.  **PDF Bitmap Management:** Downsample PDF page preview bitmaps rendered during document viewing; call `Bitmap.recycle()` explicitly when disposing preview Composables.
2.  **Memory Leak Prevention:** Enforce weak references or lifecycle-aware Coroutine scopes (`viewModelScope`, `lifecycleScope`) across all asynchronous operations.
3.  **WorkManager Battery Constraints:** Enforce `NetworkType.UNMETERED` and `RequiresBatteryNotLow` constraints on background job scraping workers.

---

## SECTION 12: TESTING STRATEGY

### Comprehensive Testing Architecture

To elevate test coverage from < 25% to > 80%, a multi-layered testing strategy is established.

```
+-----------------------------------------------------------------------------------+
| TESTING PYRAMID                                                                   |
+-----------------------------------------------------------------------------------+
|               [ E2E / UI Tests ]          --> Compose UI & Navigation Tests       |
|            [ Integration Tests ]          --> Repository + Room + MockWebServer    |
|         [ Unit & DAO Test Suites ]        --> ViewModels, UseCases, DAOs, Migrations|
+-----------------------------------------------------------------------------------+
```

---

### Test Suite Execution Plan

1.  **Unit Tests (ViewModels & Use Cases):** Use `kotlinx.coroutines.test.TestDispatcher`, `Turbine` for StateFlow assertions, and `MockK` for mocking repositories. Write test suites for all 8 existing ViewModels and the new `SettingsViewModel`.
2.  **DAO Tests:** Expand in-memory Room database tests (`@RunWith(AndroidJUnit4::class)`) for `ApplicationDao`, `AtsDao`, `CoverLetterDao`, `RoadmapDao`, and new `JobListingDao`.
3.  **Migration Tests:** Implement `MigrationTestRule` verifying incremental schema migrations (v1 -> v2 -> v3 -> v4 -> v5).
4.  **Compose UI Tests:** Test component state rendering in `:core:designsystem` and feature screens using `createComposeRule()`.
5.  **Navigation Tests:** Expand `AivanceNavGraphTest` verifying deep link routing and top-level tab backstack clearing.
6.  **Integration Tests:** Test end-to-end data flow from ViewModel through Repository to local Room cache and MockWebServer remote network responses.
7.  **Continuous Integration (CI):** Configure GitHub Actions workflow running `./gradlew testDebugUnitTest` and connected Android emulator tests on every Pull Request.

*   **Coverage Targets:**
    *   Core Modules (`:core:*`): > 90%
    *   Feature Modules (`:feature:*`): > 85%
    *   Overall Repository Coverage: > 80%

---

## SECTION 13: RELEASE ROADMAP

The release roadmap is structured into 8 sequential milestones, guaranteeing controlled risk reduction and steady progression toward production release.

```
+-----------------------------------------------------------------------------------+
| MASTER RELEASE ROADMAP                                                            |
+-----------------------------------------------------------------------------------+
| M1: Critical Fixes    (P0 Bugs, PDFBox, Encrypted Keys, Room Migrations)          |
| M2: Architecture      (Pluggable AI & Job Interfaces, Settings Infra, Nav Clean)  |
| M3: AI Platform       (Multi-LLM Providers, Streaming, Prompt Templates, Costs)   |
| M4: Job Platform      (Apify Integration, Normalization, Deduplication, Cache)    |
| M5: Settings Module   (12 Settings Sub-Pages, DataStore Wiring, Backup/Restore)   |
| M6: Testing Expansion (Unit, DAO, Migration, Compose UI, CI Automation)          |
| M7: Performance       (Baseline Profiles, Compose Stability, Cold Start Tuning)  |
| M8: Production Launch (Security Hardening, ProGuard, Play Integrity, Store Launch)|
+-----------------------------------------------------------------------------------+
```

---

### Milestone Breakdown Details

#### Milestone 1: Critical Production Blockers
*   **Objectives:** Fix `PdfTextExtractor` API 35 crash on API 26–34; implement Encrypted DataStore for API keys; configure Room schema export and safe migration rules.
*   **Files Affected:** `PdfTextExtractor.kt`, `DataStoreModule.kt`, `UserPreferencesSerializer.kt`, `AivanceDatabase.kt`, `DatabaseModule.kt`.
*   **Dependencies:** `libs.pdfbox.android`, Android Keystore / Tink.
*   **Risks:** DataStore migration corruption on legacy installs.
*   **Success Criteria:** 0 crashes during PDF upload on API 26–34 emulators/devices; API keys stored encrypted; DB schema export enabled.
*   **Estimated Duration:** 2 Days

#### Milestone 2: Architecture Modernization & Refactoring
*   **Objectives:** Implement `AiProvider` and `JobProvider` core interfaces; create Hilt provider registries; build `:feature:settings` module shell; encapsulate navigation contracts.
*   **Files Affected:** `:core:network`, `:core:datastore`, `:navigation`, new `:feature:settings`.
*   **Dependencies:** Hilt DI.
*   **Risks:** Temporary compilation breakage during service interface refactoring.
*   **Success Criteria:** Zero direct network class instantiations; Hilt cleanly injects provider registries; settings module compiles.
*   **Estimated Duration:** 4 Days

#### Milestone 3: AI Platform Implementation
*   **Objectives:** Implement `GeminiAiProvider`, `OpenAiProvider`, `GroqAiProvider`, `OllamaAiProvider`; build streaming text engine; implement conversation manager and prompt templates; wire real AI feedback parsing in Interview feature.
*   **Files Affected:** `:core:network`, `:feature:resume`, `:feature:coverletter`, `:feature:interview`.
*   **Dependencies:** Milestone 1 & 2.
*   **Risks:** API rate limiting or vendor format drift.
*   **Success Criteria:** Runtime LLM provider switching works seamlessly; interview feedback parses structured JSON; text streaming functions in Compose.
*   **Estimated Duration:** 5 Days

#### Milestone 4: Job Search Platform Implementation
*   **Objectives:** Build `ApifyJobProvider`, normalization engine, hash deduplication algorithm, ranking engine, and Room `JobListingEntity` cache; replace hardcoded mock job list.
*   **Files Affected:** `:feature:jobs`, `:core:database`, `:core:network`.
*   **Dependencies:** Apify API Token, Milestone 2.
*   **Risks:** Scraper actor output schema changes.
*   **Success Criteria:** Job search fetches live web listings via Apify; duplicate listings are eliminated; offline search serves Room cache.
*   **Estimated Duration:** 5 Days

#### Milestone 5: Settings Feature Complete Build
*   **Objectives:** Implement all 12 Settings sub-pages (`General`, `Appearance`, `Notifications`, `AI Providers`, `Job Providers`, `Storage`, `Privacy`, `Security`, `Developer`, `About`, `Backup`, `Restore`).
*   **Files Affected:** `:feature:settings`, `:navigation`.
*   **Dependencies:** Milestone 3 & 4.
*   **Risks:** Complex UI state management across 12 sub-pages.
*   **Success Criteria:** User can configure all app parameters; backup exports encrypted ZIP archive; restore imports database successfully.
*   **Estimated Duration:** 5 Days

#### Milestone 6: Testing & Quality Assurance
*   **Objectives:** Expand unit tests, DAO tests, migration tests, Compose UI tests, and integration tests to achieve > 80% coverage; set up GitHub Actions CI.
*   **Files Affected:** All test directories across all 16 modules.
*   **Dependencies:** Milestone 1 through 5.
*   **Risks:** Flaky UI tests on CI emulators.
*   **Success Criteria:** Total codebase coverage > 80%; core module coverage > 90%; CI pipeline passes cleanly on all PRs.
*   **Estimated Duration:** 4 Days

#### Milestone 7: Performance Optimization & Polish
*   **Objectives:** Add `:baselineprofile` module; optimize Compose list stability (`@Immutable`); optimize cold start times; eliminate frame drops.
*   **Files Affected:** `:baselineprofile`, all UI Composables, domain state classes.
*   **Dependencies:** Milestone 6.
*   **Risks:** Benchmark test device variability.
*   **Success Criteria:** Cold startup < 1.2s; zero jank frames during list scrolling; memory footprint < 150MB peak.
*   **Estimated Duration:** 3 Days

#### Milestone 8: Production Hardening & Launch
*   **Objectives:** Enable ProGuard/R8 code obfuscation; enforce Network Security Config; integrate Play Integrity API; run final security audit; produce release AAB.
*   **Files Affected:** `proguard-rules.pro`, `AndroidManifest.xml`, `network_security_config.xml`, `build.gradle.kts`.
*   **Dependencies:** Milestone 1 through 7.
*   **Risks:** Over-aggressive ProGuard rules breaking serialization.
*   **Success Criteria:** Approved Google Play Store release build; zero open P0/P1 bugs; 100% production readiness checklist pass.
*   **Estimated Duration:** 3 Days

---

## SECTION 14: ENGINEERING BACKLOG

The engineering backlog compiles all executable tasks required to achieve production release.

```
+-------------------------------------------------------------------------------------------------------------------------------+
| ENGINEERING BACKLOG                                                                                                           |
+------+----------+--------------------------------------+---------------------------------+-----------------+------------------+
| ID   | Priority | Title                                | Affected Modules                | Effort (Hours)  | Status           |
+------+----------+--------------------------------------+---------------------------------+-----------------+------------------+
| CORE-01 | P0   | PDF Text Extractor API 35 Fix        | :core:util                      | 3               | Ready for Dev    |
| CORE-02 | P0   | Encrypted DataStore Implementation   | :core:datastore                 | 5               | Ready for Dev    |
| DB-01   | P0   | Room Schema Export & Migration Rules | :core:database                  | 4               | Ready for Dev    |
| AI-01   | P1   | Interview AI Feedback JSON Parser    | :feature:interview              | 4               | Ready for Dev    |
| JOB-01  | P1   | Live Apify Scraper Engine & API      | :feature:jobs                   | 12              | Ready for Dev    |
| NET-01  | P1   | Network Stack OkHttp Interceptors    | :core:network                   | 6               | Ready for Dev    |
| AI-02   | P2   | Pluggable AI Provider Framework      | :core:network, :core:datastore  | 14              | Ready for Dev    |
| JOB-02  | P2   | Job Scraper Pipeline & Room Cache    | :core:database, :feature:jobs   | 16              | Ready for Dev    |
| SET-01  | P2   | Create :feature:settings Module Shell| :feature:settings (New)         | 6               | Ready for Dev    |
| SET-02  | P2   | Build 12 Settings Sub-Pages UI       | :feature:settings               | 16              | Ready for Dev    |
| NAV-01  | P2   | Encapsulate Navigation 3 Contracts   | :navigation                     | 8               | Ready for Dev    |
| RES-01  | P3   | Cover Letter PDF Export Capability   | :feature:coverletter            | 6               | Ready for Dev    |
| UI-01   | P3   | Design System Accessibility & Target | :core:designsystem              | 5               | Ready for Dev    |
| DASH-01 | P3   | Dashboard Pull-To-Refresh Support    | :feature:dashboard              | 3               | Ready for Dev    |
| PERF-01 | P4   | Add Baseline Profiles Module         | :baselineprofile (New)          | 8               | Ready for Dev    |
| PERF-02 | P4   | Compose State Stability Optimization | All :feature:* modules          | 6               | Ready for Dev    |
| TEST-01 | P1   | Feature ViewModel Test Suites        | All :feature:* test dirs        | 16              | Ready for Dev    |
| TEST-02 | P1   | Room DAO & Migration Test Suites     | :core:database                  | 8               | Ready for Dev    |
| SEC-01  | P1   | Network Security Config & TLS        | :app                            | 4               | Ready for Dev    |
| SEC-02  | P2   | Play Integrity API Integration       | :app                            | 8               | Ready for Dev    |
+------+----------+--------------------------------------+---------------------------------+-----------------+------------------+
```

---

## SECTION 15: PRODUCTION READINESS CHECKLIST

Before launching Aviance to the Google Play Store, all items in this checklist must be verified and signed off.

### Architecture & Design
*   [ ] Multi-module dependencies strictly follow directional graph without circular imports.
*   [ ] Unidirectional Data Flow (UDF) enforced across 100% of UI screens.
*   [ ] Zero direct class instantiations of core services; 100% dependency injection via Hilt.
*   [ ] Pluggable provider pattern active for AI and Job platforms.

### Features & Usability
*   [ ] Resume PDF upload and text analysis functional on API 26 through 35.
*   [ ] ATS History supports query filtering and swipe-to-delete with undo.
*   [ ] Cover Letter supports custom tone selection, streaming, and PDF export.
*   [ ] Interview mock practice parses real AI JSON feedback with score summary.
*   [ ] Job Search queries real listings via Apify scraper with local Room caching.
*   [ ] Application Tracker dispatches follow-up reminders via WorkManager.
*   [ ] Career Roadmap pre-populates dynamic milestones based on target role.
*   [ ] Settings feature fully operational across all 12 sub-pages.

### Security & Privacy
*   [ ] All API keys stored in Encrypted DataStore using Android Keystore Tink AEAD.
*   [ ] Hardcoded fallback secret strings sanitized from VCS and app binary.
*   [ ] `network_security_config.xml` enforces HTTPS cleartext traffic blocking.
*   [ ] ProGuard/R8 obfuscation enabled and verified on release build.
*   [ ] Cloud backup rules exclude internal databases and encrypted preference files.

### Performance & Offline
*   [ ] Cold startup time under 1.2 seconds measured via Macrobenchmark.
*   [ ] 60 FPS Compose rendering achieved without jank during list scrolling.
*   [ ] `:baselineprofile` pre-compilation rule included in release build.
*   [ ] Full offline readability supported for past ATS scans, cover letters, and jobs.

### Quality Assurance & Testing
*   [ ] Total codebase unit test coverage exceeds 80%.
*   [ ] Core module test coverage exceeds 90%.
*   [ ] Room schema migrations (v1 -> v5) verified via automated test rule.
*   [ ] CI pipeline executing clean test runs on every Pull Request.

---

## SECTION 16: SUCCESS METRICS & KPIS

To objectively measure the success of this engineering plan upon execution, the following target Key Performance Indicators (KPIs) are defined:

```
+-----------------------------------------------------------------------------------+
| TARGET SUCCESS METRICS & KPIS                                                     |
+----------------------------------+-----------------------+------------------------+
| Metric                           | Baseline / Current    | Target Production KPI  |
+----------------------------------+-----------------------+------------------------+
| Crash-Free User Sessions         | < 85% (API 26-34 bug) | > 99.9%                |
| App Cold Startup Time            | ~2.4s                 | < 1.2s                 |
| Frame Rendering Jank Rate        | ~8.5% jank frames     | < 1.0% (60 FPS smooth) |
| Total Unit Test Coverage         | < 25%                 | > 80%                  |
| Core Module Test Coverage        | ~30%                  | > 90%                  |
| Resume AI Analysis Latency       | ~6.5s (Block string)  | < 2.5s (First Token)   |
| Job Search Response Time         | Mock (0ms) / Slow     | < 1.5s (Cached Room)   |
| Peak Heap Memory Consumption     | ~220MB                | < 150MB                |
| Battery Drain (30-min session)   | ~3.2%                 | < 1.0%                 |
| AI Provider Failover Recovery    | 0% (Immediate Crash)  | 100% Automatic Swap    |
+----------------------------------+-----------------------+------------------------+
```

---

*End of Master Engineering Plan for Aviance Android Application.*
