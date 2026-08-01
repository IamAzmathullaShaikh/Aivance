# AiVance — v1.0.0 Launch Checklist

Use this checklist to gate the public launch. Every item must be **verified** (not assumed).

---

## 1. Play Store Assets
- [ ] **App icon** (512×512, 32-bit PNG) uploaded.
- [ ] **Adaptive icon** (foreground/background layers) configured in the project.
- [ ] **Feature graphic** (1024×500) uploaded.
- [ ] **Screenshots** (phone ≥2; tablet recommended) uploaded for listing.
- [ ] **App name / short description / full description** finalized.
- [ ] **Content rating questionnaire** completed.
- [ ] **Target audience** declared.
- [ ] **App category** set (Productivity / Job Search).
- [ ] **Privacy Policy URL** live and linked.
- [ ] **Terms of Service URL** live (or bundled).
- [ ] **Data Safety form** submitted and accurate (career content stored locally; no third-party sharing).

## 2. Build & Signing
- [ ] `versionCode` = 1, `versionName` = "1.0.0" confirmed in `app/build.gradle.kts`.
- [ ] `keystore.jks` generated and backed up securely (offline).
- [ ] Signing env vars set for CI (`AIVANCE_STORE_PASSWORD`, `AIVANCE_KEY_ALIAS`, `AIVANCE_KEY_PASSWORD`).
- [ ] `AIVANCE_KEYSTORE_BASE64` secret set in GitHub Actions.
- [ ] `./gradlew bundleRelease` produces a signed AAB.
- [ ] AAB verified with `bundletool` (signature + install).
- [ ] ProGuard `mapping.txt` retained privately (90-day artifact).

## 3. Release Notes
- [ ] `CHANGELOG.md` updated with v1.0.0.
- [ ] Play release notes written (What's New).
- [ ] Migration notes for v19 → v20 upgrade included.

## 4. Privacy & Compliance
- [ ] Privacy Center export + full wipe verified on device.
- [ ] Telemetry sweep: no PII/credentials in crash or analytics payloads.
- [ ] Firebase/analytics consent honored (`analyticsEnabled` setting).
- [ ] Encryption verified: PII columns ciphertext at rest.

## 5. Monitoring
- [ ] `CrashReporter` active in release builds.
- [ ] Crash symbolication mapping upload wired to Play Console.
- [ ] KPI dashboard targets documented (`OBSERVABILITY_GUIDE.md`).
- [ ] Slack/email alerting on CI failure verified.
- [ ] Staged rollout configured (`userFraction: 0.1`).

## 6. Backup & Recovery
- [ ] Database backup behavior validated (Room WAL/export).
- [ ] User export flow works from Privacy Center.
- [ ] Recovery validation: fresh install + same-device Keystore restores encrypted data.
- [ ] Keystore-bound limitation communicated to users (support doc).

## 7. Rollback Plan
- [ ] Previous version available for rollback in Play Console.
- [ ] Rollback runbook documented (`OPERATIONS_GUIDE.md`).
- [ ] versionCode monotonicity policy agreed.

## 8. Support Plan
- [ ] Support email/contact channel active.
- [ ] `KNOWN_ISSUES.md` published with the release.
- [ ] Escalation path documented (support → reproduce → classify → fix).
- [ ] On-call owner assigned for launch week.

## 9. Final Gates
- [ ] Full `testDebugUnitTest` green.
- [ ] `assembleDebug` + `bundleRelease` green.
- [ ] Manual QA checklist complete (`TEST_PLAN.md`).
- [ ] Repository tagged `v1.0.0`.
- [ ] **GO / NO-GO** decision recorded.

---

## GO / NO-GO
- **Date**: ________
- **Decision**: ☐ GO   ☐ NO-GO (blocked by: ________)
- **Sign-off**: ________
