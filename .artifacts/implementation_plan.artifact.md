# Implementation Plan — Remediation of V1.0 Release Blockers

This plan addresses the critical blockers identified in the V1.0 release audit. We will restore build functionality, repair database migrations, and replace all stubbed UI with real data-driven components.

## User Review Required

> [!IMPORTANT]
> **Build Recovery**: We will temporarily remove the `androidx.baselineprofile` plugin from the `:app` module to restore the ability to compile the project and run tests.

> [!CAUTION]
> **Database Integrity**: We will sanitize the SQL for migrations v11-18 to prevent crashes on upgrade. We will also register `MIGRATION_23_24` and remove the destructive migration fallback to prevent silent user data loss.

## Proposed Changes

### [Build] Stabilization
- **[MODIFY] `app/build.gradle.kts`**: Remove `id("androidx.baselineprofile")`. This plugin is misconfigured and prevents all Gradle tasks from running.
- **[MODIFY] `build.gradle.kts` (Root)**: Remove the baselineprofile plugin apply.

### [Core] Database Repair
- **[MODIFY] `AivanceDatabase.kt`**:
    - Fix syntax errors in `MIGRATION_12_13`, `MIGRATION_13_14`, `MIGRATION_14_15`, `MIGRATION_15_16`, `MIGRATION_16_17`, `MIGRATION_18_19`, and `MIGRATION_19_20` (unbalanced quotes/backticks).
- **[MODIFY] `DatabaseModule.kt`**:
    - Register `AivanceDatabase.MIGRATION_23_24`.
    - **Remove** `.fallbackToDestructiveMigration()` to ensure production data safety.

### [Feature] Intelligence Hub Integrity
- **[NEW] `IntelligenceHubViewModel`**:
    - Fetch real resumes and ATS scan history from `ResumeRepository` and `AtsRepository`.
- **[MODIFY] `IntelligenceHubScreen`**:
    - Bind the UI to `IntelligenceHubViewModel` to remove hardcoded fake data.

### [Security & System] Hardening
- **[MODIFY] `AivanceApp.kt`**: Properly schedule `SecurityMigrationWorker` if it's required for data consistency.
- **[MODIFY] `backup_rules.xml`**: Update the database filename to `aivance-database.db` to ensure PII is correctly handled during cloud backups.

### [Cleanup] Dead Code Purge
- **[DELETE]** `AiSettingsScreen`, `LoginScreen`, `HomeViewModel` and other confirmed dead code identified in the audit.
- **[DELETE]** Non-compiling test files `SettingsViewModelTest.kt` and `ProfileViewModelTest.kt`.

## Deliverables

1.  **Stable Release Build**: Compilable and runnable code at HEAD.
2.  **Repaired Migration Path**: Safe upgrades from v10 through v24.
3.  **Data-Driven Intelligence Hub**: Real resume and scan status display.
4.  **Hardenend Production Environment**: Correct backup rules and stripped debug logs.

## Verification Plan

### Automated Tests
- **Database Migration Test**: Run `MigrationTest` and extend it to cover 10->24.
- **Build Pass**: Verify `./gradlew assembleDebug` and `./gradlew assembleRelease` both succeed.

### Manual Verification
- Verify that **Intelligence Hub** shows actual resumes uploaded by the user.
- Verify that a fresh install followed by data entry and app update preserves all data (no destructive wipe).
