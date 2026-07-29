# AVIANCE - MASTER ARCHITECTURE SPECIFICATION

**Document Type:** Permanent Architecture Specification & System Design Handbook  
**Target Repository:** Aviance (Android Application)  
**Package Root:** `com.bangersoul.aivance`  
**Authors:** Chief Software Architect, Distinguished Android Engineer, Principal Systems Designer, Principal DevOps Engineer, Principal QA Architect, Principal Security Engineer, Staff Product Architect, Technical Program Director  
**Status:** Approved Architectural Standard / Active System Design Baseline  
**Related References:** `Audit.md` (System Baseline & Deficiencies), `EngineeringPlan.md` (Implementation Roadmap)  

---

## LEGEND & TAXONOMY OF STATEMENTS
To ensure complete clarity and traceability, architectural statements throughout this document are explicitly classified as follows:
* **[VERIFIED BASELINE]:** Architectural patterns, module configurations, or interfaces directly confirmed in the existing repository codebase.
* **[ARCHITECTURAL DECISION]:** Approved non-negotiable structural conventions, design patterns, and systemic constraints established for production execution.
* **[PROPOSED ARCHITECTURE]:** Future target architectures, interfaces, and module designs specified to resolve current deficiencies and scale the platform.
* **[ASSUMPTION]:** Explicit operational hypotheses regarding external services or runtime environments necessary for complete system design.

---

## SECTION 1: ARCHITECTURAL VISION

### 1.1 Mission Statement
The mission of the Aviance Android Application Architecture is to establish a deterministic, offline-first, vendor-agnostic, and secure client foundation. The system provides intelligent career acceleration tools—including automated resume analysis, Applicant Tracking System (ATS) optimization, cover letter generation, interactive interview preparation, real-time job scraping, and application tracking—with maximum execution reliability, minimal latency, and zero vendor lock-in.

### 1.2 Core Architectural Principles

1. **Strict Unidirectional Data Flow (UDF) [ARCHITECTURAL DECISION]:**  
   State flows downward from ViewModels to Compose UI via immutable `StateFlow` streams. User interactions and system events flow strictly upward via explicit event lambdas or sealed interface events. Mutations occurring outside this unidirectional stream are forbidden.

2. **Decoupled Pluggable Abstractions [ARCHITECTURAL DECISION]:**  
   All heavy external capabilities—specifically AI inference engines and web job scraping infrastructure—must operate behind vendor-agnostic provider interfaces (`AiProvider` and `JobProvider`). The core business domain and UI layers must remain completely oblivious to underlying vendor implementations (e.g., Google Gemini, OpenAI, Groq, Apify).

3. **Offline-First Data Governance [ARCHITECTURAL DECISION]:**  
   Local storage (Room SQLite Database and Encrypted DataStore) serves as the single primary source of truth for user data and application state. Remote network interactions synchronize with local storage asynchronously. Rendered UI views observe local database state streams directly, guaranteeing seamless offline readability.

4. **Layered Module Isolation [ARCHITECTURAL DECISION]:**  
   Module dependencies strictly follow a Directed Acyclic Graph (DAG). Downstream feature modules are completely isolated from one another; cross-feature communication must occur exclusively via explicit navigation routes, shared domain contracts, or centralized data repositories.

5. **Defense-in-Depth Security [ARCHITECTURAL DECISION]:**  
   User API credentials, personal career records, and local storage databases must be encrypted at rest using hardware-backed key material (Android Keystore). Plain-text serialization of sensitive tokens or unencrypted network traffic is strictly prohibited.

---

### 1.3 Engineering Philosophy
The engineering design of Aviance prioritizes **Systemic Utility, Determinism, and Resilience**. Code written for Aviance must favor predictable runtime behavior, compile-time safety, explicit type hierarchies, and testability over implicit magic or clever brevity. Every system component must fail gracefully, degrade capability predictably under adverse conditions (such as network loss or rate limiting), and emit clear telemetry for observability.

---

### 1.4 Systemic Quality Goals

```
+-----------------------------------------------------------------------------------+
| AVIANCE QUALITY GOALS                                                             |
+-----------------------------------------------------------------------------------+
| Scalability:      Multi-LLM & Multi-Scraper runtime expansion without refactoring |
| Maintainability:  Isolated feature modules with strict, testable API boundaries   |
| Reliability:      Zero UI thread blocking, fault-tolerant offline data sync       |
| Security:         Hardware-backed credential encryption & HTTPS TLS pinning       |
+-----------------------------------------------------------------------------------+
```

* **Scalability Goals:** System interfaces must allow adding new AI inference providers or job scraping engines within 4 hours of engineering effort, without requiring modifications to any UI or feature layer files.
* **Maintainability Goals:** Build times must remain incremental, with modular boundaries preventing re-compilation cascades across feature boundaries. Code coverage for business domain logic must exceed 80%.
* **Reliability Goals:** Target cold startup latency < 1.2 seconds on mid-tier hardware (API 26+). Maintain 99.9% crash-free session metrics across all supported Android SDK levels (26 through 35).
* **Security Goals:** Achieve OWASP Mobile Top 10 compliance. Protect stored API keys against extraction on rooted devices via hardware-wrapped key encryption.

---

### 1.5 Offline-First Philosophy
Network connections are inherently unreliable, high-latency, and power-intensive. Aviance implements an **Offline-First Data Architecture**. Whenever a user performs an action—such as updating an application status, generating a career roadmap, or reviewing past ATS scores—the operation is immediately written to local Room persistence. Remote sync engines (WorkManager) process queue items in the background when connectivity becomes available. Reads are served 100% locally from Room SQLite, ensuring zero UI latency and full offline usability.

---

### 1.6 AI-First Philosophy
Artificial Intelligence is deeply embedded in Aviance's workflow rather than appended as a secondary utility. However, AI responses are inherently non-deterministic. To maintain software stability:
* All AI outputs must be bounded by strict structural contracts (JSON schema enforcement).
* Raw AI text must be processed by robust parsing engines with deterministic fallback mechanisms.
* AI operations must support real-time token streaming (`Flow<String>`) to eliminate perceived user latency.
* AI execution must be cost-aware, tracking token budgets and execution latency locally.

---

## SECTION 2: SYSTEM ARCHITECTURE

### 2.1 High-Level Application Architecture [PROPOSED ARCHITECTURE]

The application architecture follows standard Android modern App Architecture guidelines combined with Clean Architecture and a Pluggable Provider model across 17 Gradle modules.

