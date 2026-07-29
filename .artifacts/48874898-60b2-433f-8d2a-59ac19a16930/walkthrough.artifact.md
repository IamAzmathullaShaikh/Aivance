# Walkthrough: Interactive AI Interview Coach

This document summarizes the implementation of the **Interactive AI Interview Coach** in the Aviance app. This feature provides users with an immersive mock interview experience powered by AI, complete with real-time feedback and performance analysis.

## Work Summary

- **AI Infrastructure Upgrade**: Enhanced the `AiService` to support multi-turn chat conversations, enabling more natural interactions between the AI interviewer and the candidate.
- **Multi-step Interview Flow**:
    - **Setup**: Users select their target job role and difficulty level.
    - **Chat**: A real-time, interactive chat interface where the AI asks role-specific questions.
    - **Feedback**: Post-interview analysis providing a summary of performance, strengths, weaknesses, and actionable tips.
- **Premium Chat UI**:
    - Implemented **Chat Bubbles** with distinct styles for the interviewer and candidate.
    - Added a **Typing Indicator** to simulate real-time thought processing by the AI.
    - Integrated smooth transitions using `AnimatedContent` for state changes (Idle -> Loading -> Chatting -> Feedback).
- **Intelligent Feedback**: Developed a structured feedback system that parses AI analysis into actionable insights (Strengths, Weaknesses, Tips).

## Code Highlights

### Multi-turn Conversation Handling

The `InterviewRepositoryImpl` manages the conversation history, ensuring the AI maintains context throughout the interview session.

```kotlin
// InterviewRepositoryImpl.kt
private suspend fun getAiResponse(): InterviewMessage {
    val history = mutableListOf(AiMessage(AiRole.System, systemPrompt))
    history.addAll(messages.value.map { msg ->
        AiMessage(
            role = if (msg.role == MessageRole.User) AiRole.User else AiRole.Assistant,
            content = msg.text
        )
    })

    val response = aiService.chat(history).getOrDefault("I'm sorry, I couldn't process that.")
    val aiMessage = InterviewMessage(role = MessageRole.AI, text = response)
    messages.update { it + aiMessage }
    return aiMessage
}
```

### UI State Transitions

The `InterviewScreen` uses a robust state machine to handle different phases of the interview, providing a polished user experience.

```kotlin
// InterviewScreen.kt
AnimatedContent(
    targetState = uiState,
    transitionSpec = { fadeIn().togetherWith(fadeOut()) },
    label = "InterviewContent"
) { state ->
    when (state) {
        is InterviewUiState.Idle -> SetupView(onStart = viewModel::startInterview)
        is InterviewUiState.Loading -> LoadingView()
        is InterviewUiState.Chatting -> ChatView(...)
        is InterviewUiState.GeneratingFeedback -> FeedbackLoadingView()
        is InterviewUiState.Feedback -> FeedbackView(feedback = state.feedback, ...)
    }
}
```

## Results

- **Immersive Experience**: Users can practice interviews for any role, from "Android Developer" to "Product Manager", with varying difficulty levels.
- **Automated Professional Feedback**: After the session, users receive a detailed breakdown of their performance, helping them prepare more effectively for real-world interviews.
- **Visual Polish**: The chat interface feels modern and "expressive," adhering to the project's design guidelines with rounded bubbles and animated indicators.

## Verification

- **Build Status**: Verified via `./gradlew :app:assembleDebug`.
- **Quality**: Code reviewed for idiomatic Kotlin, proper WindowInsets handling, and Material 3 Expressive guidelines.
- **Previews**: All major UI states (Setup, Chat, Feedback) include `@Preview` functions for rapid iteration and visual validation.
