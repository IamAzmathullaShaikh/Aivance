# Implementation Plan — Milestone 2: Navigation Architecture & Workflow Navigation Engine

This plan details the conversion of the Career OS Information Architecture into a **workflow-driven navigation system**. Navigation will adapt to the `CareerState` and guide the user through their journey.

## User Review Required

> [!IMPORTANT]
> This milestone involves deep refactoring of the `:navigation` module. We will transition from a flat destination list to a **nested workspace architecture**.

> [!WARNING]
> We will implement an **AI Assistant Overlay** that persists across all screens. This requires a structural change to the `NavigationSuiteScaffold` in the App Shell.

## Proposed Changes

### [Navigation] Workspace Architecture
We will group destinations into five distinct **Workspaces** (graphs), each maintaining its own backstack and state.

- **Dashboard (Career HQ)**
- **Intelligence Workspace** (Resume Editor, ATS Scanner, Version History)
- **Discovery Workspace** (Job Search, Company Intel, Recruiter CRM)
- **Pipeline Workspace** (Kanban Tracker, Application Timeline)
- **Prep Studio Workspace** (Interview Coach, Feedback, Question Bank)

### [Navigation] AI Overlay & Global Navigation
- **[MODIFY] `AivanceAppShell`**: Add a global `AssistantOverlay` (Modal Bottom Sheet or Floating Scrim) that can be triggered from any screen without losing the current workspace context.
- **[MODIFY] `AivanceNavGraph`**: Implement logic to handle global destinations (Profile, Settings, Notifications) as "interruptions" that return to the active workflow.

### [Intelligence] Workflow Navigation Engine
- **[NEW] `NavigationWorkflowEngine`**: A domain component that analyzes the `CareerState` and emits **Navigation Recommendations**.
    - If `state.lifecycleStage == OPTIMIZING`, it suggests "View ATS Analysis".
    - If `state.lifecycleStage == INTERVIEWING`, it suggests "Prepare for [Company]".

### [Code] Navigation Events (UDF)
- **[MODIFY] ViewModels**: All navigation will be emitted via a shared `NavigationEffect` stream to the `MainActivity` / `NavGraph`.

## Deliverables

1.  **Navigation Graph Specification**: Visual tree of nested workspaces.
2.  **Adaptive Navigation Spec**: Mapping layouts for Phone/Tablet/Foldable.
3.  **Back Stack Rules**: Definition of state restoration and predictive back.
4.  **Deep Link Spec**: Mapping URIs to workspace entry points.
5.  **AI Overlay Design**: Interaction model for the global assistant.
6.  **Navigation State Diagram**: Workflow-driven transition map.
7.  **Workflow Navigation Specification**: Logic for "Next Best Action" routing.

## Verification Plan

### Automated Tests
- **Navigation Tests**: Verify that switching tabs preserves the backstack of each workspace.
- **Deep Link Tests**: Verify that URIs correctly restore the intended context (e.g., specific Job in Discovery).

### Manual Verification
- Verify the AI Overlay opens and closes without resetting the underlying screen state.
- Verify the `NavigationSuiteScaffold` correctly adapts its UI (Bottom Bar vs. Rail) across screen sizes.
