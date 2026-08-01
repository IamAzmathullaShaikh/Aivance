# AiVance Release Guide

This guide defines how AiVance versions are managed and how a release is cut, validated, and shipped.

## Versioning

- **Scheme**: Semantic Versioning (`MAJOR.MINOR.PATCH`).
- **Current**: `1.0.0` (`versionCode 1`).
- **Rules**:
  - `MAJOR` — breaking changes to public APIs, DB schema semantics, or design-system contracts.
  - `MINOR` — backward-compatible features.
  - `PATCH` — backward-compatible fixes.
- `versionCode` increments monotonically for every Play upload.
- Debug builds append `-debug` to `versionName`.

## Release Types (CI `workflow_dispatch`)

`release_type` input: `alpha`, `beta`, `rc`, `stable`. The production upload job currently targets the `production` track with a staged rollout.

## Release Process

### 1. Pre-release checks (Release Candidate)
- [ ] Full test suite green: `./gradlew testDebugUnitTest`.
- [ ] Lint + static analysis clean.
- [ ] `assembleDebug` and `bundleRelease`/`assembleRelease` succeed.
- [ ] Instrumented tests pass (CI: API 29 & 34).
- [ ] Manual QA checklist complete (see `TEST_PLAN.md`).
- [ ] `KNOWN_ISSUES.md` reviewed — no release-blocking issues.
- [ ] Telemetry sweep — no credentials in logs.

### 2. Bump version
- Update `versionCode` and `versionName` in `app/build.gradle.kts`.
- Update `CHANGELOG.md` under `[Unreleased]` → new version heading.

### 3. Tag
```bash
git tag -a v1.0.0 -m "AiVance 1.0.0 — Production Launch"
git push origin v1.0.0
```

### 4. Build & sign
- CI `build` job produces AAB + APK + mapping (signing via secrets).
- Verify artifacts: `app/build/outputs/bundle/release/app-release.aab`, mapping file.

### 5. Play Console submission
- **Recommended**: `upload-google-play` CI job (staged `userFraction 0.1`).
- **Manual alternative**: Play Console → App bundle explorer → upload AAB → release notes → rollout.
- Upload `mapping.txt` to Play Console for crash deobfuscation.

### 6. Post-release
- Monitor crash-free sessions and KPIs (see `OBSERVABILITY_GUIDE.md`).
- Freeze: **only hotfixes** on v1.0.0. Public APIs, DB schema, design system, provider SDK, navigation, and domain models are frozen.
- Record the release in `IMPLEMENTATION_LOG.md`.

## Release Notes Template

```markdown
### Highlights
- <feature>
### Fixes
- <fix>
### Known Issues
- See KNOWN_ISSUES.md (H-01 …)
### Migration Notes
- Database v20; no breaking data changes for v19 → v20 upgrade.
```

## Rollback Plan

1. Play Console → roll back to previous version.
2. If data-affecting (e.g., migration issue), halt rollout immediately; ship hotfix.
3. Post-mortem recorded; regression test added.
