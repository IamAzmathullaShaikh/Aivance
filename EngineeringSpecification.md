# AVIANCE - FINAL ENGINEERING SPECIFICATION (SOFTWARE DESIGN SPECIFICATION - SDS)

**Document Type:** Software Design Specification (SDS) & Implementation Contract  
**Target Repository:** Aviance (Android Application)  
**Package Root:** `com.bangersoul.aivance`  
**Authors:** Chief Software Architect, Distinguished Android Engineer, Principal Technical Writer, Principal API Architect, Principal QA Architect, Principal DevOps Engineer, Principal Security Engineer, Engineering Standards Lead  
**Status:** Approved Master Engineering Specification / Active Implementation Baseline  
**Related References:** `Audit.md` (System Baseline & Deficiencies), `EngineeringPlan.md` (Release Roadmap), `Architecture.md` (System Architecture)  

---

## CLASSIFICATION TAXONOMY
Throughout this document, every architectural, interface, model, and behavioral specification is explicitly classified using the following taxonomy:
* **[VERIFIED REPOSITORY BEHAVIOR]:** Directly matches verified existing implementation in the repository codebase.
* **[ENGINEERING STANDARD]:** Non-negotiable implementation standard and coding convention mandatory across all modules.
* **[PROPOSED SPECIFICATION]:** Definitive concrete specification for new features, abstractions, interfaces, or migrations required to reach production readiness.
* **[ASSUMPTION]:** Operational assumption regarding runtime environment or third-party APIs where external interfaces are defined.

---

## SECTION 1: SYSTEM CONTRACTS

### 1.1 System Boundaries [ENGINEERING STANDARD]
* **Operating System Boundary:** Android OS (minSdk 26 / Android 8.0 Oreo to targetSdk 35 / Android 15).
* **Process Boundary:** Single primary Android process `com.bangersoul.aivance`. Background WorkManager tasks execute within the app's process space under `HiltWorker` initialization.
* **Network Boundary:** HTTPS / TLS 1.3 encrypted REST & WebSockets calls to remote LLM APIs (Google Gemini, OpenAI, Groq, OpenRouter) and Scraping Services (Apify Engine).
* **Storage Boundary:** Sandbox isolated internal storage (`/data/data/com.bangersoul.aivance/`) hosting `aivance_db` (Room SQLite) and `user_preferences.pb` (Encrypted DataStore). External storage interactions occur strictly via Android Storage Access Framework (`ActivityResultContracts.OpenDocument`).

### 1.2 Module Contracts [ENGINEERING STANDARD]
The project contains 17 Gradle modules operating under strict public boundary contracts:

1. **`:app`**
   * *Contract:* Implements `AivanceApp : Application()`, `MainActivity : ComponentActivity()`. Configures custom Hilt `Configuration.Provider` for `HiltWorkerFactory`.
   * *Public Symbols:* `AivanceApp`, `MainActivity`.
2. **`:navigation`**
   * *Contract:* Exposes `AivanceNavGraph` composable and `Destination` / `Route` sealed hierarchies. Encapsulates screen navigation transitions.
   * *Public Symbols:* `AivanceNavGraph()`, `Destination`, `Route`.
3. **`:core:common`**
   * *Contract:* Provides shared dispatchers, coroutine qualifiers, and reactive `Result<T>` sealed wrappers.
   * *Public Symbols:* `AivanceDispatchers`, `@Dispatcher`, `Result<T>`, `asResult()`.
4. **`:core:database`**
   * *Contract:* Manages local SQLite Room persistence. Exposes DAOs and Room Entity instances. Direct SQL execution outside DAOs is forbidden.
   * *Public Symbols:* `AivanceDatabase`, `ApplicationDao`, `AtsDao`, `CoverLetterDao`, `RoadmapDao`, `JobListingDao`, Entity classes.
5. **`:core:datastore`**
   * *Contract:* Manages key-value preference state. Encrypts sensitive keys via Android Keystore Tink wrapper.
   * *Public Symbols:* `UserPreferences`, `DataStoreModule`.
6. **`:core:network`**
   * *Contract:* Hosts remote communication contracts (`AiProvider`, `JobProvider`) and registry factories.
   * *Public Symbols:* `AiProvider`, `JobProvider`, `AiProviderRegistry`, `JobProviderRegistry`, `AiMessage`, `AiConfiguration`.
7. **`:core:util`**
   * *Contract:* Exposes document parsing and Uri utility contracts. Guarantees backwards-compatible PDF text extraction.
   * *Public Symbols:* `FileUtils`, `PdfTextExtractor`.
8. **`:core:designsystem`**
   * *Contract:* Centralizes Material Design 3 theme tokens, typography, colors, icons, and common UI components (`AivanceButton`, `AivanceCard`, `AivanceTextField`, `AivanceLoading`, `AivanceError`, `AivanceSuccess`).
   * *Public Symbols:* `AivanceTheme`, `AivanceButton`, `AivanceCard`, `AivanceTextField`, etc.
9. **`:feature:dashboard`**
   * *Contract:* Displays aggregate dashboard metrics. Exposes `DashboardScreen` composable and `DashboardViewModel`.
10. **`:feature:resume`**
    * *Contract:* Manages PDF/Text resume ingestion and AI matching analysis. Exposes `ResumeScreen` composable and `ResumeViewModel`.
11. **`:feature:ats`**
    * *Contract:* Manages historical ATS score reports and keyword optimization breakdown. Exposes `AtsScreen` composable and `AtsViewModel`.
12. **`:feature:coverletter`**
    * *Contract:* Generates tailored cover letters with tone selection. Exposes `CoverLetterScreen` composable and `CoverLetterViewModel`.
13. **`:feature:interview`**
    * *Contract:* Executes interactive mock interview sessions and structured AI feedback. Exposes `InterviewScreen` composable and `InterviewViewModel`.
