# AiVance Design System

The AiVance Design System (`:core:designsystem`) is the single source of truth for all visual and interaction tokens in the application. Every screen consumes these tokens — no UI component hardcodes raw values.

## Design Principles

1. **Clarity over decoration** — every element earns its place; information hierarchy is driven by type scale and spacing, not shadows.
2. **Productivity first** — the interface is a command surface: fast scanning, obvious next actions, zero dead ends.
3. **Consistency by construction** — screens are assembled from the component library; bespoke layouts are the exception, not the rule.
4. **Motion with meaning** — animations communicate state (loading, success, drag-in-progress) and never play for their own sake.
5. **Accessibility is non-negotiable** — contrast, touch targets, and reduced-motion are tokens, not afterthoughts.

## Token Architecture

Tokens are defined in `core/designsystem/src/main/java/com/bangersoul/aivance/core/designsystem/theme/`:

| Token Group | File | Exposed via |
| :--- | :--- | :--- |
| Color (Light/Dark/AMOLED/Dynamic) | `Color.kt` | `AivanceTheme.colors` |
| Typography | `Type.kt` | `MaterialTheme.typography` |
| Spacing (4/8/12/16/24/32dp) | `Dimens.kt` | `AivanceTheme.spacing` |
| Corner Radius (Small/Medium/Large/ExtraLarge) | `Shapes.kt` | `AivanceTheme.shapes` |
| Elevation (Flat/Low/Medium/High) | `Elevation.kt` | `AivanceTheme.elevation` |
| Motion (Fast/Standard/Slow) | `Motion.kt` | `AivanceTheme.motion` |
| Theme engine | `AivanceTheme.kt` | `AivanceTheme { }` |

### Color System

Semantic roles — components reference roles, never raw palette values:

- **Primary** — key actions, active navigation, focus states.
- **Secondary** — supporting actions and emphasis.
- **Surface / Background** — container hierarchy.
- **Success / Warning / Error / Info** — semantic feedback (each with a paired `*Container` / `on*Container` for chips and banners).

Theme variants: **Light**, **Dark**, **AMOLED** (pure-black background, reduced battery draw), **Dynamic Material You** (Android 12+ wallpaper sampling), and **custom accent** selection persisted in DataStore.

### Spacing System

The 4dp base grid: `extraSmall=4dp`, `small=8dp`, `medium=12dp`, `large=16dp`, `extraLarge=24dp`, `huge=32dp`. Page padding is `large` (16dp); card padding is `medium` (12dp); internal row gaps are `small` (8dp).

### Typography

Role-based type scale built on `MaterialTheme.typography`: **Display** (hero numbers), **Headline** (screen titles), **Title** (section headers and cards), **Body** (content), **Label** (captions, chips, buttons). Button text uses `labelLarge` + SemiBold.

### Motion

- **Fast** (120ms) — micro-interactions, icon toggles, chip selection.
- **Standard** (240ms) — navigation fades, card entrances, list reordering.
- **Slow** (400ms+) — score gauges, celebratory states, large layout changes.

Easing: `FastOutSlowIn` for entrances, `LinearOutSlowIn` for exits. All duration/easing values flow through `AivanceTheme.motion`; screens must never inline `tween(300)` literals.

### Elevation

Four levels: **Flat** (0dp), **Low** (1dp, resting cards on background), **Medium** (3dp, cards on surface, floating elements), **High** (6dp, modal sheets, drag targets). Prefer tonal differentiation (surface vs surfaceVariant) over elevation for hierarchy, per Material 3 guidance.

## Usage Rules

- Wrap the app in `AivanceTheme(darkTheme = ..., dynamicColor = ..., accent = ...)`; the theme reads persisted preferences via `UserPreferences`.
- Components live in `core/designsystem/components/` and are imported as `com.bangersoul.aivance.core.designsystem.components.*`.
- Preview composables use `AivanceTheme(darkTheme = true/false)` so both modes are exercised in Studio.
- Adding a new screen: assemble from the component library first; reach for custom Compose only when the library genuinely cannot express the layout.