```
+-----------------------------------------------------------------------------------+
|                                     :app                                          |
|                (Application Entry, Hilt Setup, WorkManager Configuration)          |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                                 :navigation                                       |
|                  (AivanceNavGraph, Route Contracts, Adaptive Suite)                |
+------------------------------------------+----------------------------------------+
                                           |
    +------------------+-------------------+--------------------+------------------+
    |                  |                   |                    |                  |
    v                  v                   v                    v                  v
:feature:dashboard  :feature:resume     :feature:ats     :feature:coverletter  :feature:interview
:feature:jobs       :feature:tracker    :feature:profile :feature:settings [NEW]
    |                  |                   |                    |                  |
    +------------------+-------------------+--------------------+------------------+
                                           |
    +------------------+-------------------+--------------------+------------------+
    |                  |                   |                    |                  |
    v                  v                   v                    v                  v
:core:designsystem  :core:network       :core:database       :core:datastore    :core:util
    |                  |                   |                    |                  |
    +------------------+-------------------+--------------------+------------------+
                                           |
                                           v
                                     :core:common
```

---

### 2.2 System Component Diagrams

#### Diagram 2.2a: Module Hierarchy & Dependency Graph [ARCHITECTURAL DECISION]

```
                                    +--------------+
                                    |     :app     |
                                    +------+-------+
                                           |
                                           v
                                    +--------------+
                                    | :navigation  |
                                    +------+-------+
                                           |
        +----------------------------------+----------------------------------+
        |                                                                     |
        v                                                                     v
  +-----------+                                                         +-----------+
  | :feature  | (dashboard, resume, ats, coverletter,                   | :feature  |
  | (Group A) |  interview, jobs, tracker, profile)                     | :settings |
  +-----+-----+                                                         +-----+-----+
        |                                                                     |
        +----------------------------------+----------------------------------+
                                           |
    +--------------------+-----------------+--------------------+--------------------+
    |                    |                 |                    |                    |
    v                    v                 v                    v                    v
+-------+        +---------------+   +-----------+        +------------+        +-----------+
| :core |        | :core:network |   |   :core   |        |   :core    |        |   :core   |
|:design|        | (Ai/Job Prov) |   | :database |        | :datastore |        |   :util   |
+-------+        +-------+-------+   +-----+-----+        +-----+------+        +-----+-----+
    |                    |                 |                    |                    |
    +--------------------+-----------------+--------------------+--------------------+
                                           |
                                           v
                                     +-----------+
                                     |   :core   |
                                     |  :common  |
                                     +-----------+
```

---

#### Diagram 2.2b: Data Flow Architecture [ARCHITECTURAL DECISION]

```
  +---------------------------------------------------------------------------------+
  | PRESENTATION LAYER (Jetpack Compose UI)                                         |
  |   - Observes UiState via StateFlow                                              |
  |   - Emits user Actions/Events upward                                            |
  +----------------------------------------+----------------------------------------+
                                           ^ |
                         UiState (Flow)    | | User Intent (Action)
                                           | v
  +----------------------------------------+----------------------------------------+
  | DOMAIN / VIEWMODEL LAYER                                                        |
  |   - Holds StateFlow<UiState>                                                   |
  |   - Executes UseCases / Repositories within viewModelScope                       |
  +----------------------------------------+----------------------------------------+
                                           ^ |
                             Domain Models | | Command Payload
                                           | v
  +----------------------------------------+----------------------------------------+
  | DATA LAYER (Repositories & Providers)                                           |
  |   - Implements Single Source of Truth (SSOT)                                    |
  |   - Synchronizes Room Local Database with Remote Providers                      |
  +-------------------+----------------------------------------+--------------------+
                      |                                        |
                      v                                        v
  +-------------------+--------------------+  +----------------+--------------------+
  | LOCAL PERSISTENCE                      |  | REMOTE DATA PROVIDERS               |
  |  - Room SQLite Database                |  |  - AiProvider (Gemini, Groq, OpenAI)|
  |  - Encrypted DataStore Preferences     |  |  - JobProvider (Apify Actors, APIs) |
  +----------------------------------------+  +-------------------------------------+
```

---

#### Diagram 2.2c: Navigation Architecture [ARCHITECTURAL DECISION]

```
+-----------------------------------------------------------------------------------+
| NAVIGATION ARCHITECTURE (Centralized Navigation 3 / Suite Scaffold)               |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
                              +-----------------------+
                              | AivanceNavGraph       |
                              | (NavigationSuite)     |
                              +-----------+-----------+
                                          |
        +---------------------------------+---------------------------------+
        |                                                                   |
        v                                                                   v
+-------------------------------+                         +-------------------------------+
| Bottom Bar Root Destinations  |                         | Detailed Push Destinations    |
|  - Destination.Dashboard      |                         |  - Route.AtsDetail(id)        |
|  - Destination.Resume         |                         |  - Route.CoverLetterDetail(id)|
|  - Destination.Jobs           |                         |  - Route.InterviewSession(id) |
|  - Destination.Tracker        |                         |  - Route.SettingsSubPage(page)|
|  - Destination.Profile        |                         +-------------------------------+
+-------------------------------+
```

---

#### Diagram 2.2d: Networking Architecture [PROPOSED ARCHITECTURE]

```
+-----------------------------------------------------------------------------------+
| NETWORK STACK ARCHITECTURE                                                        |
+-----------------------------------------------------------------------------------+

 +---------------------------------------------------------------------------------+
 | AiProvider / JobProvider Implementations                                        |
 +----------------------------------------+----------------------------------------+
                                          |
                                          v
 +---------------------------------------------------------------------------------+
 | Retrofit 2.11 / OkHttp 4.12 Network Client Engine                               |
 |   +---------------------------------------------------------------------------+ |
 |   | INTERCEPTOR CHAIN                                                         | |
 |   |  1. LoggingInterceptor (Level.BODY / Filtered Secrets)                     | |
 |   |  2. AuthenticationInterceptor (Bearer Token / Dynamic Headers)           | |
 |   |  3. RetryAndBackoffInterceptor (Exponential Backoff, HTTP 429 Recovery)   | |
 |   |  4. CircuitBreakerInterceptor (Failsafe on remote outage)                 | |
 |   +---------------------------------------------------------------------------+ |
 |   +---------------------------------------------------------------------------+ |
 |   | TLS & SECURITY CONFIGURATION                                              | |
 |   |  - CertificatePinner (Enforces SHA-256 Hashes for Google/Apify endpoints)| |
 |   |  - TLS 1.3 Minimum Protocol Enforcement                                   | |
 |   +---------------------------------------------------------------------------+ |
 +---------------------------------------------------------------------------------+
```

---

#### Diagram 2.2e: Background Workers Architecture [ARCHITECTURAL DECISION]

