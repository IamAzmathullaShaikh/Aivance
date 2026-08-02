# AiVance — Product Design Specification & System Reference

**Product Name**: AiVance  
**Tagline**: *AI-Powered Career Operating System*  
**Package**: `com.bangersoul.aivance`  
**Architecture**: Clean Architecture · Multi-Module Clean Code · Jetpack Compose · Material 3  

---

## 1. Product Vision & Operating System Concept

AiVance is not a collection of standalone utility screens — it is an **AI-Powered Career Operating System**. The interface is structured as an integrated command surface where every feature (Resume Engine, ATS Matcher, Job Search, Recruiter Discovery, Cover Letter Generator, Prep Studio, Application Pipeline) reads from and writes back to the central user career profile.

### Core Value Propositions & Primary Features
1. **Career HQ (Dashboard)**: The command central showing real-time Career Score (0–100), quick stats (Active Applications, ATS Average, Target Role Fit), quick actions, and context recommendations.
2. **Resume Optimizer & ATS Matcher (7-Step Pipeline)**: A linear step-by-step engine: `Import → Parsing → Preview → ATS → Optimize → Save → Export`. Features debounced live-reactive ATS scoring (re-evaluates when resume or job description changes) and token-by-token streaming keyword match analysis.
3. **AI Career Assistant**: A real-time command surface with streaming SSE response generation, intent-driven quick action chips ("Improve Resume", "Find Jobs", "Interview Prep", "Cover Letter"), provider health monitors, and voice/document/photo attachment affordances.
4. **Universal Job Tracker & Pipeline**: A 5-stage Kanban funnel (`Saved → Applied → Interview → Offer → Rejected`) with drag-and-drop/tap-to-move updates, application timeline history, notes, and salary tracking.
5. **Real Job Search & Client-Side Filtering**: Aggregated multi-provider search (RemoteOK, Remotive, Apify, Arbeitnow, Jobicy, Adzuna, USAJobs, Lever, Greenhouse) with multi-dimensional filtering across Location, Employment Type, Workplace, and Experience.
6. **Prep Studio & AI Interview Coach**: Question Bank categorized by domain (Behavioral, Technical, System Design, HR) with difficulty badges, ideal answer generation via AI, and interactive mock interview sessions.
7. **Recruiter Intelligence & CRM**: Hunter.io-powered company email domain search, recruiter discovery, email verification, and single-tap outreach draft generation.

---

## 2. Native Product Shapes & Layout Hierarchy

Instead of generic template sections, pages in AiVance are organized around the native shape of career workflows:

```
                          ┌─────────────────────────────────────────┐
                          │             AiVance AppShell            │
                          │   5-Tab Navigation Bar / Adaptive Rail   │
                          └────────────────────┬────────────────────┘
                                               │
    ┌──────────────────┬───────────────────────┼───────────────────────┬──────────────────┐
    ▼                  ▼                       ▼                       ▼                  ▼
┌──────────────┐ ┌──────────────┐      ┌──────────────┐        ┌──────────────┐   ┌──────────────┐
│  Career HQ   │ │    Resume    │      │  Job Search  │        │   Pipeline   │   │  Assistant   │
│ (Dashboard)  │ │    Engine    │      │  & Discovery │        │   (Kanban)   │   │ (Streaming)  │
└──────┬───────┘ └──────┬───────┘      └──────┬───────┘        └──────┬───────┘   └──────┬───────┘
       │                │                     │                       │                  │
       │                ▼                     │                       │                  │
       │     ┌─────────────────────┐          │                       │                  │
       │     │  7-Step Stepper:    │          │                       │                  │
       │     │  Import → Parsing   │          │                       │                  │
       │     │  Preview → ATS      │          │                       │                  │
       │     │  Optimize → Save    │          │                       │                  │
       │     │  Export (PDF)       │          │                       │                  │
       │     └─────────────────────┘          │                       │                  │
       ▼                                      ▼                       ▼                  ▼
┌──────────────┐                       ┌──────────────┐        ┌──────────────┐   ┌──────────────┐
│ Career Score │                       │ Filter Chips │        │ 5 Columns:   │   │ Intent Chips │
│ Gauge 0-100  │                       │ Location     │        │ Saved        │   │ Status Chip  │
│ Quick Stats  │                       │ Workplace    │        │ Applied      │   │ Voice/Attach │
│ Action Cards │                       │ Emp Type     │        │ Interview    │   │ Streaming    │
└──────────────┘                       │ Experience   │        │ Offer        │   │ Chat Bubbles │
                                       └──────────────┘        │ Rejected     │   └──────────────┘
                                                               └──────────────┘
```

