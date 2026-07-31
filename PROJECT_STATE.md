# AiVance Project State

## Current Architecture
- **Paradigm**: Clean Architecture, SOLID Principles, Offline-First.
- **Pattern**: MVVM with Repository pattern.
- **Dependency Injection**: Hilt.
- **UI**: Jetpack Compose with Material Design 3.
- **Concurrency**: Kotlin Coroutines & Flow.
- **Data Persistence**: Room (v20) & DataStore.
- **Background Tasks**: WorkManager.
- **Provider System**: Plug-and-play Provider SDK architecture for AI, Job, and Enrichment services.
- **Security**: Centralized on-device encryption (AES-GCM via Google Tink) and Keystore-backed secrets management.

## Current Modules
### Core
- `:core:sdk`: Base infrastructure for providers, status, and lifecycle management.
- `:core:common`: Domain models, security wrappers (`EncryptedString`), and results.
- `:core:database`: Room database implementation (v20), encrypted type converters.
- `:core:data`: Repository implementations and local/remote bridges.
- `:core:domain`: Business logic, UseCases, and capability orchestration.
- `:core:ai-providers`: Concrete implementations for Gemini, Claude, etc.
- `:core:job-providers`: Concrete implementations for LinkedIn, Indeed, etc.
- `:core:enrichment-providers`: Hunter.io integration for recruiter discovery.
- `:core:designsystem`: Reusable Compose components, themes, and spacing.
- `:core:network`: Retrofit setup, security utilities.
- `:core:datastore`: Secure secret storage and preferences.
- `:core:util`: Utility classes, including `EncryptionService`.

### Features
- `:feature:dashboard`: Unified home view with career progress overview.
- `:feature:assistant`: Intelligent orchestration and conversational interface.
- `:feature:profile`: User settings, account management, and **Privacy Center**.
- `:feature:jobs`: Job discovery, aggregation, and caching.
- `:feature:resume`: Resume builder, AI parsing, and version management.
- `:feature:ats`: Semantic matching engine and match reports.
- `:feature:tracker`: Career pipeline management and application tracking.
- `:feature:interview`: Mock interviews and AI evaluation engine.
- `:feature:coverletter`: Sectional AI cover letter generation.
- `:feature:recruiter`: Recruiter CRM and AI outreach generation.
- `:feature:analytics`: Career Intelligence and Insights dashboard.

### App & Navigation
- `:app`: Application entry point, Hilt setup, WorkManager security migrations.
- `:navigation`: Central NavGraph (Type-safe), AppShell.

## Current Provider Support
- **AI Providers**: Google Gemini, Anthropic Claude, Groq, OpenRouter, OpenAI, Ollama.
- **Job Providers**: LinkedIn, Indeed, Greenhouse, Lever, RemoteOK, Remotive, Apify, **Arbeitnow** (free, EU/Germany), **Jobicy** (free, global remote), **Adzuna** (free tier, 16 countries), **USAJobs** (free, US federal).
- **Enrichment Providers**: Hunter.io (real domain search + email verification).

## Database & API
- **Room Version**: 20.
- **Latest Migration**: `MIGRATION_19_20` (Security Hardening, Audit Logs).
- **Encryption**: AES-GCM (Tink) for PII (emails, resume text, outreach content).
- **API integrations**: Firebase AI SDK, Retrofit, OkHttp.

## Feature Completion Status
| Feature | Status | Completion % |
| :--- | :--- | :--- |
| Provider Platform | Completed | 100% |
| Intelligent Onboarding | Completed | 100% |
| Resume Engine | Completed | 100% |
| ATS Engine | Completed | 100% |
| Job Search (Unified) | Completed | 100% |
| Recruiter Platform | Completed | 100% |
| Cover Letter Engine | Completed | 100% |
| Interview Engine | Completed | 100% |
| Application Workflow | Completed | 100% |
| Analytics Platform | Completed | 100% |
| AI Career Assistant | Completed | 100% |
| Security & Privacy | Completed | 100% |

## Phase 12 Completion
- **Design System**: Tokenized color/type/spacing/shape/elevation/motion with Light/Dark/AMOLED/Dynamic themes.
- **Component Library**: `:core:designsystem` catalog (buttons, cards, states, charts, gauges, banners) — see `COMPONENT_LIBRARY.md`.
- **Redesigned**: Dashboard (Command Center), Assistant (OS-style streaming), Analytics (interactive charts), Tracker (Kanban + drag-and-drop), Profile (sectioned hub), Jobs, Resume, Interview (mock data removed), Recruiter.
- **Contracts Frozen**: Design system, components, navigation, theme, motion — see `PHASE_12_REPORT.artifact.md`.

## Known Issues
- `RecruiterIntelligenceRepository` (core:data) still uses mock logic for recruiter persistence; the Hunter.io provider itself is now a real API integration (domain search + email verification).
- Adzuna & USAJobs are registered as dormant (InvalidConfiguration) until real free keys are entered — job providers don't yet have a ProviderFactory-style runtime config path like enrichment providers.
- `SecurityMigrationWorker` currently a skeleton; full destructive plaintext cleanup planned for v21.
- Interview improvement timeline & achievements pending real analytics history (Phase 12+).

## Release Readiness
- **Stability**: Build stable across 25 modules (`assembleDebug` green).
- **Security**: All API keys moved to encrypted DataStore.
- **Privacy**: GDPR-compliant Data Export and Deletion active in Privacy Center.
- **Navigation**: Full type-safe backstack with 6 root destinations.
- **UI**: Unified design system; all screens consume shared components; no mock data or dead controls.
