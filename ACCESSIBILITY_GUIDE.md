# AiVance Accessibility Guide

Accessibility is enforced at the design-system level so features inherit it by default.

## Core Requirements

| Requirement | Enforcement |
| :--- | :--- |
| **Contrast ≥ 4.5:1** for body text | Theme roles pair `on*` colors with containers; error/warning containers tuned in both light and dark palettes |
| **Touch targets ≥ 48dp** | All buttons 48dp tall; icon buttons ≥ 48dp via modifiers; card taps have adequate padding |
| **Dynamic font scaling** | Layouts use relative `dp` + `MaterialTheme.typography`; no fixed-size text or clip-bound single-line content for critical text |
| **TalkBack / screen readers** | Every interactive element has content; icons use `contentDescription` (decorative icons use `null`); charts expose summary text |
| **Reduced motion** | All animations flow through `AivanceTheme.motion` and respect `AccessibilityManager` disable-animations; key content is never motion-dependent |
| **High contrast** | Dark and AMOLED themes maintain full contrast; semantic colors (success/error) remain distinguishable |
| **Keyboard navigation** | Focusable controls follow reading order; dialogs/bottom sheets trap focus correctly |

## Screen-Reader Patterns

- **Screens**: `AivanceScreen` content is read in layout order; headers carry their role via type style.
- **Score gauges**: `ScoreGauge` exposes the numeric score as semantics (e.g., "ATS score, 85 out of 100") — never rely on the arc alone.
- **Charts**: provide a `contentDescription` or adjacent summary metric so the data is reachable without vision.
- **Kanban (Tracker)**: each card is a single focusable unit; drag-and-drop is a pointer enhancement — card taps still move stages, so screen-reader users have an equivalent path.
- **Streaming (Assistant)**: streaming text announces incrementally; the typing indicator is non-annoying (role `polite` suppressed until content arrives).

## Writing Guidelines

- Label text fields (OutlinedTextField `label`/`placeholder`).
- Buttons describe the action: "Import Resume (PDF/DOCX)", not "Import".
- Error messages explain the fix: "Provider unavailable — check your API key", not "AI failed."
- Empty states read as instructions, not dead ends: "No applications yet — track your first application."

## Verification Checklist

1. Enable TalkBack and traverse every screen (top to bottom) — nothing unlabeled, focus order sensible.
2. Set font scale to 200% and confirm no text truncation or clipped tap targets.
3. Enable "Remove animations" in system settings — confirm screens remain fully usable and nothing hangs.
4. Force dark theme and check contrast on error/warning chips and gauges.
5. Run Accessibility Scanner on Dashboard, Tracker, Assistant, Analytics, and Profile.
6. Verify 48dp minimum touch targets via layout inspector on all tappables.

## Tooling

- Android Studio Layout Inspector (semantics tree).
- Accessibility Scanner (instrumentation).
- Compose `semantics` modifier for custom accessibility nodes.
- Preview both light and dark for every component (`@Preview` with `darkTheme = true/false`).
