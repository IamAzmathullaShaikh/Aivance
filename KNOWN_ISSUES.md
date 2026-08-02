# AiVance Known Issues

This document tracks known limitations and defects at the **v1.0.0** release. Issues are categorized by severity and have mitigation plans.

## Severity Legend
- 🔴 **High** — impacts data integrity, security, or core workflows.
- 🟡 **Medium** — degrades an experience or a secondary feature.
- 🟢 **Low** — cosmetic or niche.

---

## 🔴 High

### H-01 — ~~Legacy plaintext PII cleanup is pending (DB v21)~~ ✅ RESOLVED
- **Resolved in Phase 5**: `SecurityMigrationWorker` now performs a real scan of all `provider_configurations` rows for plaintext secret values (keys matching `apiKey`, `token`, `secret`, etc.), migrates them to `SecretsManager` encrypted storage, and strips them from the DB settings map. Worker is idempotent via `ExistingWorkPolicy.KEEP`.

### H-02 — ~~Keystore-bound recovery~~ ✅ RESOLVED
- **Resolved in Phase 6**: `BackupExporter` and `BackupImporter` implemented in `core:util`. Users can export an encrypted `.aivance_backup` file containing all Room database tables (resumes, jobs, cover letters, applications, profile) using PBKDF2 + AES-256-GCM encryption, and restore it on any device via SAF file picker.

## 🟡 Medium

### M-01 — ~~Recruiter persistence uses mock logic~~ ✅ RESOLVED
- **Resolved in Phase 4/5 audit**: `RecruiterIntelligenceRepositoryImpl` is fully backed by `RecruiterDao` (Room). `findRecruiters()` calls the Hunter.io enrichment provider, auto-saves results via `recruiterDao.insertRecruiter()` + `recruiterDao.insertContact()`, and `getRecruitersForCompany()` returns a live Room `Flow`. The stale mock annotation was incorrect.

### M-02 — ~~Keyed free job providers dormant by default~~ ✅ RESOLVED
- **Resolved in Phase 6**: Runtime configuration for `AdzunaProvider` and `USAJobsProvider` enabled via `ProviderManagementViewModel` and `saveProviderConfig`. Users can enter Adzuna App ID / API Key and USAJobs API Key dynamically at runtime; `reconfigure()` re-hydrates credentials instantly.

### M-03 — Interview analytics timeline incomplete
- **Area**: `feature:analytics`.
- **Description**: Interview improvement timeline and achievement cards require accumulated analytics history.
- **Impact**: Charts render empty/partial for new users.
- **Mitigation**: Data accumulates over time; honest empty states are shown.

## 🟢 Low

### L-01 — ~~Deprecation warnings in tests~~ ✅ RESOLVED
- **Resolved in Phase 6**: Updated test suites and dependencies for `ResumeEngineViewModelTest` to use non-deprecated model constructors and mocked exporters.

### L-02 — Tautological initial-state tests
- **Area**: Several ViewModel tests (e.g., `loading state on init`).
- **Description**: Some tests assert only the trivial initial state value.
- **Impact**: Low signal, no false failures.
- **Mitigation**: Strengthen over time; not release-blocking.

---

## v2 Redesign — Deferred / Known Items

### V2-01 — Dashboard saved-jobs count waits for a real JobRepository read
- **Area**: `DashboardViewModel`.
- **Description**: Career HQ shows `savedJobs` from `JobRepository.getSavedJobs()`; the count is 0 until a job provider is configured and jobs are bookmarked. Intentional (no hardcoded data), but the card reads empty for new users.
- **Mitigation**: Acceptable — consistent with the no-fake-data rule; empty states are honest.

### V2-02 — ~~Assistant attachment affordances are visual-only~~ ✅ RESOLVED
- **Resolved in Phase 6**: Mic (SpeechRecognizer with Accompanist RECORD_AUDIO permission request), Attach file (OpenDocument contract), and Photo (PickVisualMedia contract) are fully wired in `AssistantInputBar` (`AssistantScreen.kt`).

### V2-03 — WorkerTests needed mock stubs for notification managers
- **Area**: `app` `WorkerTests`.
- **Description**: `UploadManager`/`DownloadManager` init blocks create `NotificationChannel`s; the JVM unit tests were updated to stub `getSystemService(NOTIFICATION_SERVICE)` with a typed mock (they previously crashed with NPE/CCE after the SDK guard removal).
- **Impact**: None — tests are green.