```
+-----------------------------------------------------------------------------------+
| BACKGROUND PROCESSING ARCHITECTURE (WorkManager + Hilt Injection)                 |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
                             +-------------------------+
                             |  WorkManager Initializer|
                             |  (Custom Configuration) |
                             +------------+------------+
                                          |
        +---------------------------------+---------------------------------+
        |                                 |                                 |
        v                                 v                                 v
+-----------------------+     +-----------------------+     +-----------------------+
| FollowUpWorker        |     | JobSyncWorker         |     | CacheCleanupWorker    |
| - Trigger: Periodic/8h|     | - Trigger: Daily/12h  |     | - Trigger: Weekly     |
| - Constraint: Network |     | - Constraint: Unmetered|     | - Constraint: Idle/Bat|
| - Action: Application |     | - Action: Exec Apify  |     | - Action: Evict stale |
|   Reminder Alerts     |     |   Job Search Scrape   |     |   cached job entities |
+-----------------------+     +-----------------------+     +-----------------------+
```

---

## SECTION 3: MODULE RESPONSIBILITIES

Below is the definitive contractual specification for every module within the codebase.

### 3.1 `:app` Module [VERIFIED BASELINE]
* **Purpose:** Application assembly root, entry point, global lifecycle, and Hilt injection container.
* **Responsibilities:** Host `MainActivity`, initialize `AivanceApp` application class, configure WorkManager Hilt `Configuration.Provider`, declare global Android Manifest permissions, and host global splash/theme logic.
* **Public API:** `AivanceApp.kt`, `MainActivity.kt`, `FollowUpWorker.kt`.
* **Allowed Dependencies:** `:navigation`, all `:feature:*` modules, `:core:database`, `:core:datastore`, `:core:network`, `:core:common`, `:core:designsystem`.
* **Forbidden Dependencies:** None (Root node).
* **Ownership:** Core Platform Engineering Team.
* **Future Evolution:** Add deep link routing activity handlers and Play Integrity initialization hooks.

---

### 3.2 `:navigation` Module [VERIFIED BASELINE]
* **Purpose:** Route definitions, screen composition assembly, and top-level navigation graph management.
* **Responsibilities:** Provide `AivanceNavGraph`, maintain `Destination` sealed hierarchy, manage `NavigationSuiteScaffold` bottom bar destinations, and wire feature ViewModels to screen composables.
* **Public API:** `AivanceNavGraph()`, `Destination`, `Route`.
* **Allowed Dependencies:** All `:feature:*` modules, `:core:designsystem`, `:core:common`, `libs.androidx.navigation3.ui`.
* **Forbidden Dependencies:** Direct SQLite/Database code, low-level OkHttp/Retrofit network clients.
* **Ownership:** Staff UI/UX Architect & Navigation Team.
* **Future Evolution:** Encapsulate `navigation3` alpha contracts into stable internal navigation wrappers.

---

### 3.3 `:core:common` Module [VERIFIED BASELINE]
* **Purpose:** Shared foundation models, coroutine dispatchers, and result handling utilities.
* **Responsibilities:** Expose `@Dispatcher` Hilt qualifiers (`IO`, `Default`, `Main`), provide `Result<T>` reactive flow wrappers, and shared extension functions.
* **Public API:** `AivanceDispatchers`, `DispatchersModule`, `Result<T>`, `asResult()`.
* **Allowed Dependencies:** `libs.kotlinx.coroutines.android`, `libs.javax.inject`.
* **Forbidden Dependencies:** Android UI (`androidx.compose`), Room Database, Retrofit/OkHttp, any feature module.
* **Ownership:** Lead Core Infrastructure Engineer.
* **Future Evolution:** Add baseline domain extensions and global performance tracing annotations.

---

### 3.4 `:core:database` Module [VERIFIED BASELINE]
* **Purpose:** Local relational data persistence engine using Room SQLite.
* **Responsibilities:** Host `AivanceDatabase`, declare Room DAOs (`ApplicationDao`, `AtsDao`, `CoverLetterDao`, `RoadmapDao`, `JobListingDao`), execute migrations, and export schema JSONs.
* **Public API:** `AivanceDatabase`, DAOs, Entity models (`ApplicationEntity`, `AtsResultEntity`, `CoverLetterEntity`, `RoadmapEntity`, `JobListingEntity`).
* **Allowed Dependencies:** `:core:common`, `libs.androidx.room.runtime`, `libs.androidx.room.ktx`.
* **Forbidden Dependencies:** Android UI, Network clients (`Retrofit`, `OkHttp`), Feature modules.
* **Ownership:** Principal Database Architect.
* **Future Evolution:** Support Room FTS4 full-text search and explicit SQLite schema migration verification tests.

---

### 3.5 `:core:datastore` Module [VERIFIED BASELINE]
* **Purpose:** Encrypted key-value persistence for configuration, credentials, and user preferences.
* **Responsibilities:** Provide `UserPreferences` repository backed by Encrypted DataStore, execute hardware-backed Tink/Keystore encryption for API keys, and manage settings state persistence.
* **Public API:** `UserPreferences`, `DataStoreModule`, `EncryptedUserPreferencesSerializer`.
* **Allowed Dependencies:** `:core:common`, `libs.androidx.datastore.preferences`, `libs.crypto.tink.android`.
* **Forbidden Dependencies:** Android UI Compose layouts, Room DAOs, Feature modules.
* **Ownership:** Staff Security Engineer & Core Infrastructure Team.
* **Future Evolution:** Support secure export/import of preference backups.

---

### 3.6 `:core:network` Module [VERIFIED BASELINE]
* **Purpose:** Remote API communication engine and Provider Abstraction Layer.
* **Responsibilities:** Define `AiProvider` and `JobProvider` interfaces, host `ProviderRegistry` factories, manage OkHttp interceptor chains, execute Retrofit calls, and enforce TLS security.
* **Public API:** `AiProvider`, `JobProvider`, `AiProviderRegistry`, `JobProviderRegistry`, `AiMessage`, `AiConfiguration`, Network DTOs.
* **Allowed Dependencies:** `:core:common`, `:core:datastore`, `libs.retrofit`, `libs.okhttp.logging`.
* **Forbidden Dependencies:** Android Compose UI, Room Database DAOs, Feature modules.
* **Ownership:** Principal Systems & Network Engineer.
* **Future Evolution:** Add gRPC support for streaming local LLM endpoints.

---

