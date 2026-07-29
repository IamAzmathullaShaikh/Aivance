# AVIANCE - MASTER API CONTRACT SPECIFICATION

**Document Type:** API Contract Specification & Software Interface Handbook  
**Target Repository:** Aviance (Android Application)  
**Package Root:** `com.bangersoul.aivance`  
**Authors:** Principal API Architect, Principal Android Engineer, Staff Software Architect, Technical Documentation Lead  
**Status:** Master Source of Truth / Active API Contract Baseline  
**Related References:** `Audit.md` (System Baseline), `EngineeringPlan.md` (Roadmap), `Architecture.md` (System Design), `EngineeringSpecification.md` (Software Design Specification)

---

## SECTION 1: REPOSITORY APIS

Repositories act as the Single Source of Truth (SSOT). All repository interfaces expose reactive read streams via `kotlinx.coroutines.flow.Flow` and suspending write operations returning `com.bangersoul.aivance.core.common.result.Result<T>`.

---

### 1.1 `DashboardRepository`

**Package:** `com.bangersoul.aivance.feature.dashboard.domain`

#### Method: `getDashboardData()`
* **Purpose:** Provides a real-time reactive data stream containing aggregate dashboard statistics (ATS score, active applications count, recent activity feed, and resume status).
* **Inputs:** None.
* **Outputs:** `Flow<DashboardData>`
* **Errors:** Catches SQLite exceptions and emits fallback `DashboardData` with default zero-state values.
* **Threading:** Dispatches database observation on `Dispatchers.IO`; emits on active collector context.
* **Performance Expectations (SLA):** First flow emission <= 50ms; subsequent emissions < 10ms.
* **Security Considerations:** Contains local user application counts; no PII exported off-device.
* **Example Code Usage:**
  ```kotlin
  val dashboardDataFlow: Flow<DashboardData> = dashboardRepository.getDashboardData()
  ```

#### Method: `refreshDashboardData()`
* **Purpose:** Forces a re-query and cache invalidation across underlying DAOs.
* **Inputs:** None.
* **Outputs:** `suspend fun refreshDashboardData(): Result<Unit>`
* **Errors:** Returns `Result.Error(Throwable)` if local database is locked or corrupted.
* **Threading:** `suspend` function executing on `Dispatchers.IO`.
* **Performance Expectations (SLA):** Execution time <= 100ms.
* **Security Considerations:** Internal process state synchronization only.
* **Example Payload Response:** `Result.Success(Unit)`

---

### 1.2 `ResumeRepository`

**Package:** `com.bangersoul.aivance.feature.resume.domain.repository`

#### Method: `analyzeResume(fileUri: Uri, jobDescription: String)`
* **Purpose:** Ingests a PDF/Text resume from a Uri, extracts raw text safely across API 26-35, and invokes the active AI provider to compute a structured match analysis.
* **Inputs:**
  * `fileUri: Uri` — Local content Uri of the uploaded resume document.
  * `jobDescription: String` — Target job posting text (10–10,000 chars).
* **Outputs:** `fun analyzeResume(fileUri: Uri, jobDescription: String): Flow<Result<ResumeAnalysis>>`
* **Errors:**
  * `ValidationException` (Empty job description or unreadable file Uri).
  * `PdfParseException` (Corrupted or password-protected PDF).
  * `ProviderException` (AI rate limit, invalid API key, network timeout).
  * `JsonParseException` (AI model emitted invalid structured JSON).
* **Threading:** Dispatches PDF IO and AI network call to `Dispatchers.IO`.
* **Performance Expectations (SLA):** Total execution <= 5,000ms (PDF parsing <= 300ms, LLM response <= 4,500ms).
* **Security Considerations:** Extracts raw text strictly in memory; PDF temporary buffers deleted after execution. API keys retrieved securely via Keystore.
* **Example Payload Output:**
  ```json
  {
    "overallScore": 85,
    "matchingKeywords": ["Kotlin", "Jetpack Compose", "Coroutines", "Hilt"],
    "missingKeywords": ["GraphQL", "KMP"],
    "suggestions": [
      "Highlight experience with cross-platform architecture",
      "Quantify performance improvements in recent projects"
    ],
    "matchSummary": "Strong alignment for Senior Android Engineer role."
  }
  ```

#### Method: `getResumeHistory()`
* **Purpose:** Observes the list of ingested resumes sorted by parsed date descending.
* **Inputs:** None.
* **Outputs:** `Flow<List<Resume>>`
* **Errors:** Emits empty list on database read failure.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** First emission <= 30ms.
* **Security Considerations:** Reads local database only.

#### Method: `deleteResume(id: Long)`
* **Purpose:** Removes a resume entry and its associated local file cache from storage.
* **Inputs:** `id: Long` — Unique primary key identifier of the resume.
* **Outputs:** `suspend fun deleteResume(id: Long): Result<Unit>`
* **Errors:** Returns `Result.Error` if ID is non-existent.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Execution time <= 50ms.
* **Security Considerations:** Permanently purges user resume data from disk sandbox.

---

### 1.3 `AtsRepository`

**Package:** `com.bangersoul.aivance.feature.ats.domain`

#### Method: `getAtsResults()`
* **Purpose:** Retrieves historical ATS evaluation reports ordered by date descending.
* **Inputs:** None.
* **Outputs:** `Flow<List<AtsResult>>`
* **Errors:** Maps DB errors to `Result.Error`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** First emission <= 30ms.
* **Security Considerations:** Local persistence access.

#### Method: `getLatestAtsResult()`
* **Purpose:** Emits the most recent ATS score record for display on dashboard cards.
* **Inputs:** None.
* **Outputs:** `Flow<AtsResult?>`
* **Errors:** Emits `null` if no records exist.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** First emission <= 20ms.
* **Security Considerations:** Read-only local stream.