### V2-04 — Lint suppressions for guarded notification calls
- **Area**: `DownloadManager`, `UploadManager`, `NotificationHelper`, `AndroidManifest`.
- **Description**: `@SuppressLint("MissingPermission")` on `notify()` call sites that are already runtime-guarded (`checkSelfPermission` / `try-catch`), and `tools:ignore="AppLinkUrlError"` for the intentional `aivance://` custom-scheme deep links (not Android App Links).
- **Impact**: Lint is clean (0 errors); suppressions are justified in KDoc.

### V2-09 — ~~DOCX export deferred~~ ✅ RESOLVED
- **Resolved in Phase 6**: Native `.docx` document generation implemented via Apache POI in `DocxExporter` (`core:util`), generating fully structured Microsoft Word documents from `ResumeVersion` sections and exposing them via FileProvider.

### V2-10 — ~~Google Sign-In requires Web Client ID configuration~~ ✅ RESOLVED
- **Resolved in Phase 5**: Web Client ID `433186935073-elau2khhj78koof6puo1mrk2hifa26jt.apps.googleusercontent.com` injected into `google-services.json` for both release and debug variants (`client_type: 3`). `processDebugGoogleServices` confirmed `default_web_client_id` is now generated. Google Sign-In via Credential Manager is fully operational.

### V2-11 — ~~Certificate pinning remains disabled~~ ✅ RESOLVED
- **Resolved in Phase 6**: Updated `CertificatePinningInterceptor` with real leaf SHA-256 Public Key Info pins and CA backup pins for `api.groq.com`, `api.openai.com`, `openrouter.ai`, `remoteok.com`, `remotive.com`, and `api.apify.com`.

### V2-12 — Integration tests require local.properties API keys
- **Area**: `app/src/androidTest/java/.../integration/`.
- **Description**: Real-API integration tests require `apifyApiKey`, `groqApiKey`, etc., in local.properties.
- **Mitigation**: Documented in developer guide; CI/CD pipeline must inject secrets.

### V2-13 — ~~Camera scan in Resume Engine has no OCR~~ ✅ RESOLVED
- **Resolved in Phase 6**: Camera capture now passes `InputImage` to ML Kit Text Recognition (`TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)`), emitting `ResumeEngineEvent.ImportOcrText` to build and preview parsed resume sections directly.

---

## Resolved
- **H-01** (SecurityMigrationWorker) — resolved in Phase 5 with real plaintext scan + SecretsManager migration.
- **M-01** (Recruiter persistence) — confirmed already resolved; `RecruiterIntelligenceRepositoryImpl` uses Room `RecruiterDao` throughout.
- **V2-10** (Google Sign-In Web Client ID) — resolved in Phase 5; Web Client ID injected into `google-services.json`.
- V2-05 (PDF export) — resolved in Phase 4 with `PdfExporter` using Android's `PdfDocument` API.
- TrackEventUseCase no-op — resolved in Phase 4, now persists events to Room via `AnalyticsEngine`.
- Silent error swallowing — resolved in Phase 4, all `Result.Failure` branches across ViewModels now emit visible error states/snackbars.
- Resume Engine compile errors (Phase 3) — missing `ResumeEngineScreen` import in the nav graph, nullable camera URI for `TakePicture`, `itemsIndexed` missing import, dead `@OptIn(ExperimentalFoundationApi::class)` — fixed.
- Resume Engine state-machine dead-ends — `enterError()` now normalizes `lastStableState` (Parsing→Import, Optimizing→clear spinner, Saving→Optimizing) so back/retry after any failure restores a usable step; `retry()` only re-imports for Import/Parsing failures; `finish()` resets the engine to Import.
- `CoverLetterViewModelTest` / `JobDetailsViewModelTest` constructor mismatches (missing `exportCoverLetterUseCase`, `jobRepository`, `applicationWorkflowRepository` mocks) — updated to the current constructor signatures.
- `AtsViewModelTest` analyze tests used a 23-char JD that never crossed the live-reactive `jd.length > 50` gate — switched to a realistic long JD.
- Logging loop between `StructuredTimberTree` and `TelemetryEngineImpl` (ADR 003) — fixed.
- Stale tests referencing deleted use cases across 6 feature modules — fixed in Phase 13.
- Mock WebServer tests for Arbeitnow/Jobicy/Apify — green.
- Legacy `FakeDashboardRepository` deleted from `feature:dashboard` main source set — replaced by the real `DashboardRepositoryImpl` aggregation in the Career HQ rewrite.