### 3.7 `:core:util` Module [VERIFIED BASELINE]
* **Purpose:** Utility functions for file management, URI resolution, and document parsing.
* **Responsibilities:** Provide safe file stream copying (`FileUtils`), execute PDF text extraction using PDFBox-Android across all minSdk 26+ devices (`PdfTextExtractor`), and sanitize file paths.
* **Public API:** `FileUtils`, `PdfTextExtractor`.
* **Allowed Dependencies:** `:core:common`, `libs.pdfbox.android`.
* **Forbidden Dependencies:** Compose UI, Database DAOs, Feature modules.
* **Ownership:** Core Utilities Team.
* **Future Evolution:** Add DOCX and plain-text resume extraction parsing support.

---

### 3.8 `:core:designsystem` Module [VERIFIED BASELINE]
* **Purpose:** Centralized Material Design 3 theme system and reusable Compose UI components.
* **Responsibilities:** Define color tokens (`Color.kt`), typography scale (`Type.kt`), Material3 theme wrapper (`Theme.kt`), and standardized design components (`AivanceButton`, `AivanceCard`, `AivanceTextField`, `AivanceLoading`, `AivanceError`, `AivanceSuccess`).
* **Public API:** `AivanceTheme`, Design System UI components.
* **Allowed Dependencies:** `libs.androidx.compose.material3`.
* **Forbidden Dependencies:** Business logic, Repositories, Database, Network clients, Feature modules.
* **Ownership:** Staff UI/UX Architect.
* **Future Evolution:** Expand component preview catalog with `@PreviewParameter` providers and dynamic Material You theme switches.

---

### 3.9 Feature Modules (`:feature:*`) [VERIFIED BASELINE]
The feature modules comprise: `:feature:dashboard`, `:feature:resume`, `:feature:ats`, `:feature:coverletter`, `:feature:interview`, `:feature:jobs`, `:feature:tracker`, `:feature:profile`, and `:feature:settings` **[PROPOSED ARCHITECTURE]**.
* **Purpose:** Self-contained business domains delivering specific user workflows.
* **Responsibilities:** Host feature Compose screens, ViewModels, Domain UseCases, Repository implementations, domain models, and feature Hilt DI modules.
* **Public API:** Entry Composable functions (e.g., `ResumeScreen()`), ViewModel state contracts (`ResumeUiState`).
* **Allowed Dependencies:** `:core:designsystem`, `:core:network`, `:core:database`, `:core:datastore`, `:core:common`, `:core:util`.
* **Forbidden Dependencies:** **STRICTLY FORBIDDEN to depend on any other `:feature:*` module.** Cross-feature communication must pass through `:navigation` or shared `:core` repositories.
* **Ownership:** Feature Squad Leads.

---

## SECTION 4: LAYERED ARCHITECTURE

The application enforces a strict 8-layer architecture within each feature and core domain.

```
+-----------------------------------------------------------------------------------+
| LAYERED ARCHITECTURE CONTRACTS                                                    |
+-----------------------------------------------------------------------------------+
  1. Presentation Layer    (Compose UI Screens, Cards, Dialogs)
     ↓
  2. ViewModel Layer       (StateFlow<UiState>, User Event Reducers)
     ↓
  3. UseCase Layer         (Business Domain Logic & Validation)
     ↓
  4. Repository Layer      (Single Source of Truth, Caching, Synchronization)
     ↓
  5. Provider Layer        (AiProvider, JobProvider Abstraction Interfaces)
     ↓
  6. Persistence Layer     (Room DAOs, Encrypted DataStore)
     ↓
  7. Network Layer         (Retrofit, OkHttp Interceptors, Apify Client)
     ↓
  8. Infrastructure/Util   (PdfTextExtractor, Coroutine Dispatchers)
+-----------------------------------------------------------------------------------+
```

---

### Layer Specifications

| Layer | Responsibilities | Allowed Communication | Forbidden Communication | Lifecycle / Scope |
| :--- | :--- | :--- | :--- | :--- |
| **1. Presentation** | Renders UI using Jetpack Compose; handles animations, layout, accessibility, and user input events. | Emits actions upward to ViewModel; reads immutable `UiState` via `StateFlow`. | Direct calls to UseCases, Repositories, DAOs, or Network Providers. | Composables tied to Compose Navigation BackStack Entry. |
| **2. ViewModels** | Manages UI state, transforms domain flows into `UiState`, handles event actions, manages `viewModelScope`. | Calls UseCases or Repositories. Exposes `StateFlow` and `SharedFlow` to UI. | Direct database operations, direct HTTP/network calls, holding Android `Context` references. | Survives configuration changes via `ViewModelStoreOwner`. |
| **3. UseCases** | Encapsulates single reusable business operations (e.g., `AnalyzeResumeUseCase`, `ScrapeJobsUseCase`). | Calls Repositories and Providers. Combines multiple data streams. | UI rendering, Android framework imports (`android.widget.*`), direct DAO calls. | Stateless singleton or transient execution within Coroutine scope. |
| **4. Repositories** | Single Source of Truth (SSOT). Coordinates local Room persistence with remote API Providers. | Calls DAOs, DataStore, and Provider interfaces. Maps entities to domain models. | Directly importing Compose UI, handling UI navigation, raising UI dialogs. | `@Singleton` or `@ActivityRetainedScoped`. |
| **5. Providers** | Standardized pluggable interfaces (`AiProvider`, `JobProvider`) abstracting vendor APIs. | Communicates with Network APIs (Retrofit) and Vendor SDKs. | Knowledge of Room Entities, ViewModels, or Compose UI. | `@Singleton`. |
| **6. Database** | Local SQLite storage executing queries, indexing, and transactional writes via Room. | Reads/writes SQLite via DAOs; emits reactive Kotlin `Flow<List<T>>`. | Network operations, business logic calculations, UI rendering. | `@Singleton` Database instance. |
| **7. Network** | HTTP communication, REST parsing, JSON serialization, TLS pinning, and retry backoff. | Operates over OkHttp / Retrofit networks; returns Kotlin `Result<T>` wrappers. | Direct UI operations, Room database writes. | `@Singleton` Network clients. |
| **8. Infrastructure**| Low-level OS capabilities: Android KeyStore, WorkManager, PDFBox text extraction, Coroutine Dispatchers. | Invoked by Repositories, Providers, or Utilities. | Business domain rules, Compose layout creation. | System Application Scope. |

---

## SECTION 5: DEPENDENCY RULES

### 5.1 Strict Structural Rules [ARCHITECTURAL DECISION]

1. **No Layer Skipping:**  
   UI Composables MUST NOT bypass ViewModels to access Repositories directly. ViewModels MUST NOT bypass Repositories to access DAOs or Network Services directly.

   ```
   [ UI ] ---> [ ViewModel ] ---> [ UseCase / Repository ] ---> [ Provider / DAO ]
   ```