#### Method: `saveAtsResult(result: AtsResult)`
* **Purpose:** Persists a newly calculated ATS evaluation result into Room storage.
* **Inputs:** `result: AtsResult` — Domain ATS report object.
* **Outputs:** `suspend fun saveAtsResult(result: AtsResult): Result<Long>`
* **Errors:** `Result.Error` if database insert fails.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Insert latency <= 25ms.
* **Security Considerations:** Encrypts or sanitizes user feedback text before writing.

---

### 1.4 `CoverLetterRepository`

**Package:** `com.bangersoul.aivance.feature.coverletter.domain.repository`

#### Method: `generateCoverLetter(resumeText: String, jobDescription: String, tone: LetterTone)`
* **Purpose:** Generates a personalized cover letter using the active AI provider based on specified tone and job context.
* **Inputs:**
  * `resumeText: String` — Raw text of candidate's resume.
  * `jobDescription: String` — Target job description.
  * `tone: LetterTone` — Enum (`PROFESSIONAL`, `ENTHUSIASTIC`, `CONFIDENT`, `CREATIVE`).
* **Outputs:** `fun generateCoverLetter(resumeText: String, jobDescription: String, tone: LetterTone): Flow<Result<CoverLetter>>`
* **Errors:** `ValidationException`, `ProviderException`, `NetworkException`.
* **Threading:** Dispatches network and DB operations to `Dispatchers.IO`.
* **Performance Expectations (SLA):** Initial chunk <= 1,000ms; full letter generation <= 6,000ms.
* **Security Considerations:** Prompt templates sanitized against injection attacks.
* **Example Payload Output:**
  ```json
  {
    "id": 102,
    "company": "JetBrains",
    "role": "Senior Android Developer",
    "content": "Dear Hiring Team,\n\nI am writing to express my strong enthusiasm...",
    "dateCreated": 1774900000000,
    "tone": "PROFESSIONAL"
  }
  ```

#### Method: `getSavedCoverLetters()`
* **Purpose:** Returns a reactive stream of all saved cover letters.
* **Inputs:** None.
* **Outputs:** `Flow<List<CoverLetter>>`
* **Errors:** Emits empty list on failure.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Latency <= 30ms.
* **Security Considerations:** Local read.

---

### 1.5 `InterviewRepository`

**Package:** `com.bangersoul.aivance.feature.interview.domain`

#### Method: `startInterviewSession(role: String, company: String, difficulty: InterviewDifficulty)`
* **Purpose:** Initializes a new interactive mock interview session with context initialization.
* **Inputs:**
  * `role: String` — Target position (e.g., "Android Architect").
  * `company: String` — Target company name (optional).
  * `difficulty: InterviewDifficulty` — `EASY`, `MEDIUM`, `HARD`.
* **Outputs:** `suspend fun startInterviewSession(role: String, company: String, difficulty: InterviewDifficulty): Result<InterviewSession>`
* **Errors:** `ValidationException` if role is blank.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Execution time <= 500ms.
* **Security Considerations:** Creates session context in isolated memory.

#### Method: `sendInterviewMessage(sessionId: String, userMessage: String)`
* **Purpose:** Sends user chat response to AI interviewer and streams/emits the interviewer's reply.
* **Inputs:**
  * `sessionId: String` — Active interview session ID.
  * `userMessage: String` — Candidate text input.
* **Outputs:** `fun sendInterviewMessage(sessionId: String, userMessage: String): Flow<Result<InterviewMessage>>`
* **Errors:** `ProviderException`, `RateLimitException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** First message byte <= 800ms; complete response <= 4,000ms.
* **Security Considerations:** Conversation history windowed to prevent token limit overflow and prompt injection.

#### Method: `endSessionAndGenerateFeedback(sessionId: String)`
* **Purpose:** Concludes the interview session, analyzes complete dialogue history, and outputs structured performance feedback.
* **Inputs:** `sessionId: String`
* **Outputs:** `suspend fun endSessionAndGenerateFeedback(sessionId: String): Result<InterviewFeedback>`
* **Errors:** `JsonParseException` if structured feedback parsing fails.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Analysis time <= 4,000ms.
* **Security Considerations:** Clears sensitive in-memory transcript buffers.

---

### 1.6 `JobSearchRepository`

**Package:** `com.bangersoul.aivance.feature.jobs.domain`

#### Method: `searchJobs(query: JobSearchQuery)`
* **Purpose:** Queries remote scraping services or direct job search APIs using active scraper configurations and caches results locally.
* **Inputs:** `query: JobSearchQuery` (keywords, location, isRemoteOnly, page, limit).
* **Outputs:** `fun searchJobs(query: JobSearchQuery): Flow<Result<PageResponse<JobListing>>>`
* **Errors:** `ScraperException`, `NetworkException`, `RateLimitException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Cached query <= 50ms; live scraper query <= 3,500ms.
* **Security Considerations:** Apify tokens sent strictly via HTTPS Bearer authorization header.
* **Example Payload Output:**
  ```json
  {
    "items": [
      {
        "id": "job_99812",
        "title": "Staff Android Engineer",
        "company": "Google",
        "location": "Mountain View, CA / Remote",
        "salaryRange": "$180,000 - $240,000",
        "jobType": "FULL_TIME",
        "isRemote": true,
        "description": "We are seeking a Staff Android Engineer...",
        "url": "https://careers.google.com/jobs/99812",
        "sourceProvider": "APIFY_LINKEDIN",
        "postedDate": 1774850000000
      }
    ],
    "page": 1,
    "totalPages": 5,
    "totalItems": 95,
    "hasNext": true
  }
  ```

---

### 1.7 `JobTrackerRepository`

**Package:** `com.bangersoul.aivance.feature.tracker.domain`

#### Method: `getApplications()`
* **Purpose:** Returns a reactive stream of all tracked job applications sorted by last modified timestamp.
* **Inputs:** None.
* **Outputs:** `Flow<List<JobApplication>>`
* **Errors:** Emits empty list on failure.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Latency <= 20ms.

