# Implementation Plan — Milestone 12: Hardening & V1.0 Certification

This milestone transitions AiVance from a feature-complete repository to a **Production Release Candidate**. We will focus on release engineering, performance optimization, security hardening, and final repository stabilization.

## User Review Required

> [!IMPORTANT]
> **Production Hardening**: We will enable full R8 optimizations and resource shrinking. Any external dependencies that are not release-ready will be audited.

> [!WARNING]
> **CI/CD Stabilization**: We will formalize the GitHub Actions workflows for automated release candidate generation.

## Proposed Changes

### [Release] Build Engineering
- **[MODIFY] `build.gradle.kts` (App)**: 
    - Finalize `versionCode` and `versionName` for 1.0.0.
    - Ensure `isMinifyEnabled = true` and `isShrinkResources = true` are optimized.
    - Configure `optimization { enable = true }` for better R8 results.
- **[MODIFY] `proguard-rules.pro`**:
    - Final audit of keep rules to ensure minimal binary size without breaking runtime reflection (Hilt, Serialization, Room).

### [Performance] Macrobenchmark & Profiles
- **[NEW] `:benchmark` module**:
    - Implement `StartupBenchmark` to measure cold/warm starts.
    - Implement `ScrollBenchmark` for Hub lazy lists (Intelligence, Discovery, Pipeline).
- **[NEW] Baseline Profiles**:
    - Generate and bundle a baseline profile to eliminate first-launch JIT lag and improve startup time by ~20%.

### [Security] Production Audit
- **[AUDIT] AndroidKeyStore**: Verify that `CryptoManager` is correctly isolating keys and handling hardware-backed security where available.
- **[AUDIT] Network Security**: Implement a production-grade `network_security_config.xml` with strict TLS requirements.
- **[AUDIT] ProGuard assumed-no-side-effects**: Ensure all `Timber.d` and `Log.v` calls are stripped from the release binary.

### [Accessibility & Localization]
- **[VERIFY] String Coverage**: Ensure 100% translatable string coverage for English and Hindi.
- **[VERIFY] Semantic Labels**: Audit all `IconButton` and `Image` components for meaningful `contentDescription` values.

### [Documentation] Release Artifacts
- **[NEW] `PRODUCTION_CHECKLIST.artifact.md`**: A final validation list before Play Store submission.
- **[UPDATE] `Architecture.md`**: Final 1.0 architecture diagram and module documentation.
- **[NEW] `V1_0_RELEASE_NOTES.md`**: Public-facing changelog.

## Verification Plan

### Automated Tests
- **Release Build Compilation**: Successfully generate a signed AAB using `./gradlew bundleRelease`.
- **Benchmark Pass**: Run macrobenchmarks and verify they stay within the < 2s startup target.

### Manual Verification
- Perform a **Process Death Recovery** test on the signed release binary.
- Verify **Biometric Lock** functionality on a real physical device (or biometric-enabled emulator).
- Audit the **Release APK** size to ensure it is under the target threshold (e.g., < 15MB).