2. **No Repository-to-Repository Coupling:**  
   Repositories must not inject other Repositories. If a business operation requires data from multiple repositories, a dedicated **UseCase** in the domain layer must be created to coordinate them.

3. **Zero Feature-to-Feature Dependencies:**  
   `:feature:A` MUST NEVER import or depend on `:feature:B`. All cross-feature data transfer occurs via URL query parameters, Navigation Routes, or shared `:core` database state streams.

   ```
   WRONG:  :feature:resume  --->  :feature:ats   (FORBIDDEN)
   RIGHT:  :feature:resume  --->  :core:database <--- :feature:ats
   ```

4. **No UI Talking Directly to Providers or Database:**  
   Compose functions must never reference `AivanceDatabase`, `AiService`, `AiProvider`, or `Retrofit` classes. All data access must pass through a ViewModel.

---

## SECTION 6: AI PLATFORM ARCHITECTURE

To support seamless runtime switching between Google Gemini, OpenAI, Groq, OpenRouter, and local Ollama instances, Aviance defines a vendor-agnostic AI Platform Architecture.

```
+-----------------------------------------------------------------------------------+
| AI PLATFORM ARCHITECTURE                                                          |
+-----------------------------------------------------------------------------------+

                          +-----------------------+
                          |   AiProviderManager   |
                          |  (Runtime Switching)  |
                          +-----------+-----------+
                                      |
            +-------------------------+-------------------------+
            |                                                   |
            v                                                   v
+-----------------------+                           +-----------------------+
|  AiProviderRegistry   |                           |  CapabilityRegistry   |
| (Discovers Providers) |                           | (Streaming, JSON Mode)|
+-----------+-----------+                           +-----------------------+
            |
    +-------+-------+-------------------+-------------------+
    |               |                   |                   |
    v               v                   v                   v
+-------+     +-----------+     +---------------+     +-----------+
|Gemini |     | OpenAI    |     |    Groq       |     |  Ollama   |
|Provider     | Provider  |     |   Provider    |     | Provider  |
+-------+     +-----------+     +---------------+     +-----------+
```

---

### 6.1 Technical Contracts & Interfaces [PROPOSED ARCHITECTURE]

```kotlin
package com.bangersoul.aivance.core.network.ai

import kotlinx.coroutines.flow.Flow

enum class ProviderId { GEMINI, OPENAI, GROQ, OPENROUTER, OLLAMA, MOCK }

enum class AiCapability { STREAMING, REASONING, SYSTEM_PROMPT, JSON_MODE, VISION }

data class AiConfiguration(
    val modelName: String,
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val maxTokens: Int = 2048,
    val responseFormat: ResponseFormat = ResponseFormat.TEXT
)

enum class ResponseFormat { TEXT, JSON }

interface AiProvider {
    val id: ProviderId
    val capabilities: Set<AiCapability>
    
    suspend fun generateText(prompt: String, config: AiConfiguration): Result<String>
    fun streamText(prompt: String, config: AiConfiguration): Flow<String>
    suspend fun chat(messages: List<AiMessage>, config: AiConfiguration): Result<String>
    suspend fun validateCredentials(apiKey: String, baseUrl: String?): Boolean
}
```

---

### 6.2 Provider Registry & Dynamic Model Discovery [PROPOSED ARCHITECTURE]

```kotlin
@Singleton
class AiProviderRegistry @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards AiProvider>
) {
    private val providerMap: Map<ProviderId, AiProvider> = providers.associateBy { it.id }

    fun getProvider(id: ProviderId): AiProvider {
        return providerMap[id] ?: error("AI Provider $id not registered in Hilt container.")
    }

    fun getAvailableProviders(): List<ProviderId> = providerMap.keys.toList()
}
```

---

### 6.3 Sequence Diagram: AI Text Generation with Fallback [ARCHITECTURAL DECISION]

```
+----------+      +-------------------+      +------------------+      +---------------+
| ViewModel|      | AiProviderManager |      | Primary Provider |      |Fallback Prov. |
+----+-----+      +---------+---------+      +--------+---------+      +-------+-------+
     |                      |                         |                          |
     | generateText(prompt) |                         |                          |
     +--------------------->|                         |                          |
     |                      | generateText(prompt)    |                          |
     |                      +------------------------>|                          |
     |                      |                         |                          |
     |                      | [HTTP 429 / Rate Limit] |                          |
     |                      |<------------------------+                          |
     |                      |                         |                          |
     |                      | [Trigger Fallback]      |                          |
     |                      | generateText(prompt)    |                          |
     |                      +--------------------------------------------------->|
     |                      |                         |                          |
     |                      |                         | [HTTP 200 OK Response]   |
     |                      |<---------------------------------------------------+
     | [Result.Success]     |                         |                          |
     |<---------------------+                         |                          |
```

---

## SECTION 7: JOB PLATFORM ARCHITECTURE

The Job Platform Architecture manages web job scraping, result normalization, deduplication, ranking, and offline persistence.

```
+-----------------------------------------------------------------------------------+
| JOB PLATFORM ARCHITECTURE                                                         |
+-----------------------------------------------------------------------------------+

+---------------------------------------------------------------------------------+
| JobSearchRepositoryImpl                                                         |
+----------------------------------------+----------------------------------------+
                                         |
                                         v
+---------------------------------------------------------------------------------+
| Search Pipeline Execution                                                       |
|  1. Query Dispatch   --> Query active JobProvider (Apify Job Provider)          |
|  2. Raw Ingestion    --> Fetch JSON payload from actor datasets                 |
|  3. Normalization    --> Map diverse schemas into unified JobListing domain model|
|  4. Deduplication    --> Fingerprint matching (SHA-256 hash on company+title)   |
|  5. Ranking Engine   --> Score match relevance against user career profile      |
|  6. Room Persistence --> Upsert clean records into JobListingEntity cache       |
+---------------------------------------------------------------------------------+
```

---

### 7.1 Sequence Diagram: Job Scraping Pipeline [ARCHITECTURAL DECISION]

```
+----------+     +--------------------+     +------------------+     +---------------+
| JobsVM   |     | JobSearchRepoImpl  |     | ApifyJobProvider |     | Room Cache DB |
+----+-----+     +---------+----------+     +--------+---------+     +-------+-------+
     |                     |                         |                         |
     | searchJobs(query)   |                         |                         |
     +-------------------->|                         |                         |
     |                     | searchJobs(query)       |                         |
     |                     +------------------------>|                         |
     |                     |                         | [Run Apify Actor]       |
     |                     |                         | [Poll Dataset Result]   |
     |                     | Normalized Job Listings |                         |
     |                     |<------------------------+                         |
     |                     |                         |                         |
     |                     | [Execute Deduplication] |                         |
     |                     | [Calculate Rank Scores] |                         |
     |                     |                         |                         |
     |                     | upsertJobListings()     |                         |
     |                     +-------------------------------------------------->|
     |                     |                         |                         |
     | [Emit StateFlow]    |                         |                         |
     |<--------------------+                         |                         |
```

