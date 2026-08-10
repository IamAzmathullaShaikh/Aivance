# Changelog

All notable changes to AiVance are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added — Resumable Background Model Downloads via WorkManager (2026-08-08)
- **Download survives app backgrounding**: the on-device Gemma model download now
  runs in a foreground `@HiltWorker` (`GemmaModelDownloadWorker`, `feature:profile`)
  enqueued through WorkManager with `CONNECTED` network constraint and exponential
  backoff (30s, 5 attempts). Provider Management's *Download model* button
  enqueues it; the screen streams live progress via `ModelDownloadScheduler.observe()`
  and refreshes when the work succeeds/fails — including when the app was
  backgrounded or killed mid-download.
- **Ongoing progress notification**: `ModelDownloadNotifier` owns a dedicated
  `model_downloads` channel; the worker promotes itself to a foreground service
  (`dataSync` type, `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC`
  permissions declared in the library manifest) and refreshes the notification
  (throttled to 1/s) with percent + determinate progress bar; posts a terminal
  success/failure notification. Strings in EN + HI.
- **Resumable + retryable transfers**: `OkHttpModelFileDownloader` now writes to a
  `.part` file and resumes from it via HTTP `Range: bytes=` on retry (validates
  206 responses), so a network drop re-downloads only the remaining bytes.
  Transient failures (no network, interrupted transfer, 5xx/408/429) return
  `retry()`; permanent 4xx return `failure()`.
- **Cancellation**: deleting the model cancels any in-flight work so it cannot
  recreate the file immediately after removal.
- **Tests**: `OkHttpModelFileDownloaderTest` (6 MockWebServer cases incl.
  mid-stream interruption + Range resume), `GemmaModelDownloadWorkerTest`
  (7 Robolectric + work-testing cases: success/progress, already-ready
  short-circuit, permanent vs transient classification, offline defer, missing
  provider, compact URL forwarding), updated `ProviderManagementViewModelTest`
  (enqueue + observe-driving). 52 tests green across the touched modules.

### Added — Training-Ready JSONL Corpus for All 41 Skills (2026-08-08)
- **`skills-pack/training/`**: every skill in the pack exported to strict JSONL
  for fine-tuning another model — `skills_corpus.jsonl` (record format:
  frontmatter `name`+`description` as the `system` prompt, SKILL.md body as
  `instructions`, plus a distinct `trigger`/`task` pair per skill) and
  `skills_chat.jsonl` (same content as `{system, user, assistant}` messages
  for direct SFT). Both carry `skill` + `tier` (uesf-core/community/original/
  learned) so the corpus is filterable by provenance.
- **Regenerable**: new `skills-pack/tools/export_training_jsonl.py`
  (stdlib-only) scans every `SKILL.md` under `skills-pack/`, writes both
  corpora, and self-verifies line counts + schema before exiting — safe to run
  in CI after any skill edit. `training/README.md` documents usage for
  Hugging Face datasets / Axolotl / LLaMA-Factory and which tier is the
  cleanest training signal; MANIFEST directory layout updated.

### Added — Feature Interlinking: Saved Jobs → Resume / Tracker / Assistant (2026-08-08)
- **Per-card actions on Saved Jobs**: each saved job now offers *Create tailored
  resume* (jumps to the Resume Engine with the job description preloaded as the
  ATS-scan JD — `Destination.ResumeEngine(jobDescription)` + a new
  `SetInitialJobDescription` engine event that flows into the ATS step),
  *Track application* (opens the Pipeline with the job pre-selected — new
  `Destination.TrackApplication(jobId)`; the tracker selects the existing
  application for it via id normalization, or pre-fills the Add dialog with
  the job's company/role so one tap adds it), and *Ask AI* (opens the global
  assistant overlay with the job as context).