#### Method: `updateApplicationStatus(id: Long, status: ApplicationStatus)`
* **Purpose:** Updates the lifecycle state (`SAVED`, `APPLIED`, `INTERVIEWING`, `OFFER`, `REJECTED`) of a tracked job application.
* **Inputs:** `id: Long`, `status: ApplicationStatus`
* **Outputs:** `suspend fun updateApplicationStatus(id: Long, status: ApplicationStatus): Result<Unit>`
* **Errors:** `Result.Error` if ID not found.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Execution time <= 25ms.

---

### 1.8 `RoadmapRepository`

**Package:** `com.bangersoul.aivance.feature.profile.domain`

#### Method: `getRoadmap()`
* **Purpose:** Retrieves active career roadmap with milestone steps.
* **Inputs:** None.
* **Outputs:** `Flow<CareerRoadmap?>`
* **Errors:** Returns `null` if no roadmap has been generated.
* **Threading:** `Dispatchers.IO`.

#### Method: `generateRoadmap(targetRole: String, currentSkills: List<String>)`
* **Purpose:** Uses AI provider to synthesize a step-by-step career advancement plan.
* **Inputs:** `targetRole: String`, `currentSkills: List<String>`
* **Outputs:** `fun generateRoadmap(targetRole: String, currentSkills: List<String>): Flow<Result<CareerRoadmap>>`
* **Errors:** `ProviderException`, `JsonParseException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Latency <= 4,500ms.

---

### 1.9 `SettingsRepository`

**Package:** `com.bangersoul.aivance.feature.settings.domain`

#### Method: `getAiProviderConfig()`
* **Purpose:** Observes active AI provider preference settings (provider ID, encrypted API key, model selection, temperature).
* **Inputs:** None.
* **Outputs:** `Flow<AiProviderConfig>`
* **Errors:** Emits default fallback configuration if DataStore read fails.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Read latency <= 15ms.
* **Security Considerations:** Decrypts API key in Keystore before returning domain model.

#### Method: `saveAiProviderConfig(config: AiProviderConfig)`
* **Purpose:** Encrypts and persists updated AI provider settings to DataStore.
* **Inputs:** `config: AiProviderConfig`
* **Outputs:** `suspend fun saveAiProviderConfig(config: AiProviderConfig): Result<Unit>`
* **Errors:** `SecurityException` if Keystore encryption fails.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Write latency <= 40ms.
* **Security Considerations:** Credentials encrypted using AES-256-GCM Tink Keyset.

---

## SECTION 2: USE CASE APIS

UseCases encapsulate individual business logic workflows. Located in `:feature:<name>:domain`.

---

### 2.1 `AnalyzeResumeUseCase`
* **Purpose:** Coordinates PDF parsing, text sanitization, prompt template assembly, AI matching, and saving ATS results.
* **Inputs:** `fileUri: Uri`, `jobDescription: String`
* **Outputs:** `operator fun invoke(fileUri: Uri, jobDescription: String): Flow<Result<ResumeAnalysis>>`
* **Errors:** `ValidationException`, `PdfParseException`, `ProviderException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Total time <= 5,000ms.
* **Security Considerations:** Purges temp files; sanitizes inputs.

### 2.2 `CalculateAtsScoreUseCase`
* **Purpose:** Pure algorithm to compute deterministic ATS keyword matching percentages.
* **Inputs:** `resumeKeywords: Set<String>`, `jobKeywords: Set<String>`
* **Outputs:** `operator fun invoke(resumeKeywords: Set<String>, jobKeywords: Set<String>): AtsScoreResult`
* **Errors:** None (deterministic pure computation).
* **Threading:** Executed inline on caller context or `Dispatchers.Default`.
* **Performance Expectations (SLA):** Execution time < 1ms.

### 2.3 `GenerateCoverLetterUseCase`
* **Purpose:** Orchestrates cover letter generation with tone selection and automatic database saving.
* **Inputs:** `resumeText: String`, `jobDescription: String`, `company: String`, `role: String`, `tone: LetterTone`
* **Outputs:** `operator fun invoke(...): Flow<Result<CoverLetter>>`
* **Errors:** `ProviderException`, `ValidationException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Latency <= 5,000ms.

### 2.4 `ConductInterviewUseCase`
* **Purpose:** Manages interactive conversation turns, windowed history maintenance, and prompt injection defense.
* **Inputs:** `sessionId: String`, `userMessage: String`
* **Outputs:** `operator fun invoke(sessionId: String, userMessage: String): Flow<Result<InterviewMessage>>`
* **Errors:** `ProviderException`, `SessionNotFoundException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** First byte <= 800ms.

### 2.5 `SearchJobsUseCase`
* **Purpose:** Validates query, checks local Room job cache, triggers remote scraper sync if cache is stale (>24h), and returns paginated result.
* **Inputs:** `query: JobSearchQuery`
* **Outputs:** `operator fun invoke(query: JobSearchQuery): Flow<Result<PageResponse<JobListing>>>`
* **Errors:** `ScraperException`, `NetworkException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Cache hit <= 30ms; Scraper miss <= 3,500ms.

### 2.6 `ScrapeJobsUseCase`
* **Purpose:** Bypasses local cache to force a fresh scraping run via Apify SDK / REST API.
* **Inputs:** `query: JobSearchQuery`, `actorId: String`
* **Outputs:** `operator fun invoke(query: JobSearchQuery, actorId: String): Result<List<JobListing>>`
* **Errors:** `ScraperException`, `AuthenticationException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Latency <= 4,000ms.

### 2.7 `TrackApplicationUseCase`
* **Purpose:** Creates or updates a tracked application record and schedules WorkManager follow-up notification reminders.
* **Inputs:** `application: JobApplication`
* **Outputs:** `operator fun invoke(application: JobApplication): Result<Long>`
* **Errors:** `DatabaseException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Execution time <= 50ms.

### 2.8 `GenerateCareerRoadmapUseCase`
* **Purpose:** Generates a structured multi-step career progression plan based on current skills and target position.
* **Inputs:** `targetRole: String`, `currentSkills: List<String>`
* **Outputs:** `operator fun invoke(...): Flow<Result<CareerRoadmap>>`
* **Errors:** `ProviderException`, `JsonParseException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Latency <= 4,500ms.