---

## SECTION 8: SETTINGS ARCHITECTURE

A dedicated `:feature:settings` module **[PROPOSED ARCHITECTURE]** is established to manage system configuration, preferences, API keys, dark mode themes, and operational diagnostics.

### 8.1 Settings Hierarchy & State Architecture

```
+-----------------------------------------------------------------------------------+
| SETTINGS HIERARCHY                                                                |
+-----------------------------------------------------------------------------------+
  SettingsScreen (Root)
   ├── GeneralSubPage      (Language, Auto-sync, Clear Cache)
   ├── AppearanceSubPage   (Theme: System/Light/Dark, Dynamic Color Material You)
   ├── AiProvidersSubPage  (Active Provider, API Key, Model Picker, Temp Slider)
   ├── JobProvidersSubPage (Apify API Token, Scraper Actor Selector, Sync Frequency)
   ├── SecuritySubPage     (Encrypted Storage Toggle, Biometric Lock Toggle)
   ├── BackupRestoreSubPage(Export Database Backup, Import Restore JSON)
   └── DeveloperSubPage    (Log Level, Mock Toggle, Token Usage Telemetry)
```

### 8.2 Encrypted Preference Persistence [ARCHITECTURAL DECISION]
API Keys and sensitive tokens are encrypted using **Android Keystore-backed AES-256 GCM** encryption via Google Tink / EncryptedDataStore. Master key generation is tied to hardware-backed hardware key stores (`AndroidKeyStore`).

---

## SECTION 9: DATABASE ARCHITECTURE

The local database persistence layer uses **Room 2.6.1** over SQLite with explicit transaction handling, indexes, and automated schema migration management.

### 9.1 Entity Relationship Diagram (ERD) [ARCHITECTURAL DECISION]

```
+---------------------+          +-----------------------+
|  applications       |          |  ats_results          |
+---------------------+          +-----------------------+
| PK id: Long         |          | PK id: Long           |
|    company: String  |          |    score: Int         |
|    role: String     |          |    resumeName: String |
|    status: String   |          |    missingKeywords:Text|
|    dateApplied: Long|          |    date: Long [INDEX] |
|    followUpDate:Long|          +-----------------------+
| INDEX status        |
| INDEX dateApplied   |          +-----------------------+
+---------------------+          |  cover_letters        |
                                 +-----------------------+
                                 | PK id: Long           |
+---------------------+          |    company: String    |
|  roadmaps           |          |    role: String       |
+---------------------+          |    content: Text      |
| PK id: Long         |          |    tone: String       |
|    title: String    |          |    dateCreated: Long  |
+----------+----------+          +-----------------------+
           | 1
           |                     +-----------------------+
           | N (Cascade Delete)  |  job_listings [NEW]   |
           v                     +-----------------------+
+---------------------+          | PK id: String (Hash)  |
|  roadmap_steps      |          |    title: String      |
+---------------------+          |    company: String    |
| PK id: Long         |          |    salary: String     |
| FK roadmapId: Long  |          |    cachedTimestamp:L  |
|    title: String    |          | INDEX cachedTimestamp |
|    isCompleted: Bool|          +-----------------------+
| INDEX roadmapId     |
+---------------------+
```

---

### 9.2 Migration Strategy [ARCHITECTURAL DECISION]

1. **Schema Export Required:**  
   `exportSchema = true` is strictly enforced in `AivanceDatabase`. Schema JSON definitions are committed to `:core:database/schemas/` for version control tracking.

2. **Automated & Explicit Migrations:**  
   Every schema alteration requires an explicit `Migration(startVersion, endVersion)` implementation added to `AivanceDatabase`. Destructive migration fallbacks (`fallbackToDestructiveMigration()`) are **STRICTLY PROHIBITED** in production builds.

