# AiVance Release Backlog — TODO

Prioritized, effort-tagged remediation backlog derived from the **Database Certification** (2026-08-03) and **Security Certification** (2026-08-04) sprints, plus the v1.0 audit risk register. Items are ready for sprint planning with acceptance criteria.

**Legend:** P0 = release blocker · P1 = before launch · P2 = post-launch / backlog

---

## P0 — Release blockers (must close before Play submission)

### P0-01 — Execute the instrumented database suite on a real device/emulator
- **Effort:** S
- **Area:** `:core:database`
- **Why:** All 24 migration tests + 11 DAO tests + 12 unit tests compile and the SQL is proven by byte-identical SQLite replay (`migration_validate.py`, `db_certify.py`), but the instrumented suite has never been executed on a device (no emulator in the certification environment).
- **AC:** `./gradlew :core:database:connectedDebugAndroidTest` passes on emulator + one physical device; migration tests 5→24 green; `PRAGMA foreign_key_check` = 0.

### P0-02 — Device-based dynamic security validation (MITM / pen-test pass)
- **Effort:** M
- **Area:** app runtime, `core:network`, `core:ai-providers`
- **Why:** Pinning, fail-closed crypto, and backup exclusions are verified statically + against live TLS chains (`security_scan.py`, 20/20). Phase 12–13 of the security brief (MITM simulation, cert failure, permission denial, process death) still needs on-device execution.
- **AC:** With mitmproxy/Charles CA installed, all 9 pinned hosts fail to connect (pinning actively blocks interception); offline/provider-failure/permission-denied paths degrade gracefully; no secrets in logcat.

### P0-03 — Release build + signing validation
- **Effort:** M
- **Area:** `:app`
- **Why:** Release `BuildConfig` now embeds no provider keys (verified), but a signed release AAB has not been produced since the build-config change.
- **AC:** `./gradlew bundleRelease` succeeds; installed release APK starts, authenticates, and exercises one provider request; no `apiKey`/`token` strings extractable from the release binary.

---

## P1 — Before launch

### P1-01 — Wire `security_scan.py` into CI as a release gate
- **Effort:** S
- **Area:** CI (GitHub Actions)
- **Why:** The harness re-verifies live pins, hardcoded secrets, BuildConfig hygiene, backup rules, and fail-closed crypto. It should block merges/releases on regression.
- **AC:** Workflow runs `python security_scan.py` on every PR and before release; non-zero exit fails the job; secrets are injected from CI store, never the repo.

### P1-02 — Scheduled live-pin re-verification + rotation runbook
- **Effort:** S
- **Area:** `CertificatePins.kt`, CI schedule
- **Why:** Pins are live-verified at certification time; CA/leaf rotations (esp. Amazon CA 1, GTS R4) will invalidate them. A scheduled job must flag drift before production breaks.
- **AC:** Weekly CI job compares registry against live SPKI hashes; any mismatch opens an issue/PR referencing the rotation runbook in `CertificatePins.kt`; out-of-band pin update path documented.

### P1-03 — Google Play Data Safety form + privacy policy from the certified architecture
- **Effort:** S
- **Area:** docs / Play Console
- **Why:** Play requires a data-safety declaration; the certified inventory (KeyStore encryption, TLS pinning, backup exclusions, no plaintext secrets) is the source of truth.
- **AC:** Data Safety answers match the security inventory; privacy policy published; backup exclusions reflected in the declaration.

### P1-04 — Post-restore key-rebinding UX for KeyStore-bound backups
- **Effort:** M
- **Area:** `core:util` `BackupSecurity`, `feature:profile` Privacy Center
- **Why:** Backup secrets are wrapped by a device-bound AndroidKeyStore key; restoring onto a different device/install requires the passphrase flow. Currently the trade-off is documented but not surfaced in the UI.
- **AC:** Import flow detects KeyStore-bound backup mismatch and guides the user to re-enter the export passphrase; no silent data loss.

---

## P2 — Post-launch backlog

### P2-01 — M-03: Interview analytics timeline accumulation
- **Effort:** M · **Area:** `feature:analytics`
- **AC:** Charts render meaningful history for new users (seed/derive from real session data, no fabricated values).

### P2-02 — L-02: Tautological initial-state tests
- **Effort:** S · **Area:** feature ViewModel tests
- **AC:** Assert-on-init tests strengthened to assert post-event state or removed.

### P2-03 — Dead code: `DatabaseManager` / `DatabaseSeed` in DI
- **Effort:** S · **Area:** `:core:database`
- **AC:** Confirm zero references outside `:core:database`; remove or document; compile + tests green.

### P2-04 — Migration-file line-ending normalization (LF vs CRLF)
- **Effort:** S · **Area:** `:core:database` `AivanceDatabase.kt`
- **AC:** Consistent repo-wide line endings; diff noise eliminated.

### P2-05 — Provider-log redaction coverage review
- **Effort:** S · **Area:** `core:ai-providers`
- **AC:** Any future header carrying secrets is added to the `redactHeader` list; documented in the logging checklist.

---

## Tracking

- Sprint status, acceptance evidence, and certification reports: `DATABASE_CERTIFICATION.artifact.md`, `SECURITY_CERTIFICATION.artifact.md`.
- Verification harnesses (executable evidence): `security_scan.py`, `migration_validate.py`, `db_certify.py`, `test_sql_check.py`.
- Known/remaining issues: `KNOWN_ISSUES.md`.