14. **`:feature:jobs`**
    * *Contract:* Performs live job scraping, filtering, and job detail inspection. Exposes `JobsScreen` composable and `JobsViewModel`.
15. **`:feature:tracker`**
    * *Contract:* Tracks job application statuses, interviews, and offers. Exposes `TrackerScreen` composable and `TrackerViewModel`.
16. **`:feature:profile`**
    * *Contract:* Manages user career profile, skill inventory, and career roadmaps. Exposes `ProfileScreen` composable and `ProfileViewModel`.
17. **`:feature:settings` [PROPOSED SPECIFICATION]**
    * *Contract:* Centralized settings management for AI providers, scraper actors, database management, and security settings. Exposes `SettingsScreen` composable and `SettingsViewModel`.

### 1.3 Feature Contracts [ENGINEERING STANDARD]
Every feature module must expose exactly one top-level screen composable and one `ViewModel`. ViewModels expose a single `StateFlow<UiState>` property. Direct cross-feature module coupling is strictly prohibited. Communication between features must pass through explicit `Route` parameters or shared repository data streams.

### 1.4 Repository Contracts [ENGINEERING STANDARD]
Repositories act as the Single Source of Truth (SSOT). Repositories must:
* Return Kotlin `Flow<T>` for query streams.
* Use `suspend` functions for one-shot mutation operations returning `Result<T>`.
* Dispatch all disk IO and remote network operations explicitly to `Dispatchers.IO`.
* Automatically map raw DB entities or Network DTOs into immutable Domain Models before emitting to ViewModels.

### 1.5 Provider Contracts [PROPOSED SPECIFICATION]
All remote external engines (`AiProvider` and `JobProvider`) are defined as pluggable interfaces in `:core:network`. Implementations must be stateless or manage internal thread-safe caching, and be registered in `ProviderRegistry` instances. Dynamic provider resolution occurs at runtime based on active `UserPreferences`.

### 1.6 Persistence Contracts [ENGINEERING STANDARD]
* **Database:** Room SQLite with schema versioning enabled (`exportSchema = true`). Schema migrations must be explicitly tested. Destructive migrations in production releases are forbidden.
* **Preferences:** Encrypted DataStore Preferences. Raw plain-text storage of user keys, tokens, or credentials on disk is prohibited.

### 1.7 Navigation Contracts [VERIFIED REPOSITORY BEHAVIOR]
Navigation uses Jetpack Navigation 3 / Navigation Suite Scaffold. Destinations are defined as serializable `@Serializable` route objects inside `:navigation`. Deep links map directly to `Route` targets using URI parameters.

### 1.8 Dependency Contracts [ENGINEERING STANDARD]
* All dependencies are declared in `gradle/libs.versions.toml`.
* Dependency Injection is managed strictly via Hilt/Dagger (`2.51.1`). Manual singleton or factory static state instances are forbidden.
* Constructor injection is mandatory for all ViewModels, Repositories, UseCases, DAOs, and Workers.

### 1.9 Versioning Policy [ENGINEERING STANDARD]
The application uses Semantic Versioning (`MAJOR.MINOR.PATCH`):
* **MAJOR:** Architectural rewrites, minSdk bumps, or breaking data schema migrations requiring manual intervention.
* **MINOR:** New feature additions, new AI/Job provider implementations, schema migrations with backwards compatibility.
* **PATCH:** Bug fixes, performance optimizations, security patches, dependency updates.
* `versionCode` = `MAJOR * 10000 + MINOR * 100 + PATCH` (e.g., `1.0.0` -> `10000`).

### 1.10 Compatibility Policy [ENGINEERING STANDARD]
* **Android OS Compatibility:** Full functional capability guaranteed across API levels 26 through 35.
* **Java Toolchain:** JDK 17 / JVM target 17 across all Gradle modules.
* **API Backwards Compatibility:** Android 15 (API 35) APIs must be guarded with `@RequiresApi` or explicit runtime SDK checks (`Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM`). Standard Android 8.0-14 devices must execute fallback code paths without crashing.

---

## SECTION 2: DOMAIN MODEL SPECIFICATION

Below is the complete contractual specification for all domain models in `com.bangersoul.aivance`.

### 2.1 Resume Domain Model [PROPOSED SPECIFICATION]
```kotlin
data class Resume(
    val id: Long = 0,
    val fileName: String,
    val fileUri: String,
    val rawText: String,
    val parsedDate: Long = System.currentTimeMillis(),
    val characterCount: Int = rawText.length,
    val isPrimary: Boolean = false
)
```
* **Purpose:** Represents a candidate's ingested resume document.
* **Validation Rules:** `fileName` must not be blank; `rawText` must not be empty; `fileUri` must be a valid URI string.
* **Ownership:** `:feature:resume` & `:core:database`.
* **Persistence Mapping:** Maps to `ResumeEntity` [PROPOSED] / cached file stream.

### 2.2 ATS Result Domain Model [VERIFIED REPOSITORY BEHAVIOR]
```kotlin
data class AtsResult(
    val id: Long = 0,
    val score: Int,
    val date: Long = System.currentTimeMillis(),
    val resumeName: String,
    val missingKeywords: List<String>,
    val feedback: String,
    val matchingKeywords: List<String> = emptyList(),
    val formattingScore: Int = 100
)
```
* **Purpose:** Represents the output of an AI-driven ATS compliance evaluation.
* **Validation Rules:** `score` must be between `0` and `100`; `resumeName` must not be blank.
* **Ownership:** `:feature:ats` & `:core:database`.
* **Persistence Mapping:** Maps to `AtsResultEntity`.