### 2.9 `ManageAiProviderUseCase`
* **Purpose:** Validates API key credentials against remote provider endpoint and switches active runtime provider.
* **Inputs:** `config: AiProviderConfig`
* **Outputs:** `operator fun invoke(config: AiProviderConfig): Result<Boolean>`
* **Errors:** `InvalidCredentialsException`, `NetworkException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Validation round-trip <= 1,500ms.

---

## SECTION 3: DAO APIS (ROOM CONTRACTS)

DAOs execute local SQLite operations inside `:core:database`. Direct SQL outside DAOs is strictly forbidden.

---

### 3.1 `ApplicationDao`

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
* **Purpose:** CRUD persistence operations for tracked job applications.
* **Inputs / Outputs:** Specified in Kotlin interface signature above.
* **Errors:** Throws `SQLiteException` / `SQLiteFullException`.
* **Threading:** `suspend` functions execute on `Dispatchers.IO`; `Flow` emits on caller DB dispatcher.
* **Performance Expectations (SLA):** Queries <= 10ms (indexed by `status`, `dateApplied`).

---

### 3.2 `AtsDao`

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
* **Purpose:** Stores and queries ATS evaluation reports.
* **Performance Expectations (SLA):** Latency <= 8ms.

---

### 3.3 `CoverLetterDao`

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
* **Purpose:** Manages saved cover letters in Room.
* **Performance Expectations (SLA):** Latency <= 10ms.

---

### 3.4 `RoadmapDao`

```kotlin
@Dao
interface RoadmapDao {
    @Transaction
    @Query("SELECT * FROM roadmaps LIMIT 1")
    fun getRoadmapWithSteps(): Flow<RoadmapWithStepsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmap(roadmap: RoadmapEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<RoadmapStepEntity>)

    @Query("UPDATE roadmap_steps SET isCompleted = :isCompleted WHERE id = :stepId")
    suspend fun updateStepCompletion(stepId: Long, isCompleted: Boolean)
}
```
* **Purpose:** Handles relational career roadmap entities and nested steps using transactions.
* **Performance Expectations (SLA):** Relational query <= 15ms.

---

### 3.5 `JobListingDao`

```kotlin
@Dao
interface JobListingDao {
    @Query("SELECT * FROM job_listings ORDER BY postedDate DESC LIMIT :limit OFFSET :offset")
    suspend fun getCachedJobs(offset: Int, limit: Int): List<JobListingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobListings(jobs: List<JobListingEntity>)

    @Query("DELETE FROM job_listings WHERE cachedTimestamp < :thresholdEpochMillis")
    suspend fun clearExpiredJobs(thresholdEpochMillis: Long)
}
```
* **Purpose:** Local cache persistence and cache expiry clearing for scraped job listings.
* **Performance Expectations (SLA):** Batch insert <= 25ms.

---

## SECTION 4: AI PROVIDER APIS

Interface contract for all remote LLM service implementations (`:core:network`).

---

### 4.1 Interface Contract: `AiProvider`

```kotlin
interface AiProvider {
    val id: ProviderId // GEMINI, OPENAI, GROQ, OPENROUTER, OLLAMA
    val capabilities: Set<AiCapability>

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

    fun streamChat(
        messages: List<AiMessage>,
        config: AiConfiguration
    ): Flow<String>

    suspend fun validateCredentials(apiKey: String, baseUrl: String?): Boolean
}
```

#### Provider Enums & Configuration:
```kotlin
enum class ProviderId { GEMINI, OPENAI, GROQ, OPENROUTER, OLLAMA }

enum class AiCapability { STREAMING, REASONING, SYSTEM_PROMPT, JSON_MODE }

data class AiConfiguration(
    val modelName: String,
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val maxTokens: Int = 2048,
    val responseFormat: ResponseFormat = ResponseFormat.TEXT,
    val systemPrompt: String? = null
)

enum class ResponseFormat { TEXT, JSON }
```

#### Implementations:
1. `GeminiProvider`: Wraps Google Generative AI Client SDK (`gemini-1.5-flash`).
2. `OpenAiProvider`: Standard REST calls to OpenAI `v1/chat/completions` (`gpt-4o-mini`).
3. `GroqProvider`: High-speed inference REST API (`llama-3.3-70b-versatile`).
4. `OpenRouterProvider`: Unified router API targeting diverse LLM endpoints.
5. `OllamaProvider`: Local network REST API for local offline LLMs (`http://localhost:11434`).

#### Method Details: `generateText()`
* **Inputs:** `prompt: String`, `config: AiConfiguration`
* **Outputs:** `Result<String>`
* **Errors:** `HttpException` (401 Unauthorized, 429 Rate Limit, 500 Internal Error), `SocketTimeoutException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Groq <= 1,200ms; Gemini <= 2,500ms; OpenAI <= 3,000ms.
* **Security Considerations:** API Key supplied via HTTP header `Authorization: Bearer <KEY>`.

---

## SECTION 5: JOB PROVIDER APIS

Interface contract for web scrapers and external job search engines (`:core:network`).

---

### 5.1 Interface Contract: `JobProvider`

```kotlin
interface JobProvider {
    val providerId: String
    suspend fun searchJobs(query: JobSearchQuery): Result<List<JobListing>>
    suspend fun fetchJobDetails(jobId: String): Result<JobListing>
    suspend fun validateScraperConfig(token: String, actorId: String): Boolean
}

