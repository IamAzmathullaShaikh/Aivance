# Task: Remediation of V1.0 Release Blockers

This task tracks the critical fixes required to certify AiVance v1.0 for release.

## Status: Execution

- `[/]` Phase 1: Build Recovery & Build Optimization
    - `[ ]` Remove failing `baselineprofile` plugin from `:app`
    - `[ ]` Audit `app/build.gradle.kts` for release-ready minification
- `[ ]` Phase 2: Database Migration & Integrity
    - `[ ]` Sanitize migration SQL strings in `AivanceDatabase.kt`
    - `[ ]` Register `MIGRATION_23_24` in `DatabaseModule`
    - `[ ]` Remove `fallbackToDestructiveMigration` from Room builder
- `[ ]` Phase 3: UI Data Integrity (Intelligence Hub)
    - `[ ]` Implement `IntelligenceHubViewModel` for real data binding
    - `[ ]` Update `IntelligenceHubScreen` to remove hardcoded strings
- `[ ]` Phase 4: Security & System Hardening
    - `[ ]` Correct database filenames in `backup_rules.xml`
    - `[ ]` Add `POST_NOTIFICATIONS` permission request flow
- `[ ]` Phase 5: Repository Cleanup
    - `[ ]` Delete identified dead code modules/screens
    - `[ ]` Fix or delete non-compiling test files
    - `[ ]` Final repository health re-audit