- **Assistant with job context from any screen**: new `AssistantJobContext`
  domain model; `AppShellState`/`LocalAppShellState` moved into
  `core:designsystem` so scaffolds open the assistant overlay globally
  (`AivanceWorkspaceScaffold`'s AI action now toggles the overlay by default);
  `PromptOrchestrator.buildCopilotPrompt(…, jobContext)` renders a CURRENT JOB
  CONTEXT block (title/company/description) so answers are tailored to the
  role; `AssistantScreen(initialJobContext)` + `AssistantViewModel.setJobContext`
  thread it into the next prompt.
- **Tests**: Destination (ResumeEngine/TrackApplication routes), ResumeEngine
  (preloaded JD carried into ATS step), Tracker (`trackJob` selects existing vs
  pre-fills pending, clears pending), Assistant (job context passed into the
  orchestrated prompt). 97 unit tests green across the touched modules.

### Added — Learned Skills Tier + Updated Skills Pack (2026-08-08)
- **16 new "learned" skills** (`.agents/skills/` + `skills-pack/learned/`),
  original model-agnostic syntheses distilled from 11 public skill repos
  (anthropics/skills, obra/superpowers, mattpocock/skills,
  emilkowalski/skills, MiniMax-AI/skills, MengTo/Skills, slavingia/skills,
  google/skills, vercel-labs/skills, VoltAgent/awesome-openclaw-skills,
  multica-ai/andrej-karpathy-skills): `coding-discipline`,
  `plan-driven-implementation`, `root-cause-debugging`,
  `verification-before-completion`, `brainstorm-before-build`,
  `writing-for-agents`, `skill-lifecycle`, `motion-design`,
  `distinctive-design`, `android-compose-craft`, `document-generation`,
  `product-validation`, `issue-triage`, `skill-discovery`,
  `mcp-server-builder`, `web-visual-effects`.
- **Skills pack grown to 41 SKILL.md files (40 unique)** across 5 tiers
  (UESF core 6, community 14, original 5, learned 16); `MANIFEST.md` updated
  with the learned tier; `tools/install.sh --all` now installs all five tiers.
- **Catalog updated**: `.agents/skills/README.md` documents the learned tier
  with per-skill inheritance provenance.

### Added — Offline AI Assistant via On-device Gemma Fallback (2026-08-08)
- **Zero-connectivity Assistant**: `GetAssistantResponseUseCase` now routes to
  the on-device Gemma model when no cloud provider is configured **or** the
  configured cloud provider is unreachable (airplane mode, provider outage).
  The fallback order is: best cloud provider → ready on-device model
  (streaming, else chat) → context-aware Copilot fallback. Identity-guarded so
  the same provider instance is never invoked twice; the "no provider" error
  is surfaced only when neither a cloud provider nor a downloaded model exists.
- **SDK selection helper**: `ProviderManager.getOnDeviceProviderFor(capability)`
  returns the best Active/Ready provider implementing `ModelDownloadable` whose
  model is actually downloaded — used by the Assistant and reusable by any
  feature needing an offline LLM fallback.
- **Tests**: 9 new `GetAssistantResponseUseCaseTest` cases (cloud-unreachable→
  on-device, no-cloud→on-device, cloud-preferred, both-fail→Copilot, identity
  guard, one-shot invoke paths, no-provider error) + 4 new `ProviderManagerTest`
  cases (ready-only, null-when-idle, ignores cloud, Active>Ready).

### Added — Device-Capability Gate for On-device Model Downloads (2026-08-08)
- **Pre-download capability check**: tapping *Download model* on an on-device
  provider now inspects free storage (`StatFs`, ≥2 GiB required) and total RAM
  (`ActivityManager`, ≥4 GiB recommendation) via a new `DeviceCapabilityProvider`
  (`feature:profile`, Hilt-bound, IO-dispatched). A confirmation dialog shows the
  **exact model size** in bytes, free storage, and — when the device is
  constrained — a RAM warning and/or a storage-blocked notice.
- **Compact model alternative**: on constrained devices the dialog offers a
  genuinely smaller model — FunctionGemma 270M int8 MediaPipe `.task`
  (284,342,855 bytes ≈ 271 MiB) vs the primary Gemma 3N E2B int4
  (3,136,226,711 bytes ≈ 2.9 GiB) — as a separate download button that routes
  the compact URL to the downloader. When neither model fits, the download is
  hard-blocked with an honest snackbar instead of failing mid-download.
- **Honesty fix**: the provider previously advertised "~1.3 GB" — the real,
  live-verified artifact size is ~2.9 GiB. `GemmaOnDeviceProvider` now exposes
  exact verified sizes (`modelSizeBytes`, `compactModel` via the extended
  `ModelDownloadable` SDK interface) so the UI never understates the footprint.

### Added — On-device Offline LLM + Provider Toolkit Completion (2026-08-08)
- **On-device Gemma provider (offline, keyless)**: new `GemmaOnDeviceProvider`
  in `core:ai-providers` runs a quantized Gemma 3N E2B int4 model fully offline
  via MediaPipe LLM Inference (`tasks-genai` 0.10.35, `libllm_inference_engine_jni`
  packaged in the APK). No API key and no cloud once the ~1.3 GB model is
  downloaded. `Provider Management` gains a per-provider **Download model**
  button with live progress, a **Delete model** action, and honest
  not-downloaded/ready states. The provider self-reports `InvalidConfiguration`
  until the model is present, so provider selection never picks an unusable
  offline model (keyed cloud providers still win when configured). Model URL is
  configurable; default source + Gemma Terms-of-Use flow-down documented in KDoc.
- **Gap fixes**: `ProviderRefreshWorker.knownProviders`, `GetAvailableModelsUseCase`
  defaults, and `AiSettingsViewModel`'s provider list now include **Anthropic
  Claude** and the on-device Gemma provider (Claude was previously never
  health-checked or refreshed).
- **Dead code removed**: the app template theme (`app/ui/theme/*` — the default
  Android Studio Purple80 scaffold) was deleted; it had zero references — the
  real tokenized theme lives in `core:designsystem` (`AivanceTheme`, 4 modes +
  accents + dynamic color).

### Added — UESF Adoption (2026-08-07)
- **UESF framework adopted**: core loop skills installed to `.agents/skills/`
  (planning, implementation, testing, debugging, review, refactoring) from the
  self-validating UESF framework (`uesf/`); the framework validator + test suite
  are now a hard CI gate (`.github/workflows/uesf.yml`), including a drift check
  that the installed skills match the source.
- **Analytics baseline snapshot (M-03/P2-01)**: the guarantee lives in the data
  layer — `AnalyticsRepositoryImpl.getSnapshots()` is self-healing: when the
  snapshot list is empty it captures a real baseline snapshot (derived from real
  applications/interview sessions/ATS results, never fabricated) before forwarding
  the Room flow, `Mutex`-guarded so concurrent collectors can't double-insert.
  Every consumer (analytics dashboard, career state engine, assistant context)
  inherits the guarantee. Shared ATS-report/readiness/recruiter derivation
  extracted (`ResumeAnalysisEntity.toAtsReport()` + private helpers reused by
  `createSnapshot` and `getCareerIntelligence`); ViewModel-level
  `ensureBaselineSnapshot()` removed. New `AnalyticsRepositoryImplTest` proves
  real-data derivation and idempotency. Weekly `AnalyticsSnapshotWorker` continues
  long-term accumulation.

## [1.0.0] - 2026-07-31

### Added — Platform (Phases 1–11)
- **Provider Platform**: Metadata-driven provider SDK with AI, Job, and Enrichment provider types; lifecycle orchestration; validate-before-save onboarding.
- **AI Providers**: Google Gemini, Anthropic Claude, Groq, OpenRouter, OpenAI, Ollama.
- **Job Providers**: LinkedIn, Indeed, Greenhouse, Lever, RemoteOK, Remotive, Apify (real actor pipeline), plus four free global engines — Arbeitnow (EU/Germany, keyless), Jobicy (global remote, keyless), Adzuna (free tier, 16 countries), USAJobs (US federal).
- **Enrichment Provider**: Hunter.io — real domain search and email verification.
- **Resume Intelligence**: AI parsing, PDF/DOCX import, multi-version storage, TXT/MD/JSON export.
- **ATS Intelligence**: Semantic resume-to-JD matching, keyword gap analysis, formatting score, optimization insights.
- **Job Discovery**: Unified multi-provider aggregation, normalization, caching, and search.
- **Professional Network**: Recruiter CRM, AI outreach generation, communication history.
- **Cover Letter Intelligence**: Structured, versioned, sectional AI cover letters.
- **Interview Intelligence**: Mock interview orchestration and per-answer AI evaluation.
- **Application Workflow**: Career pipeline with application lifecycle and stage management.
- **Career Analytics**: KPI dashboards, explainable Career Score, prioritized AI recommendations.
- **AI Career Assistant**: Domain-aware conversational orchestrator.
- **Security & Privacy**: AES-GCM on-device encryption (Google Tink + Android Keystore), encrypted DataStore for secrets, Privacy Center with data export/deletion, audit logs.

### Added — UX & Design System (Phase 12)
- Tokenized design system (`:core:designsystem`): color, type, spacing, shape, elevation, motion.
- Themes: Light, Dark, AMOLED, Dynamic (Material You), custom accents.
- Reusable component library (buttons, cards, states, banners, charts, gauges, skeletons, top bars).
- Redesigned Dashboard (Command Center), Assistant (OS-style streaming), Analytics (interactive charts), Tracker (Kanban + drag-and-drop), Profile, Jobs, Resume, Interview, Recruiter.
- Honest state-driven UI: loading, empty, success, error, partial — no mock data, no dead controls.

### Added — Quality Engineering & Release (Phase 13)
- 20+ stale test files repaired against current contracts across `core:domain`, `core:data`, `app`, and 6 feature modules.
- Full project `testDebugUnitTest` green.

### Added — Production Launch (Phase 14)
- Release build variant with R8 minification + resource shrinking, ProGuard mapping, native symbol table.
- Play-ready signing configuration (env-var secrets, `keystore.jks`).
- Complete CI/CD pipeline: quick-check, code quality (detekt/lint), unit tests (16-module matrix), emulator tests (API 29/34), coverage, security scan, release build (AAB/APK/mapping), benchmarks, Play Store upload, notifications.
- Crash reporting (`CrashReporter`) and operational telemetry with privacy controls.
- Documentation suite: README, CONTRIBUTING, Architecture, API, Database Schema, Security Guide, Design System, Testing Guide, Deployment Guide, Release Guide, Operations Guide, Observability Guide, Known Issues, Test Plan, Changelog, Roadmap, LICENSE.

### Fixed
- Circular dependency between `StructuredTimberTree` and `TelemetryEngineImpl` (logging loop) — ADR 003.
- Stale tests referencing deleted use cases (`ApplyToJobUseCase`, `BookmarkJobUseCase`, `SaveJobUseCase`, `RemoveSavedJobUseCase`, `SearchSavedJobsUseCase`) and changed ViewModel contracts.
- `MockWebServer` API tests for Arbeitnow, Jobicy, and Apify pipeline.

### Security
- All API keys removed from SQLite; moved to encrypted DataStore.
- PII columns (email, raw text, outreach content) encrypted at rest (AES-GCM).
- No secrets in source code; release signing via CI secrets.

### Known Limitations
- `RecruiterIntelligenceRepository` persistence still uses mock logic; Hunter.io API integration is real.
- Adzuna & USAJobs dormant until free API keys are entered.
- `SecurityMigrationWorker` is a skeleton; destructive plaintext cleanup planned for v1.1 (DB v21).
- Localization (i18n) and cloud sync are roadmap items, not in v1.0.0.

## [1.0.1] - 2026-08-06

### Fixed — End-to-End Core Bug Fixes & Stability
- **Google Sign-In Activity Context**: Fixed Credential Manager bottom sheet invocation by passing `LocalContext.current` Activity context in `AuthScreen.kt` and `AuthViewModel.kt`.
- **Resume Engine Step 4 Text Input**: Resolved automatic text clearing on keystroke/paste by keying `AnimatedContent` on `state.stepIndex()` instead of target `state` object.
- **Resume Engine Step 5 Optimization**: Fixed section optimization failure for in-memory resumes (`versionId == 0`) by passing current section content directly to `StreamImproveSectionUseCase.kt`.
- **Resume Engine Step 7 Export**: Corrected PDF & DOCX export effect handling and intent chooser launching with `FLAG_ACTIVITY_NEW_TASK`.
- **Job Discovery Filtering & Fallbacks**: Fixed nested `LazyColumn` height constraint issue in `JobsScreen.kt`, updated `JobFilterMatcher.kt` structured location matching, and added automatic database seed fallback (`seedDefaultJobsIfEmpty`) in `JobRepositoryImpl.kt`.
- **Prep Studio Real Data Integration**: Dynamic practice hours computation, candidate profile-driven company research and interview edge insights in `PrepStudioScreen.kt`.
- **AI Assistant Copilot Fallback**: Added context-aware local Copilot response generator in `GetAssistantResponseUseCase.kt` when remote LLM providers are unconfigured or unreachable.
- **Package Visibility**: Added `<queries>` block for `ACTION_SEND` intents in `AndroidManifest.xml`.

