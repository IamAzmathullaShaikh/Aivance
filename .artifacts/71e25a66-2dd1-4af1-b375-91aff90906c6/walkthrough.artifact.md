# Walkthrough: AI Resume & CV Analyzer

This document summarizes the implementation and validation of the AI Resume & CV Analyzer feature in the Aivance app.

## Work Done

### 1. Multi-module Integration
The feature is implemented across several modules to ensure clean separation of concerns:
- **`:feature:resume`**: Contains the UI, ViewModel, and feature-specific business logic.
- **`:core:network`**: Provides the `AiService` abstraction and implementations for both Gemini AI and a Mock fallback.
- **`:core:designsystem`**: Houses reusable components like `ScoreGauge` and `KeywordChip` used for visualization.

### 2. UI Components
We developed specialized components to provide clear feedback to the user:
- **`ScoreGauge`**: A circular progress indicator that visually represents the match score between the resume and the job description.
- **`KeywordChip`**: Color-coded chips indicating whether a keyword is matched (Green) or missing (Red/Grey).

### 3. State-driven Architecture
The UI follows a strict state-driven pattern using `ResumeUiState`:
- **Idle**: Initial state where users can input their resume and job description.
- **Analyzing**: A loading state triggered during the AI processing.
- **Success**: Displays the comprehensive analysis results.
- **Error**: Handles and displays failure scenarios gracefully.

### 4. Gemini vs Mock AI Fallback
A robust integration with Google's Gemini Pro API was implemented. To ensure development stability, a `MockAiService` is automatically used as a fallback if the Gemini API key is missing or invalid.

---

## Key Logic Snippets

### Prompt Generation in `ResumeRepositoryImpl`
The AI prompt is carefully structured to ensure a structured JSON response that can be reliably parsed by the application.

```kotlin
override fun analyzeResume(resumeText: String, jobDescription: String): Flow<ResumeAnalysis> = flow {
    val prompt = """
        Analyze the following resume against the job description.

        Resume:
        ${"$"}{resumeText}

        Job Description:
        ${"$"}{jobDescription}

        Provide the analysis in the following JSON format ONLY. Do not include any other text or markdown blocks.
        {
          "matchScore": (0-100 integer),
          "keywords": [
            {"text": "keyword", "isMatched": true/false}
          ],
          "tips": [
            {"category": "category name", "description": "detailed tip"}
          ]
        }
    """.trimIndent()

    val result = aiService.analyzeText(prompt)
    // ... handling response
}
```

### UI State Handling in `ResumeScreen`
Using Jetpack Compose's `AnimatedContent` to provide smooth transitions between input and result states.

```kotlin
@Composable
fun ResumeScreen(viewModel: ResumeViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    AvianceScreen(
        isLoading = uiState is ResumeUiState.Analyzing,
        error = (uiState as? ResumeUiState.Error)?.message,
        onRetry = { /* ... */ }
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) }
        ) { state ->
            when (state) {
                is ResumeUiState.Success -> {
                    AnalysisResultContent(analysis = state.analysis)
                }
                else -> {
                    ResumeInputContent(onAnalyze = { /* ... */ })
                }
            }
        }
    }
}
```

---

## Testing and Verification

### Unit Testing & Mocking
- **Mock AI**: Developed `MockAiService` to simulate AI responses with realistic data patterns, allowing for UI development and testing without hitting API limits.
- **Data Integrity**: Verified that the JSON parsing logic correctly transforms AI responses into domain models.

### Build Verification
- Verified successful compilation of all modules (`:feature:resume`, `:core:network`).
- Ensured Dagger Hilt dependency injection is correctly configured for the `AiService`.

---

## Results

1.  **Instant Feedback**: Users receive a comprehensive analysis of their resume's relevance to a specific job within seconds.
2.  **Clear Visualization**: Missing keywords and optimization tips are presented in an easy-to-digest format, helping users focus on high-impact improvements.
3.  **Adaptive UI**: The results screen scales beautifully across different devices, maintaining legibility and visual appeal.
