# Implementation Plan — Milestone 3: AiVance Design System Foundation

This plan outlines the creation of the official **AiVance Design System**. We will build a unified set of tokens and a comprehensive component library in `:core:designsystem` that will power all future UI implementation.

## User Review Required

> [!IMPORTANT]
> This milestone strictly focuses on **Visual Identity, Tokens, and Components**. We will not implement business features, but we will "Themify" existing placeholder screens to match the new system.

> [!CAUTION]
> Adopting the new design system may require replacing existing Material 3 defaults with `Aivance*` custom components to ensure a premium, branded look.

## Proposed Changes

### [Core] Design Tokens Expansion
We will expand the existing `:core:designsystem` to include full token sets.

- **[MODIFY] `Color.kt`**: Finalize Zinc palette and semantic branding (Indigo/Violet focus).
- **[MODIFY] `Type.kt`**: Implement a consistent typography scale using Inter or similar sans-serif.
- **[NEW] `Dimens.kt`**: Define 8dp grid tokens (`spacing_medium`, `spacing_large`, etc.).
- **[NEW] `Radius.kt`**: Standardize corner radii (Small: 8dp, Medium: 16dp, Large: 24dp).
- **[MODIFY] `Motion.kt`**: Define standard durations and easings for all transitions.

### [Core] Component Library implementation
We will build the following core components in `core:designsystem:components`.

- **Buttons**: `AivanceButton` (Primary/Secondary), `AivanceIconButton`, `AivanceLoadingButton`.
- **Cards**: `AivanceCard`, `AivanceMetricCard`, `AivanceRecommendationCard`.
- **Inputs**: `AivanceTextField`, `AivanceSearchBar`, `AivanceSelect`.
- **States**: `AivanceEmptyState`, `AivanceErrorState`, `AivanceSkeleton`.
- **Navigation**: `AivanceTopBar`, `AivanceBottomNav` (Wrappers for M3 Adaptive).

### [Core] Layout & Grid System
- **[NEW] `ResponsiveGrid`**: A Compose utility for automatic 1/2/3 column switching based on screen width.

## Deliverables

1.  **AiVance Design System Specification**: Master document.
2.  **Design Token Specification**: Color, Type, Spacing, Radius tables.
3.  **Theme Specification**: Implementation details for Dark/Light/AMOLED.
4.  **Component Library Catalog**: List of all reusable components and their variants.
5.  **Motion Guidelines**: Transition rules and timings.
6.  **Responsive Layout Guide**: Breakpoint mapping and adaptive strategies.
7.  **Accessibility Guide**: Contrast and TalkBack standards.
8.  **UI Style Guide**: Visual rules for margins, hierarchy, and imagery.
9.  **Figma Component Specification**: Mapping for designers.
10. **Design QA Checklist**: Verification steps for new screens.

## Verification Plan

### Automated Tests
- **Preview Tests**: Use `@Preview` for every component in Light and Dark mode.
- **Contrast Check**: Automated script (if available) or manual check against WCAG 2.1.

### Manual Verification
- Verify that every component adapts its layout when switching from Phone to Tablet in the Android Studio Layout Inspector.
- Verify that "Large Font Mode" does not break card layouts or clip text.