data class JobSearchQuery(
    val keywords: String,
    val location: String,
    val isRemoteOnly: Boolean = false,
    val page: Int = 1,
    val limit: Int = 20
)
```

#### Implementations:
1. `ApifyJobProvider`: Dispatches asynchronous web scraping runs to configured Apify Actors (LinkedIn Scraper, Indeed Scraper, Google Jobs Scraper) and fetches dataset items upon run completion.
2. `DirectJobProvider`: Directly queries public job search REST APIs (e.g., Remotive, Jobacy) with structured JSON responses.

#### Method Details: `searchJobs()`
* **Inputs:** `query: JobSearchQuery`
* **Outputs:** `Result<List<JobListing>>`
* **Errors:** `ApifyActorException`, `NetworkException`, `ParseException`.
* **Threading:** `Dispatchers.IO`.
* **Performance Expectations (SLA):** Direct API <= 1,500ms; Apify Actor Run <= 4,000ms.
* **Security Considerations:** Apify API Token stored in Keystore; HTTPS strict transport security enforced.

---

## SECTION 6: MANAGER APIS

System managers coordinate operational state, failover policies, and cross-cutting infrastructure (`:core:network`, `:core:datastore`).

---

### 6.1 `AiProviderManager`
* **Purpose:** Controls the active `AiProvider`, manages dynamic runtime provider switching, executes automatic fallback to secondary provider if primary fails, and records health statistics.
* **Methods:**
  * `suspend fun getActiveProvider(): AiProvider`
  * `suspend fun switchProvider(providerId: ProviderId): Result<Unit>`
  * `suspend fun executeWithFallback(block: suspend (AiProvider) -> Result<String>): Result<String>`
* **Threading:** `Dispatchers.IO`.
* **Security:** Thread-safe state mutation via `Mutex`.

### 6.2 `JobProviderManager`
* **Purpose:** Manages scraper actor dispatching, job search caching policies, and request throttling.
* **Methods:**
  * `suspend fun getActiveJobProvider(): JobProvider`
  * `suspend fun setScraperActor(actorId: String): Result<Unit>`
* **Threading:** `Dispatchers.IO`.

### 6.3 `ConversationManager`
* **Purpose:** Manages interview dialogue windowing, sliding context truncation, and token budget calculations.
* **Methods:**
  * `fun appendUserMessage(sessionId: String, text: String): InterviewMessage`
  * `fun getWindowedHistory(sessionId: String, maxTokens: Int): List<AiMessage>`
  * `fun clearSession(sessionId: String)`
* **Threading:** Main / IO safe.

### 6.4 `PromptManager`
* **Purpose:** Assembles, formats, and sanitizes prompt templates for ATS analysis, Cover Letter generation, and Mock Interviews.
* **Methods:**
  * `fun buildAtsPrompt(resumeText: String, jobDescription: String): String`
  * `fun buildCoverLetterPrompt(resumeText: String, jobDescription: String, tone: LetterTone): String`
  * `fun buildInterviewSystemPrompt(role: String, company: String, difficulty: InterviewDifficulty): String`
* **Threading:** CPU execution on calling dispatcher.

### 6.5 `CredentialManager`
* **Purpose:** Securely reads, writes, and rotates API keys using Android Keystore and Encrypted DataStore.
* **Methods:**
  * `suspend fun getApiKey(providerId: ProviderId): String?`
  * `suspend fun saveApiKey(providerId: ProviderId, apiKey: String)`
  * `suspend fun clearCredentials()`
* **Security:** Keys encrypted with AES-256-GCM.

### 6.6 `HealthMonitorManager`
* **Purpose:** Performs periodic background heartbeat checks on configured providers and tracks latency and error rates.
* **Methods:**
  * `fun observeHealthStatus(): Flow<Map<ProviderId, HealthStatus>>`
  * `suspend fun checkHealth(providerId: ProviderId): HealthStatus`

### 6.7 `BackupManager`
* **Purpose:** Handles encrypted JSON/database export and import operations for user data backup and restoration.
* **Methods:**
  * `suspend fun exportBackup(outputStream: OutputStream, password: String): Result<Unit>`
  * `suspend fun importBackup(inputStream: InputStream, password: String): Result<Unit>`

---

## SECTION 7: REGISTRY APIS

Registries maintain lookup tables for available system implementations (`:core:network`).

---

### 7.1 `AiProviderRegistry`
* **Purpose:** Thread-safe registry mapping `ProviderId` to `AiProvider` instances.
* **Methods:**
  * `fun registerProvider(provider: AiProvider)`
  * `fun getProvider(id: ProviderId): AiProvider?`
  * `fun getAvailableProviders(): List<AiProvider>`

### 7.2 `JobProviderRegistry`
* **Purpose:** Maps provider keys to `JobProvider` instances.
* **Methods:**
  * `fun registerProvider(provider: JobProvider)`
  * `fun getProvider(id: String): JobProvider?`

### 7.3 `CapabilityRegistry`
* **Purpose:** Maps AI model names to supported features (e.g. streaming, JSON mode).
* **Methods:**
  * `fun getCapabilities(providerId: ProviderId, modelName: String): Set<AiCapability>`
  * `fun supportsStreaming(providerId: ProviderId, modelName: String): Boolean`

### 7.4 `ActorRegistry`
* **Purpose:** Stores pre-configured Apify actor identifiers for target platforms (LinkedIn, Indeed, Glassdoor).
* **Methods:**
  * `fun getActorForPlatform(platform: String): String`
  * `fun registerCustomActor(platform: String, actorId: String)`

---

## SECTION 8: FACTORY APIS

Factories instantiate complex system services and providers (`:core:network`).

---

### 8.1 `AiProviderFactory`
* **Purpose:** Instantiates concrete `AiProvider` instances based on `AiProviderConfig`.
* **Methods:**
  * `fun createProvider(config: AiProviderConfig): AiProvider`

### 8.2 `JobProviderFactory`
* **Purpose:** Constructs `JobProvider` instances based on `JobScraperConfig`.
* **Methods:**
  * `fun createJobProvider(config: JobScraperConfig): JobProvider`

### 8.3 `PromptFactory`
* **Purpose:** Creates structured `AiMessage` dialogue sequences with system instruction context.
* **Methods:**
  * `fun createInterviewPromptSequence(history: List<InterviewMessage>, systemInstruction: String): List<AiMessage>`

### 8.4 `ServiceFactory`
* **Purpose:** Constructs Retrofit service interfaces with OkHttp logging, timeout, and authentication interceptors.
* **Methods:**
  * `<T> fun createRetrofitService(serviceClass: Class<T>, baseUrl: String, client: OkHttpClient): T`

---

## SECTION 9: DTO SPECIFICATIONS

Data Transfer Objects (DTOs) for network and AI response deserialization. Annotates properties with `@kotlinx.serialization.Serializable` and `@SerialName`.

---

### 9.1 `ResumeAnalysisDto`
```kotlin
@Serializable
data class ResumeAnalysisDto(
    @SerialName("overall_score") val overallScore: Int,
    @SerialName("matching_keywords") val matchingKeywords: List<String> = emptyList(),
    @SerialName("missing_keywords") val missingKeywords: List<String> = emptyList(),
    @SerialName("suggestions") val suggestions: List<String> = emptyList(),
    @SerialName("match_summary") val matchSummary: String = ""
)
```

### 9.2 `AtsResultDto`
```kotlin
@Serializable
data class AtsResultDto(
    @SerialName("score") val score: Int,
    @SerialName("resume_name") val resumeName: String,
    @SerialName("missing_keywords") val missingKeywords: List<String>,
    @SerialName("feedback") val feedback: String,
    @SerialName("date_epoch") val dateEpoch: Long = System.currentTimeMillis()
)
```

### 9.3 `CoverLetterDto`
```kotlin
@Serializable
data class CoverLetterDto(
    @SerialName("company") val company: String,
    @SerialName("role") val role: String,
    @SerialName("content") val content: String,
    @SerialName("tone") val tone: String
)
```

### 9.4 `InterviewFeedbackDto`
```kotlin
@Serializable
data class InterviewFeedbackDto(
    @SerialName("overall_score") val overallScore: Int,
    @SerialName("strengths") val strengths: List<String> = emptyList(),
    @SerialName("improvements") val improvements: List<String> = emptyList(),
    @SerialName("detailed_summary") val detailedSummary: String = ""
)
```

### 9.5 `ApifyJobScraperDto`
```kotlin
@Serializable
data class ApifyJobScraperDto(
    @SerialName("id") val id: String,
    @SerialName("position_name") val positionName: String,
    @SerialName("company_name") val companyName: String,
    @SerialName("location") val location: String,
    @SerialName("salary") val salary: String? = null,
    @SerialName("url") val url: String,
    @SerialName("posted_at") val postedAt: String? = null,
    @SerialName("description") val description: String = ""
)
```

---

## SECTION 10: REQUEST MODELS

Data models sent to remote services or AI prompt configurations.

---

### 10.1 `ResumeAnalysisRequest`
```kotlin
@Serializable
data class ResumeAnalysisRequest(
    @SerialName("resume_text") val resumeText: String,
    @SerialName("job_description") val jobDescription: String
)
```

### 10.2 `CoverLetterRequest`
```kotlin
@Serializable
data class CoverLetterRequest(
    @SerialName("resume_text") val resumeText: String,
    @SerialName("job_description") val jobDescription: String,
    @SerialName("tone") val tone: String
)
```

### 10.3 `InterviewPromptRequest`
```kotlin
@Serializable
data class InterviewPromptRequest(
    @SerialName("role") val role: String,
    @SerialName("company") val company: String,
    @SerialName("difficulty") val difficulty: String,
    @SerialName("conversation_history") val history: List<AiMessageDto>
)
```

### 10.4 `JobSearchQueryRequest`
```kotlin
@Serializable
data class JobSearchQueryRequest(
    @SerialName("keywords") val keywords: String,
    @SerialName("location") val location: String,
    @SerialName("is_remote") val isRemote: Boolean,
    @SerialName("page") val page: Int = 1,
    @SerialName("limit") val limit: Int = 20
)
```

---

## SECTION 11: RESPONSE MODELS

Models returned by internal APIs and network wrapper endpoints.

---

### 11.1 `JobSearchPageResponse`
```kotlin
@Serializable
data class PageResponse<T>(
    @SerialName("items") val items: List<T>,
    @SerialName("page") val page: Int,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_items") val totalItems: Int,
    @SerialName("has_next") val hasNext: Boolean
)
```

### 11.2 `HealthCheckResponse`
```kotlin
@Serializable
data class HealthCheckResponse(
    @SerialName("provider_id") val providerId: String,
    @SerialName("is_healthy") val isHealthy: Boolean,
    @SerialName("latency_ms") val latencyMs: Long,
    @SerialName("error_message") val errorMessage: String? = null
)
```

---

## SECTION 12: ERROR PAYLOADS & CONTRACT

Standardized error classification and JSON error payload structures across all modules.

---

### 12.1 Error Hierarchy
```kotlin
sealed class ApiError : Exception() {
    data class NetworkError(val code: Int, override val message: String) : ApiError()
    data class ProviderError(val providerId: String, val statusCode: Int, override val message: String) : ApiError()
    data class ValidationError(val field: String, override val message: String) : ApiError()
    data class DatabaseError(override val message: String, val cause: Throwable? = null) : ApiError()
    data class RateLimitError(val retryAfterSeconds: Long) : ApiError()
    data class AuthError(override val message: String) : ApiError()
    data class ParseError(override val message: String) : ApiError()
    data class UnknownError(override val message: String) : ApiError()
}
```

### 12.2 Standardized JSON Error Payload
```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Quota exceeded for Gemini 1.5 Flash. Please try again in 30 seconds.",
    "status_code": 429,
    "details": {
      "provider": "GEMINI",
      "retry_after_seconds": 30
    }
  }
}
```

---

## SECTION 13: JSON SCHEMAS

Complete JSON Schema Draft-07 compliant definitions for structured AI output parsing.

---

### 13.1 `ResumeAnalysisResponse` JSON Schema
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "ResumeAnalysisResponse",
  "type": "object",
  "properties": {
    "overall_score": {
      "type": "integer",
      "minimum": 0,
      "maximum": 100
    },
    "matching_keywords": {
      "type": "array",
      "items": { "type": "string" }
    },
    "missing_keywords": {
      "type": "array",
      "items": { "type": "string" }
    },
    "suggestions": {
      "type": "array",
      "items": { "type": "string" }
    },
    "match_summary": {
      "type": "string"
    }
  },
  "required": ["overall_score", "matching_keywords", "missing_keywords", "suggestions", "match_summary"]
}
```