### 2.3 Cover Letter Domain Model [VERIFIED REPOSITORY BEHAVIOR]
```kotlin
data class CoverLetter(
    val id: Long = 0,
    val company: String,
    val role: String,
    val content: String,
    val dateCreated: Long = System.currentTimeMillis(),
    val tone: LetterTone
)

enum class LetterTone {
    PROFESSIONAL,
    ENTHUSIASTIC,
    CONFIDENT,
    CREATIVE
}
```
* **Purpose:** Represents an AI-generated job application cover letter.
* **Validation Rules:** `company` and `role` must not be blank; `content` length must be >= 50 characters.
* **Ownership:** `:feature:coverletter` & `:core:database`.
* **Persistence Mapping:** Maps to `CoverLetterEntity`.

### 2.4 Interview Session & Feedback Domain Models [VERIFIED REPOSITORY BEHAVIOR]
```kotlin
data class InterviewSession(
    val id: String,
    val targetRole: String,
    val companyName: String = "",
    val difficulty: InterviewDifficulty = InterviewDifficulty.MEDIUM,
    val messages: List<InterviewMessage> = emptyList(),
    val feedback: InterviewFeedback? = null,
    val startTime: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

enum class InterviewDifficulty { EASY, MEDIUM, HARD }

data class InterviewMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender { USER, AI_INTERVIEWER }

data class InterviewFeedback(
    val overallScore: Int,
    val strengths: List<String>,
    val improvements: List<String>,
    val detailedSummary: String
)
```
* **Purpose:** Encapsulates mock interview dialogue and structured performance feedback.
* **Validation Rules:** `overallScore` between 0 and 100; `strengths` and `improvements` non-empty when session is completed.
* **Ownership:** `:feature:interview`.
* **Persistence Mapping:** Maps to `InterviewSessionEntity` & `InterviewMessageEntity` [PROPOSED].

### 2.5 Job Domain Model [PROPOSED SPECIFICATION]
```kotlin
data class JobListing(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val salaryRange: String?,
    val jobType: JobType = JobType.FULL_TIME,
    val isRemote: Boolean = false,
    val description: String,
    val url: String,
    val sourceProvider: String,
    val postedDate: Long = System.currentTimeMillis(),
    val matchScore: Int? = null
)

enum class JobType { FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP }
```
* **Purpose:** Unified domain entity for scraped or queried job postings across providers.
* **Validation Rules:** `id`, `title`, `company`, `url` must not be blank; `url` must match valid web URL regex.
* **Ownership:** `:feature:jobs` & `:core:network`.
* **Persistence Mapping:** Maps to `JobListingEntity`.

### 2.6 Application Domain Model [VERIFIED REPOSITORY BEHAVIOR]
```kotlin
data class JobApplication(
    val id: Long = 0,
    val company: String,
    val role: String,
    val status: ApplicationStatus,
    val dateApplied: Long = System.currentTimeMillis(),
    val salaryRange: String = "",
    val notes: String = "",
    val lastModified: Long = System.currentTimeMillis()
)

enum class ApplicationStatus {
    SAVED,
    APPLIED,
    INTERVIEWING,
    OFFER,
    REJECTED
}
```
* **Purpose:** Represents an application tracked in the user's career Kanban/List.
* **Validation Rules:** `company` and `role` non-blank; `lastModified` >= `dateApplied`.
* **Ownership:** `:feature:tracker` & `:core:database`.
* **Persistence Mapping:** Maps to `ApplicationEntity`.

### 2.7 Career Profile & Roadmap Models [VERIFIED REPOSITORY BEHAVIOR]
```kotlin
data class CareerRoadmap(
    val id: Long = 0,
    val targetRole: String,
    val currentLevel: String,
    val description: String,
    val steps: List<RoadmapStep> = emptyList()
)

data class RoadmapStep(
    val id: Long = 0,
    val roadmapId: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val stepOrder: Int
)
```
* **Purpose:** Represents personalized career development milestones generated by AI.
* **Validation Rules:** `targetRole` non-blank; `stepOrder` >= 0.
* **Ownership:** `:feature:profile` & `:core:database`.
* **Persistence Mapping:** Maps to `RoadmapEntity` and `RoadmapStepEntity`.

### 2.8 Provider & Settings Domain Models [PROPOSED SPECIFICATION]
```kotlin
data class AiProviderConfig(
    val providerId: String, // GEMINI, OPENAI, GROQ, OPENROUTER, OLLAMA
    val apiKey: String,
    val selectedModel: String,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val customBaseUrl: String? = null,
    val isEnabled: Boolean = true
)

data class JobScraperConfig(
    val providerId: String, // APIFY, DIRECT
    val apifyToken: String,
    val activeActorId: String,
    val syncIntervalHours: Int = 24,
    val cacheRetentionDays: Int = 7
)
```
* **Purpose:** Configuration models for runtime provider resolution in `:core:network` and `:feature:settings`.
* **Validation Rules:** `apiKey` syntax validated per provider; `temperature` in range `[0.0, 1.0]`.
* **Ownership:** `:core:datastore`, `:core:network`, `:feature:settings`.

---

## SECTION 3: DATABASE SPECIFICATION

### 3.1 Room Entities Specification [PROPOSED SPECIFICATION]

#### 1. `ApplicationEntity`
```kotlin
@Entity(
    tableName = "applications",
    indices = [
        Index(value = ["status"]),
        Index(value = ["dateApplied"])
    ]
)
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "company") val company: String,
    @ColumnInfo(name = "role") val role: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "dateApplied") val dateApplied: Long,
    @ColumnInfo(name = "salaryRange") val salaryRange: String,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "lastModified") val lastModified: Long
)
```

#### 2. `AtsResultEntity`
```kotlin
@Entity(
    tableName = "ats_results",
    indices = [Index(value = ["date"])]
)
data class AtsResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "score") val score: Int,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "resumeName") val resumeName: String,
    @ColumnInfo(name = "missingKeywords") val missingKeywords: String,
    @ColumnInfo(name = "feedback") val feedback: String
)
```

