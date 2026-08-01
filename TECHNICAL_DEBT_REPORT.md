# AiVance — Technical Debt Report

**Version**: 1.0.0 · **Date**: 2026-07-31

Debt is categorized by the horizon in which it must be addressed. Each item has an owner and mitigation plan.

---

## 🔴 Immediate (blocking nothing, but fix before v1.1)

### T-01 — SecurityMigrationWorker is a skeleton
- **Area**: `:app` → `SecurityMigrationWorker`.
- **Debt**: The v20 migration hardened new writes, but legacy plaintext cleanup is not implemented.
- **Impact**: Residual plaintext in legacy rows after upgrade; security posture incomplete.
- **Mitigation**: Implement destructive cleanup in v1.1 (DB v21), test on an upgrade fixture from v19.

### T-02 — Recruiter persistence mock logic
- **Area**: `RecruiterIntelligenceRepository` (`core:data`).
- **Debt**: CRM storage uses mock logic; the Hunter.io provider itself is real.
- **Impact**: Recruiter records may not persist reliably.
- **Mitigation**: Wire real CRM storage to the v14 schema tables; add repository tests.

## 🟡 Short-term (v1.1–v1.2)

### T-03 — Runtime config path for keyed job providers
- **Debt**: Enrichment providers have metadata-driven runtime config; job providers (Adzuna, USAJobs) do not, so they stay dormant without keys.
- **Mitigation**: Add a ProviderFactory-style config path to the job-provider SDK; onboard keyed providers through Settings.

### T-03b — Keystore-bound recovery UX (KNOWN_ISSUES H-02)
- **Debt**: Encrypted local data is bound to the device Keystore key; factory reset/reinstall without an encrypted export loses local data.
- **Mitigation**: Ship encrypted export/import in Privacy Center (v1.1); document the limitation in-app and in `KNOWN_ISSUES.md`.

### T-04 — Deprecated core models (`AtsResult`, `ResumeAnalysis`)
- **Debt**: Marked `@Deprecated("Use AtsReport")` but still used by `AtsScoreResponse` and several ViewModels.
- **Mitigation**: Complete the AtsReport migration; remove deprecated types in v1.1.

### T-05 — Coverage gaps in tests
- **Debt**: Some ViewModel tests are tautological (initial-state assertions) or omit event verification (e.g., follow-up toggle event before Phase 14 fix).
- **Mitigation**: Strengthen assertions alongside feature work; no release impact.

### T-06 — Automated accessibility UI tests
- **Debt**: Accessibility validated by guide + manual QA; no automated a11y tests in CI.
- **Mitigation**: Add Compose a11y UI tests for critical flows in v1.1.

## 🟢 Long-term (v2.0+)

### T-07 — Logging abstraction
- **Debt**: Telemetry was decoupled from Timber (ADR 003) with raw `android.util.Log`; a robust abstraction is still pending.
- **Mitigation**: Introduce a release-grade logging facade in v2.0.

### T-08 — Sync/backup restore
- **Debt**: Encrypted data is device-bound; no encrypted export/import or server sync.
- **Mitigation**: Secure blob sync (zero-knowledge server) with encrypted export/import in v1.1/v2.0.

### T-09 — Legacy `SyncManager` endpoints
- **Debt**: `SyncManager` operations were local placeholders; remote sync endpoints do not exist.
- **Mitigation**: Replace with the v2.0 sync service; remove placeholder code.

### T-10 — Duplicated deprecated DTO paths
- **Debt**: Multiple DTO/mapper layers for jobs (feature-local `JobListing` vs core) exist.
- **Mitigation**: Consolidate on core models in a future refactor.

---

## Debt Summary

| Horizon | Items | Release impact |
| :--- | :--- | :--- |
| Immediate | 2 | None — non-blocking, scheduled for v1.1. |
| Short-term | 4 | None — scheduled v1.1–v1.2. |
| Long-term | 4 | None — scheduled v2.0+. |

## Principles Going Forward

1. **No new debt in release builds**: every PR must compile, pass tests, and lint in CI.
2. **Pay down with feature work**: T-01–T-04 are prioritized for v1.1.
3. **Frozen contracts**: none of the above requires breaking a v1.0.0 contract.
