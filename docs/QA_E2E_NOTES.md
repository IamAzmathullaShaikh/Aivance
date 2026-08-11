# QA E2E Notes — Real-API Walkthrough (2026-08-11)

Companion to `RELEASE_CHECKLIST.md`. This is the **evidence log** for the live
end-to-end pass with real Apify + Groq keys on the `aivance` emulator
(x86_64, Android 11), including every navigation/feature touched, what was
found, and what was fixed.

## 1. Real provider keys

| Provider | Key | Result |
|---|---|---|
| Groq | `gsk_opeeq…6AM9` | ✅ Entered via Provider Management → saved → **HEALTHY** (live `GET /models` 200) |
| Apify | `apify_api_cpq4…ES8K` | ✅ `GET /v2/acts?token=…` **HTTP 200**; actor run POST **HTTP 201** |
| Apify (mistyped) | `apify_api_CPQ4…` (uppercase) | ❌ 401 on every endpoint — Apify tokens are case-sensitive. Was stored by an earlier typo; corrected via the LinkedIn card's Apify API Key field |

**Bug found (fixed)**: the stored token had `CPQ4` uppercased. Requests logged
in OkHttp showed `token=apify_api_CPQ4Uvdw…` → `Apify run failed to start: 401`
and the circuit breaker tripped LinkedIn to DEGRADED. Re-entering the key with
the correct case fixed it — verified by the live request log
(`token=apify_api_cpq4Uvdw…`) and the subsequent successful actor run.

## 2. Filters do NOT block results

The zero-result trap was investigated end-to-end:

- With every filter at "All" (`Country/State/City/Workplace/Type/Remote`),
  `JobSearchFilter` has empty lists/nulls and `JobFilterMatcher` skips
  filtering for empty collections — nothing is dropped client-side.
- The `Experience` dropdown label "Experience" is the placeholder for
  `minExperienceYears == null` (`experienceLabel()` in `JobsScreen.kt`) — it is
  **not** an active filter.
- The real causes of a zero-result search were provider-side: the Apify actor
  returned evergreen junk (keyword ignored → filtered out by the client query
  pass) and the free feeds had few matching listings for narrow queries.
- **Proof of real results**: query `developer` rendered
  `Senior Independent Software Developer — A.Team — 80 Good Match —
  Remote · Contract`, `Full-Stack Developer / App / AI (m/w/d) — Factory
  Innovations GmbH — Berlin`, etc. Persisted row counts:
  `1755 arbeitnow · 114 remoteok · 100 jobicy · 20 remotive · 100 linkedin`.

## 3. Bugs found & fixed

### 3.1 CRASH — Gemma model download (foregroundServiceType) ★
- **Repro**: onboarding → select Gemma (On-device) → Continue starts the model
  download worker.
- **Crash** (logcat 18:51:54):
  ```
  java.lang.IllegalArgumentException: foregroundServiceType 0x00000001 is not a
  subset of foregroundServiceType attribute 0x00000000 in service element ...
  androidx.work.impl.foreground.SystemForegroundService.startForeground(...)
  ```
- **Root cause**: `GemmaModelDownloadWorker` requests
  `FOREGROUND_SERVICE_TYPE_DATA_SYNC` but the merged manifest declared no
  `foregroundServiceType` on `SystemForegroundService` (required on Android 14+).
- **Fix** (`app/src/main/AndroidManifest.xml`): added
  `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC` permissions and
  `<service android:name="androidx.work.impl.foreground.SystemForegroundService"
  android:foregroundServiceType="dataSync" tools:node="merge"/>`.
- **Verified live**: after reinstall, WorkManager resumed the persisted
  download — foreground service started (visible in `dumpsys activity
  services`), progress advanced `0.08704 → 0.08705 → …` in
  `WM-WorkProgressUpdater`, UI showed "Downloading model… 8%", app stayed alive.

### 3.2 Apify DTO schema mismatch
- Real actor items emit `companyName`, `postedDate`, `contractType`; the DTO
  only read `company`, `postedAt`, `type` → company/type/date silently lost.
- Fixed with `@SerialName` fallbacks + mapper preferring the real-schema value.