```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_applications_status` ON `applications` (`status`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `job_listings` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `company` TEXT NOT NULL, `salary` TEXT NOT NULL, `cachedTimestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
    }
}
```

---

## SECTION 10: NETWORK ARCHITECTURE

Network interactions are executed using Retrofit 2.11 and OkHttp 4.12 configured with custom interceptors, resilient retry backoffs, and TLS security rules.

```
+-----------------------------------------------------------------------------------+
| NETWORK OKHTTP INTERCEPTOR PIPELINE                                               |
+-----------------------------------------------------------------------------------+
  Request ──> [ AuthInterceptor ] ──> [ LoggingInterceptor ] ──> [ CircuitBreaker ] ──> Server
                                                                                            │
  Response <── [ RetryBackoffInterceptor ] <── [ ErrorHandlingInterceptor ] <───────────────┘
```

### 10.1 Circuit Breaker & Retry Policies [ARCHITECTURAL DECISION]
* **Exponential Backoff:** Requests failing due to temporary network timeouts or HTTP 429 Rate Limits automatically retry up to 3 times with jittered exponential delays (1s, 2s, 4s).
* **Circuit Breaker:** If a remote provider endpoint fails 5 consecutive requests within 60 seconds, the circuit breaker opens for 30 seconds, immediately short-circuiting subsequent requests to fallback providers without waiting for network timeouts.

---

## SECTION 11: SECURITY ARCHITECTURE

### 11.1 Security Model & Secrets Management [ARCHITECTURAL DECISION]

1. **Zero Hardcoded Secrets in VCS:**  
   No API keys or auth tokens may exist in Kotlin source code, `build.gradle.kts`, or committed property files. Local development keys must be injected via `local.properties` and read via `BuildConfig`. Production keys are retrieved dynamically from hardware-encrypted DataStore or secure backend remote config.

2. **Android Keystore Integration:**  
   Master encryption keys are generated inside the hardware-backed `AndroidKeyStore` provider (`AES/GCM/NoPadding`).

3. **Network Security Config & Certificate Pinning:**  
   Cleartext HTTP traffic is disabled globally in `AndroidManifest.xml` (`android:usesCleartextTraffic="false"`). TLS 1.3 is enforced. SHA-256 certificate pinning is configured for all core API hostnames in `res/xml/network_security_config.xml`.

---

## SECTION 12: BACKGROUND PROCESSING

Background tasks are managed exclusively via **AndroidX WorkManager 2.9.1** injected via Hilt `HiltWorkerFactory`.

```kotlin
@HiltWorker
class JobSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val jobSearchRepository: JobSearchRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            jobSearchRepository.refreshCachedJobs()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
```

---

## SECTION 13: STATE MANAGEMENT

UI state management adheres to Unidirectional Data Flow (UDF) using Kotlin `StateFlow` and immutable state models.

```
+-----------------------------------------------------------------------------------+
| UNIDIRECTIONAL STATE REDUCTION FLOW                                               |
+-----------------------------------------------------------------------------------+

   +----------------+           Action (Intent)          +--------------------+
   |  Compose UI    | ---------------------------------> |     ViewModel      |
   +----------------+                                    +---------+----------+
           ^                                                       |
           |                                                       | Executes
           |                                                       v
           |             StateFlow<UiState>              +--------------------+
           +-------------------------------------------- | State Reducer      |
                                                         +--------------------+
```

### 13.1 State Rules [ARCHITECTURAL DECISION]
* **Immutable State Models:** All `UiState` data classes must be immutable (`val` fields only) and annotated with `@Immutable` or `@Stable` to allow Jetpack Compose compiler skipping optimization.
* **Single State Flow:** Each ViewModel exposes exactly ONE primary `uiState: StateFlow<T>` stream.
* **One-Time Side Effects:** One-time side effects (such as navigation triggers or snackbar messages) must be emitted via `Channel` or `SharedFlow` buffered streams.

---

## SECTION 14: DESIGN SYSTEM ARCHITECTURE

The UI layer is built on a centralized Material Design 3 system (`:core:designsystem`).

### 14.1 Design System Rules [ARCHITECTURAL DECISION]
* **Touch Targets:** All interactive components (buttons, icons, chips) MUST satisfy the WCAG minimum 48x48dp touch target standard.
* **Dark Mode & Dynamic Color:** Theme definitions automatically adapt to system dark mode settings and support Android 12+ Material You dynamic color palettes.
* **Adaptive Layouts:** Screens must adapt responsively across Compact (Phones), Medium (Foldables), and Expanded (Tablets) window size classes using `NavigationSuiteScaffold`.

---

## SECTION 15: TESTING ARCHITECTURE

Aviance enforces a multi-tiered testing strategy target (>80% overall code coverage).

```
+-----------------------------------------------------------------------------------+
| TESTING PYRAMID ARCHITECTURE                                                      |
+-----------------------------------------------------------------------------------+
         / \        UI Compose Tests    (ComposeTestRule, HiltAndroidTest)
        /   \       Integration Tests   (Robolectric, End-to-End Flow Tests)
       /     \      DAO & Migration     (Room In-Memory Database Tests)
      /       \     ViewModel Tests     (Turbine, StateFlow Emission Tests)
     /---------\    Unit Tests          (MockK, Coroutines TestDispatcher)
+-----------------------------------------------------------------------------------+
```

* **Unit Tests:** Test ViewModels, UseCases, Repositories, and Parsers using MockK, JUnit5, and kotlinx-coroutines-test.
* **Flow Verification:** Test `StateFlow` emissions deterministically using **Turbine** (`app.cash.turbine`).
* **DAO & Migration Tests:** Verify Room database operations and SQLite migrations using in-memory databases (`Room.inMemoryDatabaseBuilder`).

---

## SECTION 16: OBSERVABILITY

System observability is implemented via structured logging, performance tracing, and metric collection without compromising user privacy.

* **Structured Logging:** Centralized `AivanceLogger` strips PII (Personally Identifiable Information), credentials, and resume text prior to writing logs.
* **AI Metric Telemetry:** Tracks request latency, input/output token counts, model names, HTTP status codes, and provider error rates.
* **Performance Tracing:** Traces app cold startup times and PDF extraction frame rendering latencies.

---

## SECTION 17: CI/CD ARCHITECTURE

```
+-----------------------------------------------------------------------------------+
| CONTINUOUS INTEGRATION / CONTINUOUS DELIVERY (CI/CD) PIPELINE                     |
+-----------------------------------------------------------------------------------+
  Git Push / PR ──> [ Static Analysis: Ktlint & Detekt ] ──> [ Unit & DAO Tests ]
                                                                      │
  Release Build <── [ Signed APK/AAB Assembly ] <── [ Compose UI Tests ] <┘
```

* **PR Validation Pipeline:** Every Pull Request triggers automated static analysis (Ktlint, Detekt), compile validation across all 17 modules, and execution of the unit test suite.
* **Static Analysis Rule Enforcers:** Enforce zero unused imports, strict explicit function return types, and code complexity thresholds.

---

## SECTION 18: ARCHITECTURE DECISION RECORDS (ADRs)

### ADR-001: Multi-Module Architecture
* **Context:** A monolithic `:app` module leads to high build times, tight code coupling, and poor team ownership boundaries.
* **Decision:** Adopt a strict 17-module structure separating `:app`, `:navigation`, `:core:*`, and `:feature:*` modules.
* **Consequences:** Significantly faster incremental build times and enforced boundary isolation. Requires explicit dependency management in `libs.versions.toml`.
* **Alternatives Considered:** Monolithic single-module structure; Layer-based modularization (`:data`, `:domain`, `:ui`).

---

### ADR-002: Pluggable AI Provider Abstraction
* **Context:** Direct integration with Google Gemini SDK binds the application to a single vendor, risking downtime, pricing shifts, or regional availability blocks.
* **Decision:** Abstract all AI inference behind a vendor-independent `AiProvider` interface managed by `AiProviderRegistry`.
* **Consequences:** Seamless runtime switching between Gemini, OpenAI, Groq, and local Ollama instances. Requires maintaining custom response serialization logic for non-Gemini providers.
* **Alternatives Considered:** Direct coupling to Google Generative AI SDK; Third-party multi-LLM wrapper libraries.

---

### ADR-003: Offline-First Architecture with Room
* **Context:** Mobile network connectivity is intermittent, creating slow UI loading states if data is fetched purely on-demand over HTTP.
* **Decision:** Use Room SQLite database as the Single Source of Truth (SSOT). All UI screens observe local database streams.
* **Consequences:** Immediate UI rendering, zero offline loading delays, automatic state synchronization. Requires managing complex database schemas and migration scripts.
* **Alternatives Considered:** Network-first caching with Retrofit memory cache; Raw SQLite helpers.

---

### ADR-004: Encrypted DataStore for Preference Persistence
* **Context:** Standard DataStore Preferences write plain-text JSON files to internal app storage, exposing user API credentials on rooted devices.
* **Decision:** Wrap DataStore preferences in Android Keystore AES-256 GCM encryption via Google Tink.
* **Consequences:** Hardware-backed protection of user secrets. Small cryptographic performance overhead during read/write operations.
* **Alternatives Considered:** Unencrypted DataStore Preferences; Plain `SharedPreferences`.

---

### ADR-005: Jetpack Compose UI Framework
* **Context:** Legacy Android XML Views increase layout boilerplate and state synchronization bugs.
* **Decision:** Build 100% of the user interface using Jetpack Compose and Material Design 3.
* **Consequences:** Declarative UI state binding, reduced layout code, unified design token system. Requires careful management of recomposition state stability.
* **Alternatives Considered:** Android XML View Layouts with ViewBinding.

---

### ADR-006: Hilt Dependency Injection
* **Context:** Manual dependency injection leads to massive boilerplate, lifecycle leaks, and difficult testing setups.
* **Decision:** Standardize on Hilt (Dagger) across all app, core, and feature modules.
* **Consequences:** Compile-time dependency graph validation, seamless ViewModel and WorkManager injection. Slightly increases build time due to KSP annotation processing.
* **Alternatives Considered:** Koin Service Locator; Manual Factory Injection.

---

### ADR-007: Dedicated Settings Feature Module
* **Context:** User settings and provider key management were previously fragmented inside `:feature:profile`.
* **Decision:** Extract configuration options into a new, dedicated `:feature:settings` module.
* **Consequences:** Clean separation of user profile data from application infrastructure configuration. Requires updating navigation graph routes.
* **Alternatives Considered:** Keeping settings nested inside `:feature:profile`.

---

### ADR-008: Apify Web Job Scraping Integration
* **Context:** The application required live job search capabilities, but building custom web scrapers for hundreds of job sites within the Android app is unfeasible.
* **Decision:** Integrate Apify REST API actors within `ApifyJobProvider` to execute cloud job scraping tasks.
* **Consequences:** Access to live LinkedIn, Indeed, and Glassdoor job data. Introduces dependency on Apify API tokens and actor execution quotas.
* **Alternatives Considered:** Hardcoded mock job search; In-app JSOUP web scraping.

---

### ADR-009: WorkManager for Background Tasks
* **Context:** Application follow-up reminders and job search caching require reliable execution even if the app process is terminated.
* **Decision:** Use AndroidX WorkManager for background job scheduling and notification dispatch.
* **Consequences:** OS-managed battery-optimized execution with strict constraint enforcement (e.g., Unmetered Network, Charging).
* **Alternatives Considered:** AlarmManager with BroadcastReceivers; Raw Coroutine Background Services.

---

### ADR-010: PDFBox-Android Text Extraction
* **Context:** Calling native `PdfRenderer.textContents` crashes devices running Android 8.0 through 14 (API 26–34) with `NoSuchMethodError`.
* **Decision:** Replace native `textContents` calls with `PDFTextStripper` from `pdfbox-android` across all supported SDK versions.
* **Consequences:** 100% crash-free PDF text extraction across all minSdk 26+ devices. Slightly increases APK size (~1.5 MB).
* **Alternatives Considered:** Requiring Android 15 (API 35) as minSdk (rejects 95% of users); Server-side PDF extraction.

---

## SECTION 19: ENGINEERING RULES

### Mandatory Development Rules [ARCHITECTURAL DECISION]

1. **Naming Conventions:**
   * Compose Screens: `[Feature]Screen.kt` (e.g., `ResumeScreen.kt`)
   * ViewModels: `[Feature]ViewModel.kt` (e.g., `ResumeViewModel.kt`)
   * State Contracts: `[Feature]UiState.kt` (e.g., `ResumeUiState.kt`)
   * Repositories: `[Domain]Repository.kt` (Interface) / `[Domain]RepositoryImpl.kt` (Class)

2. **Packaging Structure:**  
   Every feature module must strictly adhere to the following package layout:
   ```
   com.bangersoul.aivance.feature.[featureName]/
    ├── data/          (RepositoryImpl, Local/Remote DataSources)
    ├── di/            (Hilt Feature Modules)
    ├── domain/        (Repository Interfaces, UseCases, Domain Models)
    └── ui/            (Composables, ViewModels, UiState)
   ```

3. **Dependency Injection Rules:**
   * Every Repository implementation must be bound using `@Binds` in a Hilt module.
   * Direct class instantiation of services or repositories inside ViewModels or other services is strictly forbidden.

4. **Coroutines & Threading Rules:**
   * Never hardcode `Dispatchers.IO` or `Dispatchers.Default` directly in code. Always inject dispatchers via `@Dispatcher(AivanceDispatchers.IO)`.
   * All long-running file I/O or network calls MUST execute on the `IO` dispatcher.

5. **Compose Performance Rules:**
   * Never pass unstable collections (e.g., raw `java.util.List`) directly into Composables without wrapping them in immutable collections or annotating state classes with `@Immutable`.
   * Always wrap scroll state calculations or derived variables in `remember { derivedStateOf { ... } }`.

6. **Room Database Rules:**
   * All queries returning multiple rows must return reactive Kotlin `Flow<List<T>>` streams.
   * Every foreign key relationship must declare an explicit matching `@Index`.

7. **Security Rules:**
   * Never log raw user resume contents, personal names, or API credentials to Logcat or external telemetry servers.
   * Always sanitize strings before emitting debug logs.

---

## SECTION 20: FUTURE ARCHITECTURAL EVOLUTION

```
+-----------------------------------------------------------------------------------+
| FUTURE EVOLUTION ROADMAP                                                          |
+-----------------------------------------------------------------------------------+
  Phase 1: Plugin Architecture for Community AI & Job Providers
  Phase 2: Local On-Device LLM Integration (Ollama / Executable On-Device Models)
  Phase 3: Compose Multiplatform (CMP) Migration to Desktop & Web
  Phase 4: Cloud Sync & Enterprise Team Workspace Infrastructure
+-----------------------------------------------------------------------------------+
```

### 20.1 Extension Capabilities
* **Plugin Architecture:** Future releases will expose an extensible plugin interface allowing third-party developers to contribute custom `AiProvider` and `JobProvider` connectors via dynamic module loading.
* **Local On-Device LLM Execution:** Support integrated local inference via Ollama or MediaPipe LLM Inference API for complete privacy-preserving offline AI processing.
* **Compose Multiplatform (CMP):** Core logic (`:core:common`, `:core:network`, `:core:database`) and UI components (`:core:designsystem`) are engineered to facilitate seamless migration to Kotlin Multiplatform (KMP) targeting Desktop (JVM) and iOS.

---

*End of Master Architecture Specification for Aviance.*
