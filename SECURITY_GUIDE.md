# AiVance Security Guide

This guide describes the security architecture of AiVance, the threat model it defends against, and the operational practices required to keep it secure.

## Security Posture Summary

- **Encryption at rest**: AES-GCM (128-bit) via **Google Tink**, keys held in the **Android Keystore** (hardware-backed where available).
- **Secrets isolation**: All API keys live in an **encrypted DataStore** (`SecretsManager`), never in SQLite or source code.
- **PII protection**: Emails, resume raw text, and outreach content are stored as `EncryptedString` and converted transparently by Room `ProvidedTypeConverter`.
- **Integrity**: AES-GCM provides authenticated encryption — ciphertext tampering is detected.
- **Privacy**: Privacy Center offers data export, module-specific wipes, and full deletion. Audit logs track system actions.
- **Transport**: All provider traffic uses HTTPS (Retrofit/OkHttp with TLS).

## Threat Model

| Threat | Mitigation |
| :--- | :--- |
| Physical device compromise / DB extraction | PII + secrets encrypted at rest; keys in Keystore. |
| Backup exfiltration | Encrypted blobs travel; plaintext never written to unencrypted backup. |
| Secret leakage via logs/crash reports | Telemetry scrubs secrets; `CrashReporter` avoids logging credentials. |
| Man-in-the-middle on provider APIs | HTTPS only; certificate validation defaults. |
| Reverse engineering of credentials | No credentials embedded in APK; R8 minification + ProGuard mapping kept private. |
| DB export/sync leakage | Sync only already-encrypted blobs (server has zero knowledge). |
| Replay of audit/analytics events | Timestamped audit log; analytics events are non-PII. |

## Key Management

- **Keystore keys**: generated once per install; `AndroidKeyStore` with `AES/GCM/NoPadding`. Clearing app data does **not** clear Keystore keys (verified), so encrypted data survives cache clears.
- **Recovery caveat**: If the Keystore key is lost (factory reset, reinstall with data restore), encrypted local data becomes unrecoverable. This is an accepted trade-off; a future encrypted-export/import flow (v1.1) will mitigate it.

## Encryption Layers

```
┌─────────────────────────────┐
│ SQLite (Room, DB v20)       │  ← PII columns ciphertext
│ EncryptedString converters  │
├─────────────────────────────┤
│ DataStore (preferences)     │  ← EncryptedFile-backed SecretsManager
├─────────────────────────────┤
│ Android Keystore            │  ← Root of trust (AES-GCM keys)
└─────────────────────────────┘
```

## Secrets Handling (CI/CD)

Release signing uses **environment variables / GitHub Actions secrets** — never checked into the repo:

- `AIVANCE_STORE_PASSWORD`
- `AIVANCE_KEY_ALIAS`
- `AIVANCE_KEY_PASSWORD`
- `AIVANCE_KEYSTORE_BASE64` (CI decodes into `keystore.jks`)

`local.properties` in CI contains only `sdk.dir` and a test value — no real keys.

## App-Level Hardening

- `isMinifyEnabled = true` + `isShrinkResources = true` (R8) on release builds.
- ProGuard mapping files uploaded as CI artifacts (kept private, 90-day retention).
- Native debug symbols (SYMBOL_TABLE) retained for crash symbolication only.
- No experimental features, no debug backdoors in release.

## Data Safety (Google Play)

- **Collected data**: career content (resumes, applications, analytics) stored locally.
- **Shared data**: none to third parties; provider API calls transmit only what the user's configured providers need.
- **Deletion**: full local data wipe available in Privacy Center.

## Incident Response

1. **Detection**: crash reporting (Crashlytics/`CrashReporter`), CI security-scan job, audit logs.
2. **Triage**: classify (crash / data / credential). Credential incidents take priority.
3. **Containment**: revoke provider API keys via the provider consoles; rotate signing keys if the keystore leaks.
4. **Eradication**: patch, release hotfix, add regression test.
5. **Post-mortem**: record in `IMPLEMENTATION_LOG.md`; update this guide if the threat model changes.

## Security Checklist (pre-release)

- [ ] `assembleRelease` with R8 passes.
- [ ] ProGuard mapping stored securely.
- [ ] No `Log` statements emit API keys (verified by telemetry scrubber).
- [ ] `local.properties` contains no real secrets in CI.
- [ ] Privacy Center export/delete flows work.
- [ ] DB v20 migration chain verified on upgrade from v19.