---

## 3. Critical Files & Source of Truth

The codebase is organized under `:core` and `:feature` modules. All visual design tokens, component standards, and architectural contracts are anchored in the following authoritative files:

### Design System & Tokens (`:core:designsystem`)
- **Theme Root**: `core/designsystem/src/main/java/com/bangersoul/aivance/core/designsystem/theme/AivanceTheme.kt`
- **Color Palette**: `core/designsystem/src/main/java/com/bangersoul/aivance/core/designsystem/theme/Color.kt`
- **Typography Scale**: `core/designsystem/src/main/java/com/bangersoul/aivance/core/designsystem/theme/Type.kt`
- **Spacing Grid**: `core/designsystem/src/main/java/com/bangersoul/aivance/core/designsystem/theme/Dimens.kt`
- **Shape Radii**: `core/designsystem/src/main/java/com/bangersoul/aivance/core/designsystem/theme/Shapes.kt`
- **Elevation**: `core/designsystem/src/main/java/com/bangersoul/aivance/core/designsystem/theme/Elevation.kt`
- **Motion Spec**: `core/designsystem/src/main/java/com/bangersoul/aivance/core/designsystem/theme/Motion.kt`

### Shared Component Library (`:core:designsystem`)
- `AivanceButton.kt`: Primary, Secondary, Tertiary, Outlined, Destructive buttons with loading states.
- `AivanceCard.kt`: Elevated, Outlined, and Surface container cards.
- `AivanceChip.kt`: Filter, Status, and Keyword chips.
- `ScoreGauge.kt`: Radial circular ATS & Career Score progress gauge with animated sweep.
- `AivanceScreen.kt`: Universal 5-state layout wrapper (`Loading`, `Empty`, `Success`, `Error`, `Partial`).

### Navigation & Shell (`:navigation`)
- `navigation/src/main/java/com/bangersoul/aivance/navigation/AivanceNavGraph.kt`: Top-level router (Auth + Main graphs).
- `navigation/src/main/java/com/bangersoul/aivance/navigation/Destination.kt`: Type-safe route objects (`Dashboard`, `ResumeEngine`, `Jobs`, `Pipeline`, `Assistant`, `Profile`, `InterviewPrep`, `CoverLetter`, `Recruiter`).
- `navigation/src/main/java/com/bangersoul/aivance/navigation/AivanceAppShell.kt`: Bottom navigation bar & adaptive navigation rail.

### Feature Command Surfaces (`:feature:*`)
- **Dashboard**: `feature/dashboard/src/main/java/com/bangersoul/aivance/feature/dashboard/DashboardScreen.kt`
- **Resume Engine**: `feature/resume/src/main/java/com/bangersoul/aivance/feature/resume/ResumeEngineScreen.kt`
- **ATS Matcher**: `feature/ats/src/main/java/com/bangersoul/aivance/feature/ats/AtsScreen.kt`
- **Assistant**: `feature/assistant/src/main/java/com/bangersoul/aivance/feature/assistant/AssistantScreen.kt`
- **Jobs & Discovery**: `feature/jobs/src/main/java/com/bangersoul/aivance/feature/jobs/JobsScreen.kt`
- **Application Tracker**: `feature/tracker/src/main/java/com/bangersoul/aivance/feature/tracker/TrackerScreen.kt`
- **Prep Studio**: `feature/interview/src/main/java/com/bangersoul/aivance/feature/interview/PrepStudioScreen.kt`
- **Profile & Settings**: `feature/profile/src/main/java/com/bangersoul/aivance/feature/profile/ProfileScreen.kt`

---

## 4. Visual Identity & Token Architecture

The visual identity is dark-first, clean, modern, and high-contrast, built on top of Jetpack Compose Material 3 design tokens.

### A. Color System (`Color.kt`)
Semantic roles guarantee theme adaptability across Light, Dark, AMOLED (pure black `#000000`), and Material You Dynamic Wallpaper themes.