#### 3. `CoverLetterEntity`
```kotlin
@Entity(
    tableName = "cover_letters",
    indices = [Index(value = ["dateCreated"])]
)
data class CoverLetterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "company") val company: String,
    @ColumnInfo(name = "role") val role: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "dateCreated") val dateCreated: Long,
    @ColumnInfo(name = "tone") val tone: String
)
```

#### 4. `RoadmapEntity` & `RoadmapStepEntity`
```kotlin
@Entity(tableName = "roadmaps")
data class RoadmapEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String
)

@Entity(
    tableName = "roadmap_steps",
    foreignKeys = [
        ForeignKey(
            entity = RoadmapEntity::class,
            parentColumns = ["id"],
            childColumns = ["roadmapId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["roadmapId"])]
)
data class RoadmapStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "roadmapId") val roadmapId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "isCompleted") val isCompleted: Boolean,
    @ColumnInfo(name = "stepOrder") val stepOrder: Int
)
```

#### 5. `JobListingEntity` [PROPOSED SPECIFICATION]
```kotlin
@Entity(
    tableName = "job_listings",
    indices = [
        Index(value = ["postedDate"]),
        Index(value = ["company"]),
        Index(value = ["isRemote"])
    ]
)
data class JobListingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val company: String,
    val location: String,
    val salaryRange: String?,
    val jobType: String,
    val isRemote: Boolean,
    val description: String,
    val url: String,
    val sourceProvider: String,
    val postedDate: Long,
    val cachedTimestamp: Long = System.currentTimeMillis()
)
```

### 3.2 DAOs Specification [PROPOSED SPECIFICATION]

#### 1. `ApplicationDao`
```kotlin
@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications ORDER BY lastModified DESC")
    fun getAllApplications(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE status = :status ORDER BY lastModified DESC")
    fun getApplicationsByStatus(status: String): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE id = :id")
    suspend fun getApplicationById(id: Long): ApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: ApplicationEntity): Long

    @Update
    suspend fun updateApplication(application: ApplicationEntity)

    @Query("DELETE FROM applications WHERE id = :id")
    suspend fun deleteApplicationById(id: Long)

    @Query("SELECT COUNT(*) FROM applications")
    fun getApplicationCount(): Flow<Int>
}
```

#### 2. `AtsDao`
```kotlin
@Dao
interface AtsDao {
    @Query("SELECT * FROM ats_results ORDER BY date DESC")
    fun getAllAtsResults(): Flow<List<AtsResultEntity>>

    @Query("SELECT * FROM ats_results ORDER BY date DESC LIMIT 1")
    fun getLatestAtsResult(): Flow<AtsResultEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAtsResult(result: AtsResultEntity): Long

    @Query("DELETE FROM ats_results WHERE id = :id")
    suspend fun deleteAtsResultById(id: Long)
}
```

#### 3. `CoverLetterDao`
```kotlin
@Dao
interface CoverLetterDao {
    @Query("SELECT * FROM cover_letters ORDER BY dateCreated DESC")
    fun getAllCoverLetters(): Flow<List<CoverLetterEntity>>

    @Query("SELECT * FROM cover_letters WHERE id = :id")
    suspend fun getCoverLetterById(id: Long): CoverLetterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoverLetter(coverLetter: CoverLetterEntity): Long

    @Query("DELETE FROM cover_letters WHERE id = :id")
    suspend fun deleteCoverLetterById(id: Long)
}
```

#### 4. `RoadmapDao`
```kotlin
@Dao
interface RoadmapDao {
    @Query("SELECT * FROM roadmaps LIMIT 1")
    fun getRoadmap(): Flow<RoadmapEntity?>

    @Query("SELECT * FROM roadmap_steps WHERE roadmapId = :roadmapId ORDER BY stepOrder ASC")
    fun getStepsForRoadmap(roadmapId: Long): Flow<List<RoadmapStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmap(roadmap: RoadmapEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<RoadmapStepEntity>)

    @Query("UPDATE roadmap_steps SET isCompleted = :isCompleted WHERE id = :stepId")
    suspend fun updateStepCompletion(stepId: Long, isCompleted: Boolean)

    @Transaction
    suspend fun replaceRoadmapWithSteps(roadmap: RoadmapEntity, steps: List<RoadmapStepEntity>) {
        clearRoadmap()
        val id = insertRoadmap(roadmap)
        val stepsWithId = steps.map { it.copy(roadmapId = id) }
        insertSteps(stepsWithId)
    }

    @Query("DELETE FROM roadmaps")
    suspend fun clearRoadmap()
}
```

#### 5. `JobListingDao` [PROPOSED SPECIFICATION]
```kotlin
@Dao
interface JobListingDao {
    @Query("SELECT * FROM job_listings ORDER BY postedDate DESC")
    fun getCachedJobListings(): Flow<List<JobListingEntity>>

    @Query("SELECT * FROM job_listings WHERE title LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%'")
    fun searchCachedJobs(query: String): Flow<List<JobListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobListings(jobs: List<JobListingEntity>)

    @Query("DELETE FROM job_listings WHERE cachedTimestamp < :expiryThreshold")
    suspend fun deleteExpiredCache(expiryThreshold: Long)
}
```

