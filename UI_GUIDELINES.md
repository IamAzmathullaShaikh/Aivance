# AiVance UI Guidelines

These guidelines govern how screens are designed and implemented. They are binding for all feature modules.

## Screen Anatomy

Every screen follows the same skeleton:

1. **Top bar** — shared `AivanceTopBar` (or Material `TopAppBar` with transparent container) with a bold title.
2. **Content** — `AivanceScreen` scaffold that composes loading, error, empty, and content states.
3. **Bottom bar / FAB** — only for root destinations; detail screens return via back.

Root destinations use bottom navigation (via the AppShell); nested destinations push onto the back stack with fade transitions.

## State Coverage — Every State, Everywhere

A screen must render all five states — there are no exceptions:

| State | Component | Notes |
| :--- | :--- | :--- |
| Loading | `AivanceLoading` / `SkeletonCard` | Skeletons for content-heavy lists; full-screen spinner only for cold loads |
| Empty | `AivanceEmptyState` | Illustration circle + title + explanation + primary action + secondary action |
| Success | Content | The main content, always live — no mock data |
| Error | `AivanceError` | Human-readable message + Retry; explain the failure, never "Something went wrong" |
| Partial | Inline banners / offline chips | Progress while streaming or background sync |

### Empty State Copy Pattern

- **Title**: the noun ("No Interviews").
- **Explanation**: one sentence on why and what to do ("Start a mock session to practice for your target role.").
- **Primary action**: the single most valuable next step ("Start Practice").
- **Secondary action**: the alternative ("Browse Jobs").

## Typography & Hierarchy Rules

- One `headline*` per screen, for the screen identity.
- Section headers use `titleMedium` + SemiBold; body content uses `bodyMedium`.
- Numbers that matter (scores, counts) use Display/Headline + Bold.
- Never use color alone to convey meaning — pair with text (e.g., chips carry labels, not just tints).

## Interaction Rules

- Every tappable element is ≥ 48dp tall (buttons are exactly 48dp via the design system).
- Buttons: one primary action per view; secondary actions outlined; tertiary as text.
- Destructive actions use error tones and require a confirm dialog.
- Disabled controls are disabled for a reason — provide helper text ("Add a target role to continue").

## Navigation

- Type-safe routes in `navigation/Destination.kt`; no string route literals.
- Root destinations: Dashboard, Jobs, Pipeline (Tracker), Assistant, Analytics, Profile.
- Detail flows (Resume, ATS, Interview, Recruiter, Cover Letter) push with fade transitions.
- Deep links route into feature detail screens; the AppShell re-reads onboarding state on start.

## Data & Honesty Rules

- **No static placeholder data.** If a use case isn't available, render the honest loading or empty state.
- **No dead controls.** Every button dispatches a real event; TODO click handlers are forbidden.
- Optimistic updates (e.g., Kanban drag) update the UI immediately and reconcile from the repository.
- Streaming content (assistant) shows typing indicators and incremental text; never fake latency.

## Tablet & Foldable Support

- Use `AdaptiveNavigationSuite` in AppShell for navigation rail/navigation bar switching.
- Content uses `weight`-based layouts that reflow; detail screens cap readable width.
- Landscape and multi-window must remain usable — verify with the responsive previews.

## Code Organization

- Screen → `XxxScreen.kt` (composables only), state via `XxxViewModel` (business logic), events via `XxxUiEvent`.
- Design tokens imported from `:core:designsystem`; never duplicate theme code in feature modules.
- Strings should be localized (hardcoded English is tolerated only for in-progress features, tagged for extraction).
