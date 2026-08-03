# AiVance — Intelligence Engines Design

This document details the logic and interfaces for the Workflow, Recommendation, and Context engines.

## 1. Workflow Engine Design

The `WorkflowEngine` is responsible for state transitions and identifies the current "Focus" of the Career OS.

### Logic Matrix
| Condition | Lifecycle Stage | Next Best Action |
| :--- | :--- | :--- |
| `providers.empty()` | `ONBOARDING` | "Setup AI & Job Providers" |
| `resume == null` | `PREPARING` | "Upload or Create your Resume" |
| `atsScore < 70` | `OPTIMIZING` | "Optimize Resume for [Role]" |
| `savedJobs.size < 5` | `EXPLORING` | "Search for [Role] jobs" |
| `activeApps < 1` | `APPLYING` | "Apply to your saved jobs" |
| `interviews.isNotEmpty()`| `INTERVIEWING` | "Start a mock interview session" |

### Implementation Hook
```kotlin
@Singleton
class WorkflowEngine @Inject constructor() {
    fun determineStage(state: CareerState): CareerLifecycleStage {
        // Hierarchical evaluation of blockers
        return when {
            state.intelligence.latestResumeId == null -> CareerLifecycleStage.PREPARING
            state.intelligence.atsScore < 70 -> CareerLifecycleStage.OPTIMIZING
            // ... etc
            else -> CareerLifecycleStage.EXPLORING
        }
    }
}
```

---

## 2. Recommendation Engine Design

The `RecommendationEngine` provides a unified source of truth for actionable career advice.

### Recommendation Sources
1.  **Deterministic Rules**: Based on `CareerState` (e.g., "Resume missing" -> "Upload Resume").
2.  **AI-Generated**: Periodically triggered or on-demand via LLM analysis of the full state.

### Unified Recommendation Interface
```kotlin
data class CareerRecommendation(
    val title: String,
    val description: String,
    val category: RecommendationCategory,
    val priority: Priority,
    val actionDeepLink: String
)

enum class RecommendationCategory { RESUME, JOB, INTERVIEW, PROVIDER, SYSTEM }
```

---

## 3. Context Engine Design

The `ContextEngine` solves the "cold start" problem for AI Assistant interactions by providing a rich context injection.

### System Prompt Injection
Whenever the Assistant is invoked, the `ContextEngine` generates a compact summary of the `CareerState`:

> "User is currently in the **OPTIMIZING** stage. Their target role is **Android Engineer**. Their latest resume has an ATS score of **45%** against a **Senior Android Developer** role at **Google**. They have **2** upcoming interviews."

### Technical Implementation
The `ContextEngine` maps the `CareerState` to a `Map<String, String>` that the `AssistantViewModel` includes as "System Messages" in the LLM chat history.

---

## 4. Validation Report

- **Zero Duplication**: Career Score calculation is moved from `DashboardRepository` to `CareerStateEngine`.
- **Extensibility**: New lifecycle stages or recommendation rules can be added to the engines without touching UI code.
- **Consistency**: Dashboard, Assistant, and Notifications are guaranteed to show identical scores and stage names as they all observe `CareerStateEngine`.
