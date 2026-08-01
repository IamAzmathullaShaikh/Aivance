# Changelog

All notable changes to AiVance are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