### 13.2 `InterviewFeedbackResponse` JSON Schema
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "InterviewFeedbackResponse",
  "type": "object",
  "properties": {
    "overall_score": {
      "type": "integer",
      "minimum": 0,
      "maximum": 100
    },
    "strengths": {
      "type": "array",
      "items": { "type": "string" }
    },
    "improvements": {
      "type": "array",
      "items": { "type": "string" }
    },
    "detailed_summary": {
      "type": "string"
    }
  },
  "required": ["overall_score", "strengths", "improvements", "detailed_summary"]
}
```

---

## SECTION 14: SERIALIZATION RULES

1. **Kotlinx Serialization Engine:** Single instance configured in `:core:network` and `:core:datastore`:
   ```kotlin
   val DefaultJson = Json {
       ignoreUnknownKeys = true
       isLenient = true
       encodeDefaults = true
       coerceInputValues = true
       prettyPrint = false
   }
   ```
2. **Naming Naming Strategy:** Snake_case used for remote API DTOs (`@SerialName("user_id")`); camelCase used for local preferences.
3. **Polymorphism:** Sealed interfaces (`ProviderId`, `ApplicationStatus`) serialize via explicit string identifiers or custom serializers.
4. **Markdown Tag Stripping:** AI responses wrapped in ```json ... ``` tags must be stripped via regex before passing to `Json.decodeFromString()`:
   ```kotlin
   fun String.cleanJson(): String =
       this.replace(Regex("^```json\\s*"), "")
           .replace(Regex("\\s*```$"), "")
           .trim()
   ```

---

## SECTION 15: VERSIONING

1. **API Endpoints:** Versioned using URL paths (e.g. `https://api.apify.com/v2/acts/...`).
2. **Database Versioning:** Managed via Room migrations. Version integer increments sequentially (`1`, `2`, `3`). Every schema change requires a tested `Migration(old, new)`.
3. **Provider API Version Headers:** Anthropic (`anthropic-version: 2023-06-01`), OpenAI (`OpenAI-Beta: assist=v2`).
4. **Deprecation Policy:** Deprecated methods marked with `@Deprecated(message, replaceWith, level = DeprecationLevel.WARNING)` for one minor release before removal.

