# Repository Map

## Modules
- **`:app`**: Application shell, WorkManager automation, Security migrations.
- **`:navigation`**: Central NavGraph, type-safe routing, Privacy Center, adaptive navigation suite.
- **`:core:designsystem`**: Design tokens (color/type/spacing/shape/elevation/motion), theme engine, component library.
- **`:core:sdk`**: Provider Platform (AI, Job, Enrichment) with secure credential resolution.
- **`:core:domain`**: Business logic, Workflow Engine, Analytics engines.
- **`:core:data`**: Repositories, Entity mappers, Secrets resolution.
- **`:core:database`**: Room entities (v20), Encrypted Type Converters, Audit Logs.
- **`:core:common`**: Shared models, `EncryptedString` security wrapper.
- **`:core:job-providers`**: Job search adapters (RemoteOK, Remotive, LinkedIn, Indeed, Greenhouse, Lever, Apify, Arbeitnow, Jobicy, Adzuna, USAJobs).
- **`:core:enrichment-providers`**: Hunter.io domain search + email verification.
- **`:core:util`**: `EncryptionService` (Tink AES-GCM), Text extractors.
- **`:core:datastore`**: `SecretsManager` (Encrypted DataStore), Preferences.
- **`:feature:dashboard`**: User overview and metrics.
- **`:feature:tracker`**: Career Pipeline and Workflow management.
- **`:feature:recruiter`**: Recruiter CRM and Outreach.
- **`:feature:analytics`**: Career Intelligence dashboard.
- **`:feature:assistant`**: Orchestration Chat and proactive briefings.

## Provider Platform
- `ProviderRegistry` -> `ProviderManager` -> `Set<JobProvider>` (multibound) + `Set<EnrichmentProvider>` (multibound).
- Keyless providers (Arbeitnow, Jobicy) are Active out of the box; keyed providers (Adzuna, USAJobs) register as `InvalidConfiguration` until credentials are supplied.
- Apify runs real actor jobs (run -> poll -> dataset); Hunter.io performs real domain search + email verification.

## Database Relationships
- `Application` -> (1:1) -> `Job`.
- `Application` -> (1:1) -> `ResumeVersion`.
- `AnalyticsSnapshot` -> (1:1) -> `CareerScore`.
- `AuditLog` -> (1:1) -> System Action.
- `AssistantConversation` -> (1:N) -> `AssistantMessage`.