### 3.3 Database Migrations & Schema Evolution [PROPOSED SPECIFICATION]
`AivanceDatabase` must increment version from `4` to `5` for indexes and `6` for `job_listings` table:
* `exportSchema = true` configured in `:core:database/build.gradle.kts`.
* **Migration 4 -> 5:** Adds performance indexes to `applications`, `ats_results`, and `cover_letters`.
* **Migration 5 -> 6:** Creates `job_listings` entity table with indices.
```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_applications_status` ON `applications` (`status`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_applications_dateApplied` ON `applications` (`dateApplied`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ats_results_date` ON `ats_results` (`date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_roadmap_steps_roadmapId` ON `roadmap_steps` (`roadmapId`)")
    }
}
```

---

## SECTION 4: API CONTRACT SPECIFICATION

Below are the exact public method signatures, parameters, return types, threading, and error contracts for all core repositories and managers.

### 4.1 `DashboardRepository` Contract [VERIFIED REPOSITORY BEHAVIOR]
```kotlin
interface DashboardRepository {
    fun getDashboardData(): Flow<Result<DashboardData>>
}
```
* **Threading:** `Dispatchers.IO`
* **Error Behavior:** Emits `Result.Error` if underlying Room stream fails.

### 4.2 `ResumeRepository` Contract [VERIFIED REPOSITORY BEHAVIOR]
```kotlin
interface ResumeRepository {
    suspend fun analyzeResume(resumeText: String, jobDescription: String): Result<ResumeAnalysis>
    suspend fun extractTextFromPdf(pdfUri: Uri): Result<String>
}
```
* **Threading:** `Dispatchers.IO`
* **Errors:** Returns `Result.Error(AivanceError.ParsingError)` for corrupt PDFs or `Result.Error(AivanceError.ProviderError)` on LLM failure.

### 4.3 `AtsRepository` Contract [VERIFIED REPOSITORY BEHAVIOR]
```kotlin
interface AtsRepository {
    fun getAtsResults(): Flow<List<AtsResult>>
    fun getLatestAtsResult(): Flow<AtsResult?>
    suspend fun saveAtsResult(result: AtsResult): Result<Long>
    suspend fun deleteAtsResult(id: Long): Result<Unit>
}
```
* **Threading:** `Dispatchers.IO`

### 4.4 `CoverLetterRepository` Contract [VERIFIED REPOSITORY BEHAVIOR]
```kotlin
interface CoverLetterRepository {
    suspend fun generateCoverLetter(
        resumeText: String,
        jobDescription: String,
        companyName: String,
        roleTitle: String,
        tone: LetterTone
    ): Result<CoverLetter>

    fun getSavedCoverLetters(): Flow<List<CoverLetter>>
    suspend fun saveCoverLetter(coverLetter: CoverLetter): Result<Long>
    suspend fun deleteCoverLetter(id: Long): Result<Unit>
}
```
* **Threading:** `Dispatchers.IO`

### 4.5 `InterviewRepository` Contract [VERIFIED REPOSITORY BEHAVIOR]
```kotlin
interface InterviewRepository {
    suspend fun startSession(role: String, company: String, difficulty: InterviewDifficulty): Result<InterviewSession>
    suspend fun sendMessage(sessionId: String, userMessage: String): Result<InterviewMessage>
    fun streamResponse(sessionId: String, userMessage: String): Flow<String>
    suspend fun finishSessionAndGetFeedback(sessionId: String): Result<InterviewFeedback>
}
```
* **Threading:** `Dispatchers.IO`
* **Errors:** Parses structured JSON feedback via `kotlinx.serialization`. On parse failure, falls back to structured error payload instead of hardcoded strings.

### 4.6 `JobSearchRepository` Contract [PROPOSED SPECIFICATION]
```kotlin
interface JobSearchRepository {
    fun searchJobs(
        query: String,
        location: String = "",
        isRemoteOnly: Boolean = false,
        forceRefresh: Boolean = false
    ): Flow<Result<List<JobListing>>>

    suspend fun getJobDetails(jobId: String): Result<JobListing>
}
```
* **Threading:** `Dispatchers.IO`
* **Behavior:** First emits cached `JobListingEntity` records from `JobListingDao`. If `forceRefresh` or cache stale, executes `JobProvider.searchJobs()`, updates database cache, and re-emits.

### 4.7 `JobTrackerRepository` Contract [VERIFIED REPOSITORY BEHAVIOR]
```kotlin
interface JobTrackerRepository {
    fun getApplications(): Flow<List<JobApplication>>
    fun getApplicationsByStatus(status: ApplicationStatus): Flow<List<JobApplication>>
    suspend fun addApplication(application: JobApplication): Result<Long>
    suspend fun updateApplicationStatus(id: Long, newStatus: ApplicationStatus): Result<Unit>
    suspend fun deleteApplication(id: Long): Result<Unit>
}
```
* **Threading:** `Dispatchers.IO`

### 4.8 `RoadmapRepository` Contract [VERIFIED REPOSITORY BEHAVIOR]
```kotlin
interface RoadmapRepository {
    fun getCareerRoadmap(): Flow<CareerRoadmap?>
    suspend fun generateRoadmap(targetRole: String, currentLevel: String): Result<CareerRoadmap>
    suspend fun toggleStepCompletion(stepId: Long, isCompleted: Boolean): Result<Unit>
}
```
* **Threading:** `Dispatchers.IO`

---

## SECTION 5: AI CONTRACTS

### 5.1 Core `AiProvider` Interfaces [PROPOSED SPECIFICATION]
```kotlin
interface AiProvider {
    val providerId: String // "GEMINI", "OPENAI", "GROQ", "OPENROUTER", "OLLAMA"
    val capabilities: Set<AiCapability>

    suspend fun generateText(
        prompt: String,
        config: AiConfiguration
    ): Result<String>

    fun streamText(
        prompt: String,
        config: AiConfiguration
    ): Flow<String>

    suspend fun generateStructuredJson(
        prompt: String,
        jsonSchema: String,
        config: AiConfiguration
    ): Result<String>

    suspend fun validateCredentials(apiKey: String, baseUrl: String?): Boolean
}

enum class AiCapability {
    TEXT_GENERATION,
    STREAMING,
    STRUCTURED_JSON,
    SYSTEM_PROMPTS,
    VISION
}

