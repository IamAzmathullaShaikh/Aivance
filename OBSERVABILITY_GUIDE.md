# AiVance Observability Guide

This guide documents how AiVance is monitored in production: crash reporting, operational telemetry, KPIs, and the privacy rules that govern them.

## Principles

1. **No sensitive information transmitted** — telemetry events must never contain API keys, resume text, PII, or provider credentials.
2. **User consent** — analytics can be disabled from Settings (`analyticsEnabled`); telemetry respects it.
3. **Local-first** — most diagnostics (audit logs, usage events) are stored locally and exported through the Privacy Center.

## Crash & Stability Monitoring

- **CrashReporter** (`core:data/analytics/CrashReporter.kt`) centralizes crash capture and scrubs payloads before reporting.
- **Target metrics**:
  - Crash-free sessions ≥ 99.5%.
  - ANR rate ≤ 0.1% of sessions.
  - No startup crashes (0% fatal on cold start).

## Operational Telemetry (KPIs)

| KPI | Definition | Target |
| :--- | :--- | :--- |
| Cold start time | Process start → first frame | < 2.5 s on mid-range device |
| Screen load time | Composition of a destination | < 500 ms p95 |
| AI response time | Provider `generateText` latency | < 15 s p95 (streaming) |
| Provider health | Success rate per provider ID | ≥ 95% |
| Background worker failures | WorkManager run failures | < 1% |
| Memory | RSS growth on long sessions | No unbounded growth |
| Network | Requests per search; payload size | Minimized via caching |

These are measured by the benchmark job (`macrobenchmark`, baseline profile) and by `AnalyticsSnapshotWorker` snapshots.

## Privacy-Safe Event Model

- Events carry only: event name, timestamp, provider ID (for health), success/failure, duration.
- **Never** captured: credential values, resume/JD content, recruiter contact data, conversation text.
- The `TrackEventUseCase` validates event names and allows blank-name rejection.

## Logging

- **Timber** (`StructuredTimberTree`) for structured development logs.
- **TelemetryEngine** decoupled from Timber to prevent logging loops (ADR 003).
- Release builds strip verbose logs; structured errors are the reporting surface.

## Monitoring Plan

1. **Pre-release**: CI `notify` job summarizes build status; benchmark job publishes performance baselines.
2. **Post-release (v1.0.0)**: enable crash reporting dashboards; track KPI dashboards weekly.
3. **Alerting**: Slack + email on CI failure; crash-free threshold alerts after Crashlytics integration completes.

## Operations Checklist

- [ ] Crash symbolication mapping uploaded per release (CI `proguard-mapping` artifact).
- [ ] Release notes include known issues (`KNOWN_ISSUES.md`).
- [ ] Privacy Center export used to validate the data model before each release.
- [ ] Telemetry sweep: grep release logs for credentials before tagging.
