# Aivance — Full System Audit (Track 1)

Repo pinned: master @ 3cacfcf0 · commit IamAzmathullaShaikh/Aivance  
Auditor role: Principal Software + Android + UX Architect · Repo Auditor · TPM · QA Lead · Product Architect  
**Read-only.** No source modifications.

## Conventions
- All paths relative to repo root
- All claims cite file:line or attached artifact
- Every cross-reference links the actual deliverable in this .audit/ tree

---

## Stage 1 — File Inventory (DONE)

Inventory of 1006 source files (no `build/`, `.gradle/`, `.kotlin/`, `.idea/`, `.freebuff/`, `.artifacts/`, agent/agents/claude/github dir contents).

Outputs:
- `.audit/file-inventory.csv` — all 1006 files with `path,module,size,last_modified,ext`
- `.audit/module-rollup.csv` — per-module `.kt` file count + LOC + total files

### Module rollup
| Module | .kt | LOC | Total |
|---|---:|---:|---:|
| app | 26 | 2,903 | 56 |
| core/common | 18 | 2,545 | 19 |
| core/domain | 121 | 6,431 | 122 |
| core/data | 55 | 6,061 | 56 |
| core/database | 70 | 3,464 | 90 |
| core/datastore | 8 | 513 | 9 |
| core/util | 11 | 929 | 12 |
| core/designsystem | 21 | 3,188 | 22 |
| core/sdk | 23 | 1,773 | 24 |
| core/ai-providers | 12 | 1,340 | 13 |
| core/job-providers | 51 | 4,396 | 52 |
| core/enrichment-providers | 5 | 440 | 6 |
| core/network | 4 | 536 | 5 |
| feature/dashboard | 14 | 1,752 | 17 |
| feature/resume | 5 | 2,251 | 8 |
| feature/ats | 5 | 813 | 8 |
| feature/coverletter | 3 | 781 | 6 |
| feature/tracker | 8 | 1,215 | 11 |
| feature/interview | 9 | 596 | 10 |
| feature/jobs | 16 | 3,182 | 19 |
| feature/profile | 23 | 4,893 | 26 |
| feature/recruiter | 3 | 515 | 6 |
| feature/analytics | 3 | 439 | 6 |
| feature/assistant | 3 | 1,048 | 6 |
| navigation | 13 | 3,009 | 16 |
| **Sum (modular)** | **528** | **51,491** | **611** |
| root-level / docs / scripts | 107 | 10,150 | (n/a) |
| **TOTAL Kotlin** | **635** | **~61,641** | **1006** |

> Note: `core/resume` and `core/tracker` are referenced inside `core/data`; `feature/profile` at 23 files is by far the largest single feature module. Will be a focus in next stages.

### Observations from inventory alone (no claim yet — to verify in Stage 2)

1. **`feature/profile` has 23 .kt / 4.9k LOC** — outweighs every other feature except `feature/jobs` (16 / 3.2k). Profile is doing a lot more than just "profile" — needs Module-level analysis.
2. **`core/job-providers` has 51 .kt / 4.4k LOC** — the largest core module. Provider abstraction is a strategic surface area.
3. **`core/database` has 70 .kt / 3.5k LOC** but `feature/resume` is only 5 .kt / 2.3k LOC — Room schema is heavy; verify whether feature modules are thin by design (delegate to repos) or whether UI/feature code is missing.
4. **`feature/recruiter`, `feature/analytics`, `feature/assistant`, `feature/coverletter`** each ≤3 files / ≤1.1k LOC — candidate "incomplete or dead" zones to verify next.

Next: Stage 2 — Function-level reverse-engineering starting from `:core:domain` (most abstractions, smallest blast radius).
## Stage 2 — Tier 1 Checkpoint: `core/*` modules

Scope: `:core:common :core:domain :core:data :core:database :core:datastore :core:util :core:designsystem :core:sdk :core:ai-providers :core:job-providers :core:enrichment-providers :core:network`