data class AiConfiguration(
    val modelName: String,
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val maxTokens: Int = 2048,
    val systemPrompt: String? = null
)
```

### 5.2 Provider Registry & Factory [PROPOSED SPECIFICATION]
```kotlin
interface ProviderRegistry {
    fun registerProvider(provider: AiProvider)
    fun getProvider(providerId: String): AiProvider?
    fun getAllProviders(): List<AiProvider>
}

@Singleton
class AiProviderFactory @Inject constructor(
    private val registry: ProviderRegistry,
    private val userPreferences: UserPreferences
) {
    suspend fun getActiveProvider(): AiProvider {
        val selectedId = userPreferences.selectedAiProvider.first()
        return registry.getProvider(selectedId)
            ?: registry.getProvider("GEMINI")
            ?: throw IllegalStateException("No valid AI provider registered")
    }
}
```

### 5.3 Streaming, Retry & Fallback Contract [PROPOSED SPECIFICATION]
* **Streaming Contract:** `streamText()` returns `Flow<String>` emitting sequential text tokens. Emits `Throwable` via Flow error downstream if network drops.
* **JSON Contract:** AI prompts requiring JSON include explicit JSON schema constraints. Output sanitization strips markdown formatting tags (e.g. ````json ... ````) before passing to `Json.decodeFromString()`.
* **Retry Policy:** 3 exponential backoff retries for transient HTTP errors (429, 503, SocketTimeoutException) with initial delay 1000ms, multiplier 2.0.
* **Fallback Policy:** If active provider fails with permanent error (401 Unauthorized / Invalid Key), `AiManager` automatically attempts fallback execution via `MockAiService` and triggers a user notification prompt.

---

## SECTION 6: JOB CONTRACTS

### 6.1 `JobProvider` Contract [PROPOSED SPECIFICATION]
```kotlin
interface JobProvider {
    val providerId: String // "APIFY", "DIRECT_API", "MOCK"

    suspend fun searchJobs(query: JobSearchQuery): Result<List<JobListing>>
    suspend fun fetchJobDetails(jobId: String): Result<JobListing>
    suspend fun validateConfig(apiToken: String): Boolean
}

data class JobSearchQuery(
    val keywords: String,
    val location: String = "",
    val isRemoteOnly: Boolean = false,
    val page: Int = 1,
    val limit: Int = 20
)
```

### 6.2 Search Pipeline & Data Normalization [PROPOSED SPECIFICATION]
The Job Platform pipeline executes 5 sequential stages:
1. **Query Construction:** Formats raw keywords and filter options into provider-specific actor payloads (e.g. Apify LinkedIn Scraper input JSON).
2. **Execution:** Dispatches background HTTP job to Apify REST endpoints or scraping actor instance.
3. **Normalization:** Maps heterogeneous scraped JSON fields to standard `JobListing` domain models.
4. **Deduplication:** Hashes `(company.toLowerCase() + title.toLowerCase() + location.toLowerCase())` into MD5 signature to filter duplicate postings.
5. **Ranking:** Scores listings against active user resume keywords (`0-100%` match score).

---

## SECTION 7: STATE SPECIFICATION

Every screen ViewModel exposes an immutable `StateFlow<UiState>`. UI states implement standard state patterns.

### 7.1 UiState Models Specification [VERIFIED REPOSITORY BEHAVIOR]

```kotlin
// Dashboard
sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val data: DashboardData) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

// Resume
sealed interface ResumeUiState {
    data object Idle : ResumeUiState
    data class ExtractingText(val progress: Float) : ResumeUiState
    data object Analyzing : ResumeUiState
    data class Success(val analysis: ResumeAnalysis) : ResumeUiState
    data class Error(val message: String) : ResumeUiState
}

// Jobs
sealed interface JobsUiState {
    data object Loading : JobsUiState
    data class Success(
        val jobs: List<JobListing>,
        val query: String,
        val isRefreshing: Boolean = false
    ) : JobsUiState
    data class Error(val message: String) : JobsUiState
    data object Empty : JobsUiState
}

// Settings [PROPOSED SPECIFICATION]
data class SettingsUiState(
    val aiConfig: AiProviderConfig,
    val scraperConfig: JobScraperConfig,
    val availableModels: List<String>,
    val isTestingConnection: Boolean = false,
    val connectionTestResult: String? = null,
    val errorMessage: String? = null
)
```

### 7.2 UI State Restoration & Cancellation [ENGINEERING STANDARD]
* **State Restoration:** Screen state parameters are preserved across Activity recreation / configuration changes using `SavedStateHandle` in ViewModels.
* **Cancellation:** Long-running AI generation coroutines are bound to `viewModelScope`. Navigating away automatically cancels active HTTP requests via Coroutine scope cancellation.

---

## SECTION 8: EVENT SPECIFICATION

Communication from UI to ViewModels uses explicit sealed event interfaces or lambda callbacks.

```kotlin
// Resume User Events
sealed interface ResumeUiEvent {
    data class SelectPdfFile(val uri: Uri) : ResumeUiEvent
    data class UpdateJobDescription(val text: String) : ResumeUiEvent
    data object SubmitAnalysis : ResumeUiEvent
    data object ClearError : ResumeUiEvent
}