---

## SECTION 16: COMPATIBILITY

1. **Android OS Compatibility:**
   * minSdk = 26 (Android 8.0 Oreo), targetSdk = 35 (Android 15).
   * API 35 specific calls (`PdfRenderer.textContents`) guarded with explicit SDK checks (`Build.VERSION.SDK_INT >= 35`), falling back to PDFBox Android on API 26-34 devices.
2. **Forward/Backward DTO Compatibility:** All DTO fields have default values or nullable types to ensure new API response fields do not crash older client builds (`ignoreUnknownKeys = true`).
3. **Offline Fallback:** When internet connectivity is absent or AI providers fail, `MockAiService` or cached Room database content is served seamlessly.

---

## SECTION 17: PAGINATION

1. **Page-Based Pagination Contract:**
   ```kotlin
   data class JobSearchQuery(
       val keywords: String,
       val location: String,
       val page: Int = 1,
       val limit: Int = 20
   )
   ```
2. **Metadata Contract:** Repositories emit `PageResponse<T>` containing `page`, `totalPages`, `totalItems`, and `hasNext: Boolean`.
3. **LazyColumn Integration:** ViewModels maintain scroll offset and append new page items to `StateFlow<List<JobListing>>` when `hasNext` is true and user scrolls within 3 items of column end.

---

## SECTION 18: STREAMING PROTOCOL

1. **Server-Sent Events (SSE):** AI text streaming uses SSE or OkHttp `ResponseBody.byteStream()` chunk reading.
2. **Flow Emission:** Provider streams chunks as `Flow<String>`. ViewModels collect chunks and accumulate text in UI state:
   ```kotlin
   fun streamText(prompt: String, config: AiConfiguration): Flow<String> = channelFlow {
       val response = okHttpClient.newCall(request).execute()
       val source = response.body?.source() ?: return@channelFlow
       while (!source.exhausted()) {
           val line = source.readUtf8Line() ?: continue
           if (line.startsWith("data: ")) {
               val chunk = line.removePrefix("data: ")
               send(chunk)
           }
       }
   }.flowOn(Dispatchers.IO)
   ```
3. **Backpressure Strategy:** `Flow` uses default buffering (`BufferOverflow.SUSPEND`) to ensure slow UI consumers do not drop stream tokens.
4. **Cancellation:** Cancelling the collecting Coroutine Job immediately closes the underlying OkHttp call/socket.

---

## SECTION 19: VALIDATION

1. **Input Validation Rules:**
   * **PDF Uploads:** MIME type must be `application/pdf`; file size <= 10MB.
   * **API Keys:** Non-blank; minimum length 20 chars; regex matching provider format (e.g. `^AIzaSy[A-Za-z0-9_-]{33}$` for Gemini).
   * **Job Query:** Keywords length between 2 and 100 characters.
   * **URL Validation:** Job URLs must match `android.util.Patterns.WEB_URL`.
2. **Prompt Injection Defense:** User inputs in resume text, job descriptions, and interview chat are sanitized:
   * Strips harmful prompt override instructions (`"Ignore previous instructions"`).
   * Enforces structural system prompt isolation in `AiMessage` system roles.

---

## SECTION 20: EXAMPLES