### 3.3 Apify actor input mismatch
- `{"search": …}` is ignored by the LinkedIn actors — every run returned
  evergreen postings ("General Apply", "JOIN THE Family", "Careers",
  "Don't fit another role…") regardless of keyword (reproduced with curl on
  both `valig~linkedin-jobs-scraper` and `curious_coder~linkedin-jobs-scraper`,
  with and without `keywords`/`locationIds`).
- **Final verified input** (live, 2026-08-11): the actor keys off its AI-search
  filters — `keywords` (string) + `location`/`country` + `maxItems`. With
  `keywords="Android Engineer"` + `location="United States"` it returns real
  keyword-relevant LinkedIn roles: Software Engineer II, Android Engineering
  (Axon) · Software Engineer II, Android (Pinterest) · Software Engineer,
  Android — All Teams (DoorDash) · Android Engineer, Applied Foundations
  (OpenAI) · Stellantis · EVgo · Red Cat Holdings · Waymo… `positions[]` with an
  empty location returns an explicit actor error ("Provide either LinkedIn jobs
  search URLs, or fill in the AI search filters (keywords, location, etc.)").
- **Poll budget**: the actor takes 60–120s+; the old 30×2s (60s) poll budget
  timed out before the dataset was ready (run `C3Tc7OFyczet2uDyg`), so LinkedIn
  silently contributed nothing. Bumped to 90×3s (≈4.5 min), covering the
  actor's 300s timeout.
- Switched the LinkedIn provider to the canonical
  `curious_coder~linkedin-jobs-scraper`. Client-side keyword filtering still
  trims non-matching items.

### 3.4 Apify free-tier quota exhaustion (upstream, not an app bug)
- After ~8 verification runs the token's free-tier monthly compute budget was
  exhausted: new runs return **403 "Monthly usage hard limit exceeded"**
  (user plan FREE, $5/month, 625 compute units). The app degrades gracefully:
  circuit breaker trips LinkedIn, then the per-provider cache fallback returns
  only LinkedIn's own cached jobs ("Network failed for linkedin, returning 1
  cached jobs") — no cross-provider echo.

## 4. Navigation & features exercised

- **Auth**: fresh account `qa.e2e.2026@gmail.com` created via Continue —
  Welcome → onboarding → Dashboard.
- **Onboarding**: Gemma step shows **Download model** button, no URL field
  (previous session's fix still intact); Continue gated on download.
- **Dashboard**: greeting, Naukri chip, Career Score 18, ATS Match 0%,
  quick commands, suggested advice — all render.
- **Identity Hub** (Dashboard → profile icon): tabs Identity / Preferences /
  Providers / Vault / System; Provider Center lists per-provider health +
  masked keys + refresh + toggle.
- **Provider Management** ("Manage Providers — API Keys & Models"): AI /
  Job / Enrichment sections; per-provider Save/Test; key fields (Groq API Key,
  Apify API Key, USAJobs, OpenAI, Hunter…).
- **Job Discovery**: query + structured filters; real results render below the
  filter panel (scroll past "Best match"); jobs persisted per source provider.
- **Intelligence Hub**: Your Resumes, Recent ATS Scans (legacy 0% report from
  a malformed earlier scan remains in the DB — flagged, see §6).

## 5. Screenshot / logcat backlog

- `/tmp/qa/screen_now.png` — Job Discovery with filters ("All").
- `/tmp/qa/dash.png` — Dashboard (compact foldable posture).
- logcat evidence: `Apify run started: 3IKzbuA9KFlNGHn27 (READY)`,
  `Provider linkedin request failed (3/3)` (pre-fix 401), dataset fetch of
  `linkedin.com/jobs/view/…` URLs (post-fix), `WM-WorkProgressUpdater` progress
  ticks, FATAL EXCEPTION trace §3.1.

## 6. Leftover / follow-ups

- The stale **0% ATS Scan** row in the Intelligence Hub (from the malformed
  `%20`-laden JD test) — harmless, but worth a "delete report" affordance.
- Free LinkedIn actors return evergreen postings for keyword searches — with
  the fixed input + client-side filter they no longer pollute results, but a
  paid actor (or `locationIds` from LinkedIn geo codes) is needed for
  genuinely keyword-targeted LinkedIn results.
- The in-memory backstack still resets the current tab on full process death
  (pre-existing architecture limitation; typed data and DB state persist).