// Navigation Events (SharedFlow / Channel)
sealed interface NavigationEvent {
    data class NavigateTo(val route: Route) : NavigationEvent
    data object PopBackStack : NavigationEvent
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : NavigationEvent
}
```

---

## SECTION 9: ERROR MODEL

### 9.1 Exception Hierarchy [ENGINEERING STANDARD]
```kotlin
sealed class AivanceError(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    data class ValidationError(val field: String, val reason: String) :
        AivanceError("Validation failed for $field: $reason")

    data class NetworkError(val statusCode: Int?, val rawMessage: String) :
        AivanceError("Network error [$statusCode]: $rawMessage")

    data class ProviderError(val providerId: String, val errorDetails: String) :
        AivanceError("Provider $providerId failed: $errorDetails")

    data class DatabaseError(val op: String, override val cause: Throwable) :
        AivanceError("Database operation $op failed", cause)

    data class ParsingError(val targetFormat: String, override val cause: Throwable) :
        AivanceError("Failed to parse $targetFormat", cause)

    data class TimeoutError(val timeoutMs: Long) :
        AivanceError("Operation timed out after ${timeoutMs}ms")

    data class AuthenticationError(val providerId: String) :
        AivanceError("Invalid or missing API key for $providerId")

    data class UnknownError(override val cause: Throwable) :
        AivanceError("An unexpected error occurred", cause)
}
```

### 9.2 User-Facing Error Mapping [ENGINEERING STANDARD]
`AivanceError` instances are mapped to user-localized strings in UI components via `AivanceError.toUserMessage()` extension.

---

## SECTION 10: VALIDATION RULES

### 10.1 System Validation Specification [ENGINEERING STANDARD]

1. **Resume Ingestion:**
   * Maximum allowed PDF file size: `10 MB` (10,485,760 bytes).
   * File extension must be `.pdf` or `.docx`.
   * Extracted raw text character count must be >= 100 characters.

2. **API Keys Validation Regex:**
   * **Google Gemini:** `^AIzaSy[a-zA-Z0-9_-]{33}$`
   * **OpenAI:** `^sk-[a-zA-Z0-9]{32,}$`
   * **Groq:** `^gsk_[a-zA-Z0-9]{48}$`
   * **Apify Token:** `^apify_api_[a-zA-Z0-9]{32,}$`

3. **Job Search Input:**
   * Keywords query length <= 100 characters.
   * Special characters stripped except `+`, `#`, `-`, `.`.

---

## SECTION 11: BACKGROUND WORK SPECIFICATION

### 11.1 WorkManager Specification [PROPOSED SPECIFICATION]

#### 1. `FollowUpWorker` [VERIFIED REPOSITORY BEHAVIOR]
* **Trigger:** Periodic (Every 24 Hours).
* **Constraints:** `NetworkType.CONNECTED`.
* **Action:** Checks `applications` database for applications in `APPLIED` status with `lastModified` > 7 days ago. Emits local system notification prompting user to record follow-up status.

#### 2. `JobSyncWorker` [PROPOSED SPECIFICATION]
* **Trigger:** Periodic (Every 12 Hours).
* **Constraints:** `NetworkType.UNMETERED`, `RequiresBatteryNotLow`.
* **Action:** Dispatches background job search query via active `JobProvider` to refresh cached job listings.

#### 3. `CacheCleanupWorker` [PROPOSED SPECIFICATION]
* **Trigger:** Periodic (Weekly).
* **Constraints:** `RequiresDeviceIdle`.
* **Action:** Evicts cached `JobListingEntity` records older than configured retention policy (e.g. 7 days).

---

## SECTION 12: SETTINGS SPECIFICATION

### 12.1 Preferences Keys & Defaults [PROPOSED SPECIFICATION]

| Preference Key | Data Type | Default Value | Encryption Status | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `selected_ai_provider` | `String` | `"GEMINI"` | Plain Text | Id of active LLM engine |
| `gemini_api_key` | `String` | `""` | **Encrypted** | User Gemini API Credential |
| `openai_api_key` | `String` | `""` | **Encrypted** | User OpenAI API Credential |
| `groq_api_key` | `String` | `""` | **Encrypted** | User Groq API Credential |
| `apify_api_token` | `String` | `""` | **Encrypted** | User Apify Scraper Credential |
| `ai_temperature` | `Float` | `0.7f` | Plain Text | Generation randomness |
| `dark_theme_mode` | `String` | `"SYSTEM"` | Plain Text | UI Dark/Light/System setting |
| `enable_biometric_lock`| `Boolean` | `false` | Plain Text | Enables App Biometric Gate |

---

## SECTION 13: SECURITY SPECIFICATION

### 13.1 Key Storage & Encryption [ENGINEERING STANDARD]
* **Key Generation:** Master keys are generated using `MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()`.
* **Encrypted Storage:** API Keys are serialized via Tink AES256 GCM encrypted Proto DataStore wrapper.
* **Network Security:** `res/xml/network_security_config.xml` enforces `cleartextTrafficPermitted="false"`. TLS 1.3 is enforced for all outgoing connections.
* **PII Protection:** Resume text and chat messages are stored locally on device only. No user data is transmitted to analytics servers.

---

## SECTION 14: OBSERVABILITY SPECIFICATION

### 14.1 Logging & Telemetry [ENGINEERING STANDARD]
* **Logging Framework:** `Timber` wrapper. Production builds strip `DEBUG` logs.
* **Sanitization:** Log messages automatically mask API keys matching regex `(?i)(key|secret|token)=[\w-]+` with `[REDACTED]`.
* **AI Metrics Tracked:** Latency per request (ms), Time to First Token (TTFT ms), Token count (prompt + completion), HTTP error code frequencies.

---

## SECTION 15: TEST SPECIFICATION

### 15.1 Testing Architecture & Targets [ENGINEERING STANDARD]

1. **Unit Tests (`src/test`):**
   * *Target:* ViewModels, Repositories, UseCases, Parsers, Validation rules.
   * *Frameworks:* JUnit 4, MockK, Kotlinx Coroutines Test, Turbine.
   * *Coverage Target:* >= 80% line coverage across domain and data layers.

2. **DAO Tests (`src/androidTest`):**
   * *Target:* All Room DAOs and Migrations.
   * *Frameworks:* Room In-Memory Database, AndroidX Test Runner.
   * *Coverage Target:* 100% DAO method verification.

3. **Compose UI Tests (`src/androidTest`):**
   * *Target:* Screen composables, Design System components, Navigation flows.
   * *Frameworks:* Compose UI Test (`createComposeRule()`), HiltTestRunner.

---

## SECTION 16: PERFORMANCE SPECIFICATION