| Role | Dark Token | Light Token | Application |
| :--- | :--- | :--- | :--- |
| **Primary** | `#D0BCFF` | `#6750A4` | Main CTAs, Active Tab Icons, Stepper Active Nodes |
| **OnPrimary** | `#381E72` | `#FFFFFF` | Text/Icons inside Primary Buttons |
| **Secondary** | `#CCC2DC` | `#625B71` | Secondary Badges, Sub-header Accents |
| **Accent / Purple** | `#7F67BE` | `#6750A4` | Score Gauges, AI Highlights, Sparkle Icons |
| **Surface / Background** | `#1C1B1F` / `#000000` | `#FFFBFE` | Screen Backgrounds, Card Surface Containers |
| **SurfaceVariant** | `#49454F` | `#E7E0EC` | Inactive Stepper Nodes, Input Bar Backgrounds |
| **Success** | `#4CAF50` | `#2E7D32` | High ATS Score (>75), Saved State, Verified Email |
| **Warning** | `#FF9800` | `#ED6C02` | Medium ATS Score (50-74), Interview Scheduled |
| **Error** | `#F44336` | `#D32F2F` | Low ATS Score (<50), Rejections, Destructive Actions |
| **Info** | `#2196F3` | `#0288D1` | Job Provider Active, Application Applied Stage |

### B. Typography Scale (`Type.kt`)
Built using role-based typography enforcing strict hierarchy:

- **Display Large / Medium** (`Bold` / 32–44sp): Hero Score Gauge values (e.g. `85%`, `92/100`).
- **Headline Small / Medium** (`Bold` / 20–24sp): Screen Identity Titles ("Career HQ", "Resume Engine").
- **Title Medium / Small** (`SemiBold` / 14–16sp): Card Headers, Stepper Step Labels, Section Headers ("Quick Actions", "Suggested Prompts").
- **Body Large / Medium** (`Normal` / 14–16sp): AI Assistant Chat Bubbles, Job Description Content, Resume Bullet Points.
- **Label Large** (`SemiBold` / 14sp): Button Text, Primary Action Labels.
- **Label Small** (`Medium` / 11–12sp): Filter Chips, Status Badges, Timestamp Captions.

### C. Spacing Grid (`Dimens.kt`)
Strict 4dp grid system:
- `extraSmall`: **4dp** — Icon-to-text gap, micro chip internal padding.
- `small`: **8dp** — Card internal item gap, tag spacing, list item padding.
- `medium`: **12dp** — Card internal padding, input field content padding.
- `large`: **16dp** — Standard screen edge margins, section spacing.
- `extraLarge`: **24dp** — Major container margins, hero element spacing.
- `huge`: **32dp** — Section separation on large displays.

### D. Shape Radii (`Shapes.kt`)
- `small`: **8dp** — Tooltips, dropdown menus, micro badges.
- `medium`: **12dp** — Standard cards, list items, dialog containers.
- `large`: **16dp** — Input text fields, bottom sheet containers.
- `extraLarge`: **24dp** — Hero cards, AI assistant welcome surfaces.
- `full`: **100dp** (Pills) — Chips, Action Buttons, Status Badges, Circular Avatars.

### E. Motion & Easing (`Motion.kt`)
- **Fast** (120ms): Micro-interactions, chip toggles, icon color shifts.
- **Standard** (240ms): Page navigation transitions, card expansion, list reordering.
- **Slow** (400ms+): Score gauge radial sweeps, AI typing indicator pulses, celebratory animations.
- **Easing**: `FastOutSlowInEasing` for entrances; `LinearOutSlowInEasing` for exits.

---

## 5. Terminology, Labels & Real Copy Dictionary

All copy in the application is grounded directly in the codebase definitions. No generic or invented strings are permitted.

### Core Terminology
- **Career HQ**: The central dashboard overview.
- **Career Score**: Composite metric (0–100) calculated from profile completion, ATS match averages, active applications, and interview prep.
- **Resume Engine**: 7-step pipeline tool for resume optimization.
- **ATS Matcher**: Applicant Tracking System keyword and formatting scanner.
- **Prep Studio**: AI-powered mock interview and question bank interface.
- **Pipeline**: The 5-stage job application Kanban board.
- **Provider Manager**: Orchestration layer managing AI, Job, and Enrichment service providers.

