# ADR 0003: Design System and Theming

## Status
Accepted

## Context
Aivance targets a professional, high-end user base with a focus on productivity and minimal visual clutter. The UI must be highly responsive and support modern Android features like dynamic color.

## Decision
We will implement a custom design system layer on top of Material 3.

### Design Tokens
We use a centralized token system for:
- **Colors**: Dark-first palette with dynamic color support (Android 12+).
- **Typography**: Inter as the primary typeface, optimized for readability.
- **Spacing**: 4dp-based grid for consistency.
- **Shapes**: High corner radii (8dp to 24dp) to convey a modern, premium feel.

### Component Architecture
Components are built to be reusable across all features:
- **`AivanceScreen`**: Standard layout container with built-in loading and error states.
- **`DashboardCard`**: Unified container for all information modules.
- **`ActionButton`**: Consistent interaction style for all CTA elements.

## Consequences
- **Pros**: Consistent look and feel, faster feature development, easy global UI updates.
- **Cons**: Initial overhead in building the design system core.