Complete, runnable Kotlin integration examples demonstrating primary API flows.

---

### 20.1 End-to-End Resume Analysis Flow Example

```kotlin
class AnalyzeResumeExample @Inject constructor(
    private val pdfTextExtractor: PdfTextExtractor,
    private val aiProviderManager: AiProviderManager,
    private val atsDao: AtsDao,
    private val json: Json
) {
    suspend fun executeAnalysis(fileUri: Uri, jobDescription: String): Result<AtsResult> = withContext(Dispatchers.IO) {
        try {
            // 1. Extract text safely across API 26-35
            val resumeText = pdfTextExtractor.extractText(fileUri)
            if (resumeText.isBlank()) {
                return@withContext Result.Error(ApiError.ValidationError("fileUri", "PDF text extraction returned empty string"))
            }

            // 2. Build sanitized prompt
            val prompt = """
                Analyze the following resume against the job description.
                Return ONLY a JSON object matching this schema:
                {"overall_score": Int, "matching_keywords": ["..."], "missing_keywords": ["..."], "suggestions": ["..."], "match_summary": "..."}
                
                RESUME:
                $resumeText
                
                JOB DESCRIPTION:
                $jobDescription
            """.trimIndent()

            // 3. Invoke active AI provider
            val provider = aiProviderManager.getActiveProvider()
            val config = AiConfiguration(modelName = "gemini-1.5-flash", responseFormat = ResponseFormat.JSON)
            val rawResponse = provider.generateText(prompt, config).getOrThrow()

            // 4. Clean and parse JSON response
            val cleanedJson = rawResponse.replace(Regex("^```json\\s*"), "").replace(Regex("\\s*```$"), "").trim()
            val dto = json.decodeFromString<ResumeAnalysisDto>(cleanedJson)

            // 5. Persist ATS result to local Room database
            val entity = AtsResultEntity(
                score = dto.overallScore,
                date = System.currentTimeMillis(),
                resumeName = "Resume_${System.currentTimeMillis()}",
                missingKeywords = dto.missingKeywords.joinToString(", "),
                feedback = dto.matchSummary
            )
            val insertedId = atsDao.insertAtsResult(entity)

            val domainResult = AtsResult(
                id = insertedId,
                score = dto.overallScore,
                date = entity.date,
                resumeName = entity.resumeName,
                missingKeywords = dto.missingKeywords,
                feedback = dto.matchSummary,
                matchingKeywords = dto.matchingKeywords
            )

            Result.Success(domainResult)
        } catch (e: Exception) {
            Result.Error(ApiError.UnknownError(e.message ?: "Analysis failed"))
        }
    }
}
```

---

### 20.2 Interactive Mock Interview Streaming Example

```kotlin
class InterviewStreamExample @Inject constructor(
    private val aiProviderManager: AiProviderManager
) {
    fun streamInterviewerReply(
        conversationHistory: List<AiMessage>,
        userResponse: String
    ): Flow<String> = flow {
        val activeProvider = aiProviderManager.getActiveProvider()
        val updatedHistory = conversationHistory + AiMessage(role = AiRole.USER, content = userResponse)

        val config = AiConfiguration(
            modelName = "llama-3.3-70b-versatile",
            temperature = 0.7f,
            systemPrompt = "You are an expert technical interviewer conducting a mock software engineering interview."
        )

        activeProvider.streamChat(updatedHistory, config)
            .collect { chunk ->
                emit(chunk)
            }
    }.flowOn(Dispatchers.IO)
}
```

---

### 20.3 Live Job Scraping Example

```kotlin
class JobScrapeExample @Inject constructor(
    private val jobProviderManager: JobProviderManager,
    private val jobListingDao: JobListingDao
) {
    suspend fun searchAndCacheJobs(keywords: String, location: String): Result<List<JobListing>> = withContext(Dispatchers.IO) {
        try {
            val provider = jobProviderManager.getActiveJobProvider()
            val query = JobSearchQuery(keywords = keywords, location = location, page = 1)

            val scrapeResult = provider.searchJobs(query)
            if (scrapeResult is Result.Success) {
                val jobs = scrapeResult.data
                // Cache listings locally in Room
                val entities = jobs.map { job ->
                    JobListingEntity(
                        id = job.id,
                        title = job.title,
                        company = job.company,
                        location = job.location,
                        salaryRange = job.salaryRange,
                        jobType = job.jobType.name,
                        isRemote = job.isRemote,
                        description = job.description,
                        url = job.url,
                        sourceProvider = job.sourceProvider,
                        postedDate = job.postedDate
                    )
                }
                jobListingDao.insertJobListings(entities)
            }
            scrapeResult
        } catch (e: Exception) {
            Result.Error(ApiError.NetworkError(500, e.message ?: "Job search failed"))
        }
    }
}
```

---

### 20.4 Dynamic AI Provider Switcher Example

```kotlin
class ProviderSwitchExample @Inject constructor(
    private val credentialManager: CredentialManager,
    private val aiProviderManager: AiProviderManager,
    private val settingsRepository: SettingsRepository
) {
    suspend fun configureAndSwitchProvider(
        providerId: ProviderId,
        apiKey: String,
        modelName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Store encrypted key
            credentialManager.saveApiKey(providerId, apiKey)

            // 2. Validate credentials with remote endpoint
            val provider = aiProviderManager.getActiveProvider()
            val isValid = provider.validateCredentials(apiKey, null)

            if (!isValid) {
                return@withContext Result.Error(ApiError.AuthError("Invalid API key for $providerId"))
            }

            // 3. Save new config to preferences
            val config = AiProviderConfig(
                providerId = providerId.name,
                apiKey = apiKey,
                selectedModel = modelName
            )
            settingsRepository.saveAiProviderConfig(config)

            // 4. Switch active provider instance in manager
            aiProviderManager.switchProvider(providerId)

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ApiError.UnknownError("Failed to switch provider: ${e.message}"))
        }
    }
}
```

---

*End of Master API Contract Specification for Aviance.*
