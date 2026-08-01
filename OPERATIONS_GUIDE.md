# AiVance Operations Guide

This guide covers the day-to-day operation of AiVance after release: monitoring, support, maintenance, and incident handling.

> Related: `OBSERVABILITY_GUIDE.md` (KPIs & telemetry), `RELEASE_GUIDE.md` (release process), `KNOWN_ISSUES.md` (issue catalog).

## Operational Responsibilities

| Area | Owner | Cadence |
| :--- | :--- | :--- |
| CI/CD health | Platform engineer | Daily |
| Crash/stability review | Mobile engineer | Weekly |
| KPI dashboards | Product/Data | Weekly |
| Provider health (API keys, quotas) | Platform engineer | Weekly |
| Play Console policy & ratings | Product | Monthly |
| Dependency/security updates | Platform engineer | Monthly |
| Backup/restore validation | Platform engineer | Per release |

## Daily Operations

1. Check CI runs on `master` — all gates green (`code-quality`, `unit-tests`, `security-scan`, `build`).
2. Review `notify` job output; triage any failure.
3. Watch provider health: keyless providers (Arbeitnow/Jobicy) must stay Active; keyed providers (Adzuna/USAJobs, Apify, Hunter.io) need quota headroom.

## Monitoring

- **Stability**: crash-free sessions, ANRs, startup failures (targets in `OBSERVABILITY_GUIDE.md`).
- **Workers**: `AnalyticsSnapshotWorker`, `JobSyncWorker`, `CacheCleanupWorker`, `DatabaseCleanupWorker`, `ProviderRefreshWorker`, `SecurityMigrationWorker` — alert on repeated failures.
- **Performance**: cold start, screen load, AI latency, memory, network/storage usage.

## Support

- **Escalation path**: Support triage → reproduce → classify (crash/data/provider) → hotfix vs. minor.
- **Data issues**: use Privacy Center export on the affected device to inspect local state.
- **Provider issues**: verify the API key/credential in Settings; quota errors surface as honest UI errors with retry.

## Maintenance Windows

- Hotfixes may ship any time (frozen contracts).
- Minor features bundle into scheduled `v1.x` releases.
- Database migrations must be tested across the full chain (`MIGRATION_1_10` … `19_20`) before shipping.

## Incident Response

1. **Detect** (monitoring/CI/Play alerts).
2. **Triage** (severity: 🔴 data/security → immediate; 🟡 degraded → next release; 🟢 cosmetic → backlog).
3. **Contain** (rollback on Play, revoke exposed credentials).
4. **Fix + regression test** (hotfix pipeline).
5. **Post-mortem** → `KNOWN_ISSUES.md` / `IMPLEMENTATION_LOG.md`.

## Backup & Recovery

- Local data is device-bound and Keystore-encrypted (see `SECURITY_GUIDE.md` H-02).
- **User export**: Privacy Center JSON export.
- **Recovery validation**: per release, verify export → fresh install → import on the same device profile.
- **Server-side**: no plaintext career data is stored remotely; future sync (v1.1) moves encrypted blobs only.

## Play Console Operations

- Keep Data Safety form accurate (career content stored locally; no third-party sharing).
- Monitor user ratings; respond to support emails.
- Review staged rollout (`userFraction`) before full ramp.