### Tier 1 inventory (`-function-catalog.tsv` appended to `.audit/function-catalog.core/`)

| Module | .kt | Top-level decls | Specialization |
|---|---:|---:|---|
| core/common | 18 | 163 | Enums (17), DTOs, exceptions, mappers, shared domain models |
| core/util | 11 | 20 | small utility layer |
| core/domain | 121 | 175 | Use cases per feature (ai, analytics, assistant, ats, career, coverletter, crm, interview, job, provider, resume, settings, user, workflow) |
| core/datastore | 8 | 12 | preferences + key-value |
| core/data | 55 | 130 | repositories (per-domain Impls), EntityMappers (61 decls!), local data sources, telemetry impls |
| core/database | 70 | 82 | 19 DAO interfaces, entities, converters, migrations |
| core/network | 4 | 4 | thin: NetworkModule + CertificatePinningInterceptor + SecurityUtils + a test |
| core/designsystem | 21 | 117 | Theme + components (AivancePrimaryButton / Secondary / Tertiary, Empty/Loading/Success/Error, Elevation, Shapes, Spacing, TopBar, Motion) |
| core/sdk | 23 | 34 | `AIProvider`, `EnrichmentProvider`, `JobProvider`, `OcrProvider`, `ResumeParserProvider` (abstract), `ProviderManager/Registry/Factory`, `BaseProvider`, `ProviderConfiguration`, `SecretManager`, sealed `AivanceSdkException`, telemetry/error mapping |
| core/ai-providers | 12 | 22 | `ClaudeProvider`, `GeminiAIProvider`, `OpenAIProvider`, `GroqProvider`, `OllamaProvider`, `OpenRouterProvider`, `OpenAiBaseProvider`, DTOs |
| core/job-providers | 51 | 69 | `AdzunaProvider`, `ApifyJobProvider`, `ArbeitnowProvider`, `GreenhouseProvider`, `IndeedProvider`, `LeverProvider`, `RemoteOKProvider` (sample), `RestJobProvider` base, `JobCache` (Memory/Room), `RetryInterceptor`, `UserAgentInterceptor`, DTOs |
| core/enrichment-providers | 5 | 10 | `HunterEnrichmentProvider` only — **single enrichment provider, candidate thin/dead** |
| **TOTAL** | **429** | **838** | |

> `data class` appears in 230 rows and `class` in 258 — the codebase is **DTO/entity-heavy**, typical of an integration platform.

### Five strategic findings — evidence-cited

#### 1. Provider SDK abstraction is mature, isolated, and testable — ✅ GREEN
**Evidence**: `core/sdk/src/main/kotlin/com/bangersoul/aivance/sdk/api/` defines **5 abstract provider contracts** (`AIProvider`, `EnrichmentProvider`, `JobProvider`, `OcrProvider`, `ResumeParserProvider`). Infrastructure (`ProviderRegistry`, `ProviderManager`, `ProviderFactory`) is Hilt-injected and unit-tested (`ProviderManagerTest`, `ProviderRegistryTest`, `SecretManagerTest`). Configuration via `ProviderConfiguration` + per-field `ConfigField`/`FieldType` metadata is a clean schema pattern.
**Implication**: Switching AI/job/enrichment providers is a first-class concern, not bolted on. Good foundation.

#### 2. AI providers — 6 implemented, real coverage ✅ 🟡
**Evidence** (`core/ai-providers/`):
- Claude (`ClaudeProvider`, `ClaudeApi`, `ClaudeMessageRequest`, `ClaudeMessageResponse`, `ClaudeStreamEvent`, `ClaudeDelta`, `ClaudeUsage`)
- Gemini (`GeminiAIProvider`)
- OpenAI-compatible: `OpenAiBaseProvider` + `OpenAIProvider`, `GroqProvider`, `OpenRouterProvider`, `OllamaProvider`
- One test file: `OpenAiApiSerializationTest`
**Gap**: 5 of 6 providers have **no provider-specific tests**. Streaming code paths rely on provider-shared `OpenAiBaseProvider`, which means Claude/Gemini/Groq/OpenRouter/Ollama miss unit coverage on top of base.

