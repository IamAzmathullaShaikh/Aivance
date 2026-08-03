# Implementation Plan — Milestone 2.5: Career Intelligence Orchestrator

This milestone establishes the **Intelligence Orchestrator**, a centralized coordination layer that connects Career State, Workflow, and User Intent into a seamless experience. This is the final architectural milestone before UI implementation.

## User Review Required

> [!IMPORTANT]
> We are introducing the **Intent Engine** and **Command System**, which will fundamentally change how users interact with the app—moving from "button clicks" to "objective-driven" actions.

> [!WARNING]
> The **Workspace Manager** will introduce a persistence layer for temporary UI state (scroll positions, draft text), which requires careful handling of lifecycle events to avoid memory leaks.

## Proposed Changes

### [Core] Orchestrator Architecture
We will implement a set of coordinated engines in `:core:domain` and `:core:data`.

- **[NEW] `IntentEngine`**:
    - Translates natural language or implicit signals into **Workflow Objectives**.
    - Integrates with the AI Assistant to convert chat into structured tasks.
- **[MODIFY] `WorkflowEngine`**:
    - Expand to track **Objective Completion**.
    - Map stage transitions to navigation and notification events.
- **[MODIFY] `RecommendationEngine`**:
    - Centralize all "suggested actions" into a single reactive stream.
- **[NEW] `WorkspaceManager`**:
    - Manages `WorkspaceState` (Drafts, Filters, Scroll positions).
    - Ensures "Resume where you left off" across app restarts.
- **[NEW] `NotificationIntelligence`**:
    - Logic for deriving push/in-app notifications from the `CareerState`.

### [Core] Search & Commands
- **[NEW] `CommandSystem`**:
    - A registry of global commands (e.g., `SEARCH_JOBS`, `OPTIMIZE_RESUME`).
    - Powers the upcoming Command Palette and AI Copilot.
- **[NEW] `UniversalSearchEngine`**:
    - Aggregates results from Resumes, Jobs, Recruiters, and Settings.

### [Feature] Integration
- **[MODIFY] `AssistantViewModel`**: Refactor to act as the primary interface for the `IntentEngine`.
- **[MODIFY] `DashboardViewModel`**: Refactor to consume the expanded `RecommendationEngine`.

## Deliverables

1.  **Career Intelligence Orchestrator Architecture**: Master coordination diagram.
2.  **Intent Engine Specification**: User-to-Objective mapping logic.
3.  **Workflow Engine Expansion**: Progress tracking and completion logic.
4.  **Recommendation Engine Specification**: Contextual suggestion rules.
5.  **Workspace Manager Design**: UI state preservation and recovery strategy.
6.  **Global Command System**: Action registry and intent mapping.
7.  **Universal Search Specification**: Search indexing and retrieval rules.
8.  **Notification Engine Design**: State-driven alert triggers.
9.  **Event Flow Architecture**: Unified event bus for the OS.
10. **Context Propagation Diagram**: How state flows through the engines.
11. **Orchestrator Sequence Diagram**: Life of an intent.
12. **Repository Integration Report**: Ensuring alignment with the 25-module codebase.

## Verification Plan

### Automated Tests
- **Orchestrator Tests**: Verify that an intent (e.g., "I want a job") correctly populates the Workflow Engine with objectives.
- **Search Tests**: Verify search indexing across multiple entity types.

### Manual Verification
- Trigger a command via the Assistant and verify the app moves to the correct workspace with the expected context.
- Verify that closing the app mid-resume-edit restores the exact scroll position and unsaved text.
