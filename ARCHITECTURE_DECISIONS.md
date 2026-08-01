# Architectural Decisions

## ADR 001: Metadata-Driven Provider Configuration
Metadata-driven system for dynamic provider discovery and UI form generation.

## ADR 002: Provider Lifecycle Orchestration
Thread-safe lifecycle management via `Mutex` per provider.

## ADR 003: Logging System Decoupling
Decoupled telemetry from Timber to avoid circular dependency loops.

## ADR 004: Version-Centric Resume Storage
Normalized "Resume -> Version -> Section" schema for multi-role optimization.

## ADR 005: Resume-to-JD Semantic Matching
AI-orchestrated matching engine for contextual compatibility scores.

## ADR 006: Provider-Agnostic Job Aggregation
Centralized repository for unified search across multiple Job providers.

## ADR 007: Unified Career CRM Layer
Centralized storage for company data, recruiter profiles, and communication history.

## ADR 008: Sectional AI Document Generation
Structured document architecture for independent section refinement and regeneration.

## ADR 009: Granular Interview Evaluation
Per-answer AI deep-dives for communication, technical accuracy, and STAR method compliance.

## ADR 010: Workflow Aggregate Orchestration
Established the `Application` model as the primary aggregate root for cross-module coordination.

## ADR 011: Explainable Career Scoring
Defined four actionable dimensions (ATS, Networking, Interview, Consistency) for success measurement.

---

## ADR 012: On-Device Encryption Architecture
### Problem
Career data (resumes, emails, transcripts) is highly sensitive. Storing it in plaintext SQLite tables poses a privacy risk.

### Selected Solution
Implemented a platform-wide encryption layer using **Google Tink** and **Android Keystore**. Created an `EncryptedString` value class paired with a Room `ProvidedTypeConverter` to automate encryption at rest.

### Reasoning
This ensures that even if the physical device is compromised or the database file is extracted, the sensitive career history remains unreadable without the hardware-backed key. By using `AES-GCM`, we guarantee both confidentiality and integrity of the data.

### Future Impact
Enables "Secure Cloud Sync" (Phase 13) by allowing the app to sync already-encrypted blobs to a remote server without the server ever seeing plaintext data.

---

## ADR 013: Tokens-First Design System
### Problem
Screens each styled themselves with raw colors, shapes, and durations, producing inconsistent layouts and preventing coherent theming (Light/Dark/AMOLED/Dynamic).

### Selected Solution
Centralized every visual decision into `:core:designsystem` token groups (Color, Type, Spacing, Shapes, Elevation, Motion) exposed via `AivanceTheme`. A theme engine supports Light, Dark, AMOLED, Dynamic Material You, and custom accents, persisted through DataStore. A reusable component library consumes these tokens exclusively.

### Reasoning
Single-source tokens guarantee visual consistency, make dark/AMOLED theming trivial, and freeze contracts so Phase 13+ features inherit the design language automatically.

### Future Impact
Presentation contracts are frozen (see `PHASE_12_REPORT.artifact.md`); new UI must compose from tokens and the component library.

## ADR 014: Honest State-Driven UI
### Problem
Placeholder layouts and hardcoded mock data (interview questions/feedback, dead TODO buttons) misrepresented the product and blocked real UX work.

### Selected Solution
Every screen now renders five first-class states (loading, empty, success, error, partial) using shared components. Mock data and dead controls were eliminated; state that cannot yet be produced renders an honest loading/empty state instead.

### Reasoning
Honest states make failures explainable ("Provider unavailable" with retry), keep the UI truthful during streaming/background work, and preserve business logic untouched.

### Future Impact
New features must ship with complete state coverage and real event wiring — no placeholders.

---

## ADR 015: Release-Ready Test Contracts
### Problem
Phase 1–11 rewrites changed use-case and ViewModel signatures, leaving 20+ stale test files that referenced deleted classes (`ApplyToJobUseCase`, `BookmarkJobUseCase`, `SearchSavedJobsUseCase`), obsolete constructor shapes, and Flow-returning stubs where production now returns `CoreResult` synchronously. The stale tests failed to compile, blocking the release gate.

### Selected Solution
Rewrote every stale test against the current contracts: direct `CoreResult` stubs, Main-dispatcher scheduler advancement (`testDispatcher.scheduler.advanceUntilIdle()`) before `coVerify`/`effects.test`, turbine for effects, and removal of tests for deleted behavior. App-module tests mock WorkManager via `mockkObject(WorkManager.Companion)` and stub `getSystemService` for ConnectivityMonitor.

### Reasoning
Tests must describe the system as it exists, not as it was. Deterministic Main-dispatcher scheduling removes the cross-scheduler races that caused flaky tests.

### Future Impact
All new features must ship with tests matching current contracts in the same commit — enforced by the CI unit-test matrix.

---

## ADR 016: Environment-Separated Release Pipeline
### Problem
Production distribution requires reproducible builds, secure signing, and staged rollout with no secrets in source code.

### Selected Solution
Release signing is driven by environment variables (`AIVANCE_STORE_PASSWORD`, `AIVANCE_KEY_ALIAS`, `AIVANCE_KEY_PASSWORD`) plus an optional `keystore.jks`; CI decodes `AIVANCE_KEYSTORE_BASE64`. The pipeline runs gates (quality → tests → security) before producing signed AAB/APK, ProGuard mapping, and native symbols, then uploads to Google Play with a staged 10% rollout and crash symbolication mapping.

### Reasoning
Builds are reproducible and reversible (rollback via Play Console); signing secrets never touch the repository; mapping files stay private.

### Future Impact
Every future release follows the same pipeline; version bumps are the only per-release code change.

---

## ADR 017: Privacy-First Production Telemetry
### Problem
Crash and analytics reporting must not leak sensitive career data or provider credentials.

### Selected Solution
Centralized `CrashReporter` with payload scrubbing; KPI telemetry carries only non-PII metadata (event names, timestamps, provider health, durations); analytics can be disabled in Settings; audit logs stay local and exportable via the Privacy Center.

### Reasoning
Monitors production stability while preserving user trust and GDPR posture.

### Future Impact
All future instrumentation must pass the same scrub rules before it ships.
