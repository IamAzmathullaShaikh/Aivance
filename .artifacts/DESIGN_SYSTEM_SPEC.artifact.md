# AiVance Design System Specification

This document defines the visual identity and structural rules for the AiVance Career Operating System UI.

## 1. Design Philosophy: "The Precise Partner"

AiVance is not a tool; it's an OS. The design must feel **Instrument-like**, **Efficient**, and **Calm**.
- **Inspiration**: Linear (Speed), Notion (Structure), Arc (Identity).
- **Core Principle**: Information Density over Decoration.

---

## 2. Design Tokens

### Color Palette (The "Midnight Indigo" Palette)
| Token | Light Value | Dark Value | AMOLED Value | Role |
| :--- | :--- | :--- | :--- | :--- |
| `Surface` | Zinc 50 | Zinc 950 | Pure Black | The Canvas |
| `Primary` | Indigo 600 | Indigo 500 | Indigo 400 | Key Actions |
| `Secondary`| Zinc 600 | Zinc 400 | Zinc 300 | Meta-data |
| `Accent` | Violet 600 | Violet 500 | Violet 400 | Intelligence |
| `Error` | Rose 600 | Rose 500 | Rose 400 | Blockers |

### Typography (Inter Sans)
| Role | Size | Weight | Line Height | Letter Spacing |
| :--- | :--- | :--- | :--- | :--- |
| `Display` | 32sp | Bold | 40sp | -0.5sp |
| `Headline`| 24sp | SemiBold | 32sp | -0.25sp |
| `Title` | 18sp | Medium | 24sp | 0.1sp |
| `Body` | 14sp | Regular | 20sp | 0.25sp |
| `Caption` | 12sp | Regular | 16sp | 0.4sp |
| `Code` | 13sp | Mono | 18sp | 0 |

### Spacing (8dp Grid)
- `4dp` (XXS), `8dp` (XS), `16dp` (S), `24dp` (M), `32dp` (L), `48dp` (XL), `64dp` (XXL).

### Radius
- `8dp` (Small), `16dp` (Medium), `24dp` (Large), `Rounded` (Circle).

---

## 3. Component Library Catalog

### Inputs
- **`AivanceTextField`**: Outlined, floating label, support for icons.
- **`AivanceSearchBar`**: Pill-shaped, persistent, integrated filters.

### Selection
- **`AivanceSwitch`**: Branded indigo track.
- **`AivanceSegmentedControl`**: Zinc background, Indigo selection.

### Structure
- **`AivanceCard`**: 1px Zinc-800 border in Dark mode, subtle shadow.
- **`AivanceWorkspaceScaffold`**: Auto-handles Hub-to-Spoke transitions.

### Progress & Skeletons
- **`AivanceCircularProgress`**: Custom Indigo sweep.
- **`AivanceSkeleton`**: Neutral Zinc pulse.

---

## 4. Motion Guidelines

- **Transitions**:
    - **Enter**: `SlideInVertically` (Offset 24dp) + `FadeIn`.
    - **Exit**: `FadeOut`.
- **Shared Elements**: Icons/Images shared between Hub and Detail screens.
- **FPS Goal**: 60-120Hz (ProMotion aware).

---

## 5. Responsive & Accessibility Guide

### Adaptive Breakpoints
- **Compact**: Bottom Bar (Active hub is always centered).
- **Medium**: Nav Rail (Top-aligned).
- **Expanded**: Modal Nav Drawer (Persistent).

### Accessibility Standards
- **TalkBack**: Every interactive component must have a `contentDescription`.
- **Contrast**: Enforce `AA` compliance for all text vs background.
- **Reduced Motion**: Honored via system settings to skip slides/fades.

---

## 6. Figma Component Specification

- **Auto Layout**: Required for all component groups.
- **Variants**: Every component must include: `Default`, `Hover`, `Pressed`, `Loading`, `Disabled`.
- **Naming**: `[Component]/[Variant]/[State]` (e.g., `Button/Primary/Pressed`).

---

## 7. Design QA Checklist

- [x] Verified zero hardcoded hex values in component code.
- [x] Verified 48dp minimum touch target for all buttons.
- [x] Verified consistent 16dp horizontal page margin.
- [x] Verified AMOLED mode uses pure black `#000000`.
