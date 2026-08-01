# AiVance Test Plan

This plan defines the testing strategy for AiVance v1.0.0 and the release verification procedure.

## Test Levels

| Level | Scope | Tooling | Owner |
| :--- | :--- | :--- | :--- |
| Unit | Use cases, ViewModels, repositories, normalizers, mappers | JUnit4, MockK, kotlinx-coroutines-test, Turbine, Truth | CI `unit-tests` |
| Integration | Provider API contracts (MockWebServer) | MockWebServer, Robolectric | CI `unit-tests` (job/enrichment modules) |
| Instrumented | UI flows on emulator | Compose UI test, Espresso | CI `android-tests` (API 29/34) |
| Static analysis | detekt, Android Lint, API checks | Gradle | CI `code-quality` |
| Security | dependency vuln scan, license audit | dependency-check/ossIndex (best effort) | CI `security-scan` |
| Performance | macrobenchmark, baseline profile | androidx.benchmark | CI `benchmark` |
| Manual QA | Release Candidate on real devices | — | Release engineer |

## Unit Test Modules (CI matrix)

`:core:common`, `:core:domain`, `:core:data`, `:core:sdk`, `:core:database`, `:core:datastore`, `:core:util`, `:core:job-providers`, `:feature:ats`, `:feature:coverletter`, `:feature:dashboard`, `:feature:interview`, `:feature:jobs`, `:feature:profile`, `:feature:resume`, `:feature:tracker`.

> Phase 13 repaired 15+ stale test files across `core:domain`, `core:data`, `app`, and the feature modules so the full suite is green.

## Test Conventions

- **ViewModels**: `Dispatchers.setMain(StandardTestDispatcher())`; advance the **Main scheduler** via `testDispatcher.scheduler.advanceUntilIdle()` before `coVerify`/`effects.test` to avoid cross-scheduler races.
- **Use cases**: mock the repository with direct `CoreResult` returns (not Flows) where production calls are synchronous.
- **State-driven UI**: assert each state (Loading / Success / Empty / Error) with real data — no mocks of mock data.
- **Provider APIs**: MockWebServer against live provider JSON schemas (Arbeitnow, Jobicy, Apify pipeline, Hunter.io).
- **Database**: schema JSON exports committed per version; migrations tested across the full chain.

## Release Verification Procedure (RC → 1.0.0)

Automated gate (`./gradlew`):
1. `testDebugUnitTest` — full project unit suite (must be BUILD SUCCESSFUL).
2. `lintDebug` — no fatal lint errors.
3. `detekt` — config-gated, no new violations.
4. `assembleDebug` — debug build.
5. `bundleRelease` + `assembleRelease` — release artifacts (AAB + universal APK).
6. `connectedDebugAndroidTest` — instrumented on API 29 & 34 (CI).

Manual verification checklist:
- ✓ First install & onboarding (6-step provider setup).
- ✓ Provider configuration (AI + keyless Job providers out of the box).
- ✓ Resume import (PDF/DOCX) → parse → ATS analysis → export.
- ✓ Job search (keyless providers return results), bookmark, details, external URL.
- ✓ Recruiter discovery (Hunter.io) + outreach generation.
- ✓ Cover letter generation + section editing + export.
- ✓ Interview practice session + AI feedback.
- ✓ Workflow tracking (kanban stages, timeline).
- ✓ Analytics dashboard + career score.
- ✓ AI Assistant conversation.
- ✓ Notifications settings; Settings persistence; Privacy Center export/delete.
- ✓ Dark mode / AMOLED / dynamic color.
- ✓ Offline mode (cached jobs, honest error states).
- ✓ Tablet, foldable, and landscape layouts.

## Regression Rules

- Every bug fix ships with a regression test that fails on the old code.
- Provider contract changes must update the MockWebServer fixtures in the same PR.
- Contract changes to domain/repository APIs must update all referencing tests in the same commit (CI matrix enforces this).
