# AiVance Component Library

All shared UI lives in `:core:designsystem` (`com.bangersoul.aivance.core.designsystem.components`). Features must not re-declare these components. This catalog is the canonical reference.

## Buttons — `AivanceButtons.kt`

| Component | Use when | Notes |
| :--- | :--- | :--- |
| `AivancePrimaryButton(text, onClick, icon?, enabled?)` | The single key action | 48dp, primary container |
| `AivanceSecondaryButton(text, onClick, icon?)` | Supporting action | Outlined, 48dp |
| `AivanceTertiaryButton(text, onClick, icon?)` | Low-emphasis / dismiss | Text button, 48dp |
| `ActionButton(text, onClick, containerColor?, contentColor?, icon?)` | Legacy wrapper | Prefer the Aivance* trio for new code |

## Cards — `AivanceCards.kt`

| Component | Use when | Notes |
| :--- | :--- | :--- |
| `MetricCard(label, value, trend?, icon?)` | KPI tile (score, count) | Headline value + optional trend color |
| `InsightCard(text, icon?)` | AI recommendation / tip | Accent icon chip + body text |
| `ActionCard(title, subtitle?, icon?, onClick?)` | Navigational tile | Icon block + title + subtitle |
| `ProgressCard(title, progress, valueLabel?, subtitle?)` | Goal/coverage progress | Animated progress bar |
| `StatusChip(text, tone)` | Pipeline status / labels | SUCCESS/ERROR/WARNING/INFO tones |
| `DashboardCard(onClick?, content)` | General content container | Tonal, border-outlined |

## States — `AivanceEmptyState.kt`, `AivanceSkeleton.kt`, `ErrorUI.kt`, `LoadingUI.kt`, `AivanceScreen.kt`

| Component | Use when |
| :--- | :--- |
| `AivanceEmptyState(title, description, icon?, primaryActionText?, onPrimaryAction?, secondaryActionText?, onSecondaryAction?, compact?)` | Any empty dataset |
| `SkeletonCard()` / skeleton block | Content loading in place of lists |
| `AivanceError(message, onRetry)` | Recoverable failures |
| `AivanceLoading()` | Full-surface loading |
| `AivanceScreen(isLoading, error, isEmpty, topBar, content, ...)` | Standard screen scaffold |

## Feedback — `AivanceBanner.kt`, `AivanceSuccess.kt`

- `AivanceBanner(message, tone)` — inline banners for partial/offline states.
- `AivanceSuccess(message)` — ephemeral success confirmation.

## Charts — `AivanceCharts.kt`

- `AivanceLineChart(data, ...)` — trends (career score, interview performance).
- `AivanceBarChart(data, ...)` — comparisons (section scores, funnel stages).
- `AivanceDonutChart(data, ...)` — proportions (keyword coverage).
- `ScoreGauge(score, maxScore?, size?, modifier?)` — animated radial score (ATS, Career Score, interview performance).

## Dashboard — `DashboardComponents.kt`

- Priority/task rows, quick-action grids, and insight rails used by the Dashboard Command Center.

## Top Bar — `AivanceTopBar.kt`

- Shared app-bar with title + back/actions; adaptive to navigation mode.

## AI — `AiComponents.kt`, `ChatComponents.kt`

- `KeywordChip(text, isMatched)` — keyword gap display (ATS/resume analysis).
- Typing indicator, streaming bubble, context cards, and suggestion chips for the Assistant.

## Guidelines

1. **Consume, don't copy.** Import from the components package; never inline re-implementations.
2. **Compose from tokens.** All components read `AivanceTheme.*`; they never hardcode colors/durations.
3. **Extend deliberately.** A new shared component belongs in `:core:designsystem` with a preview; a one-off layout belongs in its feature.
4. **Semantic labels.** Every Icon gets `contentDescription` (or `null` for decorative) — required for TalkBack.