### 16.1 Measurable Performance SLAs [ENGINEERING STANDARD]

| Metric | Target Threshold | Measurement Tool |
| :--- | :--- | :--- |
| **Cold Startup Latency** | < 1200 ms | Macrobenchmark / Android Vitals |
| **Warm Startup Latency** | < 400 ms | Macrobenchmark |
| **Frame Render Time** | 99% frames < 16.6 ms (60 fps) | JankStats / Profileable Build |
| **Peak Memory Usage** | < 250 MB | Android Studio Profiler |
| **Room Query Execution** | < 15 ms | Room Query Tracing |
| **AI Time to First Token** | < 800 ms (Streaming) | Telemetry Tracker |
| **Background Battery Impact**| < 1% per 24 hours | Battery Historian |

---

## SECTION 17: ACCESSIBILITY SPECIFICATION

### 17.1 Accessibility Standards [ENGINEERING STANDARD]
* **TalkBack Support:** Every interactive component (`AivanceButton`, `AivanceCard`) exposes `contentDescription` or `semantics { role = Role.Button }`.
* **Touch Targets:** Minimum touch target size `48dp x 48dp` enforced for all clickable elements.
* **Color Contrast:** Minimum contrast ratio 4.5:1 for normal text and 3:1 for large text across light and dark themes.
* **Dynamic Font Scaling:** UI scales seamlessly up to 200% font scaling without text clipping or layout overflow.

---

## SECTION 18: ENGINEERING STANDARDS

### 18.1 Code Style & Formatting [ENGINEERING STANDARD]
* **Language:** Kotlin 2.0.21.
* **Static Analysis:** `Ktlint` and `Detekt` rules enforced prior to commit.
* **Coroutines:** Explicit dispatcher injection (`@Dispatcher(AivanceDispatchers.IO)`) required. Never call `Dispatchers.IO` directly inside classes.
* **Compose Re-composition:** Data classes passed to Composables must be annotated with `@Immutable` or wrap collections with kotlinx immutable collections to avoid unnecessary re-compositions.

---

## SECTION 19: TRACEABILITY MATRIX

### 19.1 Feature Traceability Matrix

| Feature | Audit.md Ref | Architecture.md Ref | EngineeringPlan.md Ref | Primary Repository | Primary ViewModel | Database Entities | Provider / Worker | Test Suite |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Dashboard** | Sec 3.1 | Sec 2.1 | Milestone 1 | `DashboardRepository` | `DashboardViewModel` | `ApplicationEntity`, `AtsResultEntity` | N/A | `DashboardScreenTest` |
| **Resume** | Sec 3.7 / BUG-001 | Sec 3.3 | Milestone 1 | `ResumeRepository` | `ResumeViewModel` | `AtsResultEntity` | `PdfTextExtractor` | `ResumeViewModelTest` |
| **ATS** | Sec 3.3 | Sec 3.4 | Milestone 2 | `AtsRepository` | `AtsViewModel` | `AtsResultEntity` | `AiProvider` | `AtsDaoTest` |
| **Cover Letter**| Sec 3.4 | Sec 3.4 | Milestone 2 | `CoverLetterRepository` | `CoverLetterViewModel` | `CoverLetterEntity` | `AiProvider` | `CoverLetterRepositoryTest` |
| **Interview** | Sec 3.6 / BUG-004 | Sec 6.1 | Milestone 3 | `InterviewRepository` | `InterviewViewModel` | `InterviewSessionEntity` | `AiProvider` | `InterviewViewModelTest` |
| **Jobs** | Sec 3.7 / BUG-003 | Sec 7.1 | Milestone 4 | `JobSearchRepository` | `JobsViewModel` | `JobListingEntity` | `JobProvider`, `JobSyncWorker`| `JobSearchRepositoryTest` |
| **Tracker** | Sec 3.8 | Sec 3.4 | Milestone 2 | `JobTrackerRepository` | `TrackerViewModel` | `ApplicationEntity` | `FollowUpWorker` | `ApplicationDaoTest` |
| **Profile** | Sec 3.8 | Sec 3.4 | Milestone 2 | `RoadmapRepository` | `ProfileViewModel` | `RoadmapEntity`, `RoadmapStepEntity`| `AiProvider` | `RoadmapDaoTest` |
| **Settings** | Sec 13 / BUG-005 | Sec 8.1 | Milestone 5 | `SettingsRepository` | `SettingsViewModel` | Encrypted DataStore | `AiProviderRegistry` | `SettingsViewModelTest` |

---

## SECTION 20: DEFINITION OF DONE (DoD)

A feature or engineering change is officially **DONE** and ready for production release when it satisfies all of the following criteria:

1. **Architecture Compliance:** Strict adherence to UDF, Clean Architecture, and DAG module dependencies verified.
2. **Contract Compliance:** Implementation completely satisfies interface contracts defined in this specification.
3. **Compilation & Build:** Compiles cleanly with zero warnings or errors on Kotlin 2.0.21 and Gradle 8.11.
4. **Testing Targets Met:**
   * Unit test coverage >= 80% for affected domain/data modules.
   * 100% Room DAO integration tests passing.
   * Compose UI tests passing for modified screens.
5. **Performance SLA Verified:** Zero frame drops during scrolling; memory usage stays under 250MB; cold startup < 1.2s.
6. **Security & Privacy Checked:** No hardcoded API keys; all sensitive tokens stored in Encrypted DataStore; PII masked in logs.
7. **Accessibility Audited:** TalkBack descriptions provided; touch target sizes >= 48dp; contrast ratio >= 4.5:1.
8. **Static Analysis Clean:** Passes `Ktlint` and `Detekt` checks without suppression annotations.
9. **Zero Critical Bugs:** Zero P0/P1 bugs in Bug Register.

---
*End of Master Software Design Specification for Aviance.*