### Exact Terminology & Options (from Code & Schemas)

#### Job Filter Options (`JobFilterState.kt` / `JobsScreen.kt`)
- **Employment Type**: `Full-time`, `Part-time`, `Internship`, `Apprenticeship`, `Contract`
- **Workplace**: `On-site`, `Remote`, `Hybrid`
- **Experience Level**: `0–2 years (Entry)`, `3–5 years (Mid)`, `6–10 years (Senior)`, `10+ years (Lead/Executive)`
- **Location**: `Country`, `State`, `City`

#### Pipeline Kanban Stages (`JobApplicationEntity.kt`)
- `Saved` → `Applied` → `Interview` → `Offer` → `Rejected`

#### Provider Status Labels (`ProviderStatusUi.kt`)
- `Active` · `Healthy`
- `Degraded` · `Fallback Mode`
- `Invalid Configuration` · `No Provider`
- Capabilities: `TextGeneration`, `StreamingChat`, `JobSearch`, `RecruiterDiscovery`, `EmailVerification`, `SalaryAnalytics`

#### Prep Studio Categories (`InterviewQuestionEntity.kt`)
- `Behavioral`, `Technical`, `System Design`, `HR & Culture Fit`
- Difficulty: `Easy`, `Medium`, `Hard`

#### Standard Action Labels
- `Improve Resume`
- `Find Jobs`
- `Interview Prep`
- `Generate Cover Letter`
- `Apply & Track`
- `Scan with Camera`
- `Export PDF`
- `Configure Provider`
- `Retry`
- `Switch Provider`

---

## 6. Mandatory Screen State Coverage (The 5-State Rule)

Every screen in AiVance must implement and handle all 5 UI states defined in `AivanceScreen.kt`:

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Empty(
        val title: String,
        val description: String,
        val primaryActionText: String? = null,
        val onPrimaryAction: (() -> Unit)? = null
    ) : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(
        val title: String,
        val message: String,
        val canRetry: Boolean = true,
        val onRetry: (() -> Unit)? = null
    ) : UiState<Nothing>
    data class Partial<T>(val data: T, val message: String) : UiState<T>
}
```

1. **Loading**: Display content-specific skeleton shimmer cards (`SkeletonDashboard`, `SkeletonList`). Never use plain centered spinners for content-heavy views.
2. **Empty**: Honest empty states with clear iconography, title, single-sentence explanation, and direct action CTA. No dummy or placeholder data allowed.
3. **Success**: Real, live repository data bound through `StateFlow` to Compose components.
4. **Error**: User-understandable error title, actionable failure detail, and explicit `Retry` / `Switch Provider` actions.
5. **Partial**: Streaming or offline data with progress indicators or warning chips.

---

## 7. Accessibility & Touch Target Rules

1. **Minimum Touch Target**: Every interactive element must be at least **48dp × 48dp**.
2. **Color Contrast**: All text roles meet WCAG AA standards (minimum 4.5:1 ratio for normal text, 3:1 for large display text).
3. **Icon Labels**: Every `Icon` carries a localized `contentDescription` or explicit `null` when paired with adjacent visible text.
4. **Text-Plus-Color**: Information is never conveyed by color alone — always paired with text, icons, or explicit badge labels.

---

## 8. Summary of UI Component Registry (`:core:designsystem`)

- `AivancePrimaryButton`: Full-width or auto-sized filled button with loading indicator support.
- `AivanceSecondaryButton`: Tonal surface button for secondary choices.
- `AivanceOutlinedButton`: Outlined stroke button for auxiliary actions.
- `AivanceTertiaryButton`: Text-only button for inline actions.
- `AivanceCard`: Base container with 12dp rounded corners and subtle border stroke.
- `ScoreGauge`: Circular radial progress ring with animated sweep and centered score text.
- `KeywordChip`: Colored status pill for matched (green), missing (red), or recommended (amber) resume keywords.
- `ProviderStatusChip`: Header chip showing active AI provider name and dot status indicator.
- `AssistantBubble`: Rounded message container distinguishing User messages (primary color) from AI responses (surfaceVariant).
- `KanbanCard`: Compact application summary card with company logo, title, stage badge, and relative date.