#### 3. Job providers — 7+, cache layer with persistence + memory ✅
**Evidence** (`core/job-providers/`):
- Concrete: `AdzunaProvider`, `ApifyJobProvider`, `ArbeitnowProvider`, `GreenhouseProvider`, `IndeedProvider`, `LeverProvider`, `RemoteOKProvider`
- Cache abstraction: `JobCache` interface with `MemoryJobCache` and `RoomJobCache` (so caching **does** survive across sessions)
- HTTP plumbing: `RestJobProvider` abstract base, `RetryInterceptor`, `UserAgentInterceptor`, DTOs per provider
- DI: `JobProvidersModule` (abstract — concrete providers must be contributed by `app/`)
**Observation**: `RoomJobCache` exists, but I haven't yet confirmed which DAO columns it persists. **Verify in Stage 3**.

#### 4. Enrichment providers — only 1: possible gap vs brief 🟡
**Evidence** (`core/enrichment-providers/`): only `HunterEnrichmentProvider`, only `HunterApi`, only Hunter DTOs, only `HunterEnrichmentProviderTest`. The brief promises "Enrichment Providers" (plural) for **Company Intelligence** and **Recruiter Intelligence**. Just one provider means company/recruiter data sources for these two features **all flow through Hunter** — that's a single point of failure.
**Action**: verify in Stages 7–8 how `CompanyIntelligenceRepository` and `RecruiterIntelligenceRepositoryImpl` source their data and whether fallback/secondary providers are wired.

#### 5. Network module — minimal but likely incomplete 🟠
**Evidence** (`core/network/`): only 4 files:
- `NetworkModule` (Hilt)
- `CertificatePinningInterceptor`
- `SecurityUtils`
- 1 test for the interceptor
- **No** retry layer, auth-injector, request-id interceptor, host whitelist **beyond pins**, error mapping, refresh-token plumbing.
**However**: `RetryInterceptor` and `UserAgentInterceptor` live in `core/job-providers/base/`. So retry lives in the wrong layer **if other call sites also need it** (AI provider calls, enrichment calls).
**Action**: verify in Stage 3 whether AI/enrichment clients share a single Retrofit instance or each builds its own. If each builds its own, retry duplication is real.

### Cross-cutting observations

- **Test coverage is uneven**: `core/domain`, `core/data`, `core/database`, `core/sdk` have tests. `core/common`, `core/datastore`, `core/designsystem`, `core/util` do not (or barely). AI providers mostly untested.
- **`EntityMappers.kt` in `core/data` has 61 top-level declarations** — likely a god-file. Verify in Stage 3 whether it should be split per feature (mapper/ResumeMapper, mapper/JobMapper, …).
- **Two "shared domain model" homes**: domain models live in both `core/common/model/` (DTO-leaning) and `core/domain/model/` (clean domain). Verify if this is the intended two-tier mapping (DTO ↔ Domain) or accidental duplication. **Strategy will be clarified in Stage 3 after I read both layers.**
- **Provider abstractions are package-pure**: provider packages are NOT under `:core:sdk` — `:core:ai-providers`, `:core:job-providers`, `:core:enrichment-providers` are siblings. That means the **boundary is "abstract contracts vs concrete impls"**, which is exactly the right split for a multi-provider system. The only seam that worries me is `core/job-providers/base/RetryInterceptor` — should retry belong to `core/network`?

### What I'll do next (Tier 2 = `feature/*` modules, 11 features)

1. Build same TSV catalog for `feature/*` (expect ~3,500 decls).
2. Cross-link each feature's ViewModels → UseCases → Repositories → Providers.
3. Surface the "thin" features (`recruiter`, `analytics`, `assistant`, `coverletter` — all ≤3 files at top level).
4. Confirm or refute: does each feature have navigation entry? Where is the **Dashboard** (the Career HQ) assembled?

Will checkpoint at the end of Tier 2.

