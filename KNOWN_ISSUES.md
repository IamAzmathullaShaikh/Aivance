# AiVance Known Issues

This document tracks known limitations and defects at the **v1.0.0** release. Issues are categorized by severity and have mitigation plans.

## Severity Legend
- 🔴 **High** — impacts data integrity, security, or core workflows.
- 🟡 **Medium** — degrades an experience or a secondary feature.
- 🟢 **Low** — cosmetic or niche.

---

## 🔴 High

### H-01 — Legacy plaintext PII cleanup is pending (DB v21)
- **Area**: `SecurityMigrationWorker` (`:app`).
- **Description**: The migration worker is a skeleton. Legacy plaintext rows (pre-v20) are not yet destructively cleaned.
- **Impact**: Residual plaintext may remain in old table layouts after upgrade.
- **Mitigation**: Ship v1.1 with the destructive cleanup migration; until then, v20 writes are always encrypted.

### H-02 — Keystore-bound recovery
- **Area**: `EncryptionService` / `SecretsManager`.
- **Description**: Encrypted data is bound to the device Keystore key. Factory reset or reinstall without an encrypted export loses local data.
- **Impact**: Data unrecoverable after device reset.
- **Mitigation**: Documented; encrypted export/import + secure cloud sync planned for v1.1.

## 🟡 Medium

### M-01 — Recruiter persistence uses mock logic
- **Area**: `RecruiterIntelligenceRepository` (`core:data`).
- **Description**: Recruiter CRM persistence layer still uses mock logic for storage. The Hunter.io provider integration itself is real (domain search + email verification).
- **Impact**: Recruiter records may not persist across sessions reliably.
- **Mitigation**: Replace with real CRM storage backed by v14 tables in v1.1.

### M-02 — Keyed free job providers dormant by default
- **Area**: `AdzunaProvider`, `USAJobsProvider` (`core:job-providers`).
- **Description**: Both register as `InvalidConfiguration` until free API keys are entered; there is no ProviderFactory-style runtime config path for job providers yet (enrichment providers have one).
- **Impact**: Users must configure keys; search aggregation silently filters these providers.
- **Mitigation**: Add metadata-driven runtime config for job providers in v1.1.

### M-03 — Interview analytics timeline incomplete
- **Area**: `feature:analytics`.
- **Description**: Interview improvement timeline and achievement cards require accumulated analytics history.
- **Impact**: Charts render empty/partial for new users.
- **Mitigation**: Data accumulates over time; honest empty states are shown.

## 🟢 Low

### L-01 — Deprecation warnings in tests
- **Area**: `ResumeViewModelTest`.
- **Description**: `AtsResult`/`ResumeAnalysis` (core model) are `@Deprecated("Use AtsReport")`; tests still construct them because `AtsScoreResponse` carries those types.
- **Impact**: Warning noise in build logs.
- **Mitigation**: Acceptable; track AtsReport migration separately.

### L-02 — Tautological initial-state tests
- **Area**: Several ViewModel tests (e.g., `loading state on init`).
- **Description**: Some tests assert only the trivial initial state value.
- **Impact**: Low signal, no false failures.
- **Mitigation**: Strengthen over time; not release-blocking.

---

## Resolved Previously
- Logging loop between `StructuredTimberTree` and `TelemetryEngineImpl` (ADR 003) — fixed.
- Stale tests referencing deleted use cases across 6 feature modules — fixed in Phase 13.
- Mock WebServer tests for Arbeitnow/Jobicy/Apify — green.
