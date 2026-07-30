# Implement OpenAI-Compatible Providers

Implement a reusable base and concrete providers for OpenAI-compatible APIs (OpenAI, Groq, OpenRouter, Ollama) in the `:core:ai-providers` module.

## Proposed Changes

### [core:ai-providers](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/ai-providers)

#### [NEW] [OpenAiApi.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/ai-providers/src/main/kotlin/com/bangersoul/aivance/ai/openai/OpenAiApi.kt)
Define Retrofit interface and data models for OpenAI Chat Completions API.
- `ChatCompletionRequest`
- `ChatCompletionResponse`
- `ChatCompletionChunk`
- `OpenAiMessage`

#### [NEW] [OpenAiBaseProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/ai-providers/src/main/kotlin/com/bangersoul/aivance/ai/openai/OpenAiBaseProvider.kt)
Reusable base class handling:
- Retrofit initialization.
- Chat and text generation implementation.
- Streaming support via OkHttp and manual SSE parsing (to avoid extra dependencies if possible, or use standard OkHttp approach).

#### [NEW] [OpenAIProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/ai-providers/src/main/kotlin/com/bangersoul/aivance/ai/openai/OpenAIProvider.kt)
Concrete implementation for OpenAI.

#### [NEW] [GroqProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/ai-providers/src/main/kotlin/com/bangersoul/aivance/ai/openai/GroqProvider.kt)
Concrete implementation for Groq.

#### [NEW] [OpenRouterProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/ai-providers/src/main/kotlin/com/bangersoul/aivance/ai/openai/OpenRouterProvider.kt)
Concrete implementation for OpenRouter.

#### [NEW] [OllamaProvider.kt](file:///C:/Users/iamsh/AndroidStudioProjects/Aivance/core/ai-providers/src/main/kotlin/com/bangersoul/aivance/ai/openai/OllamaProvider.kt)
Concrete implementation for Ollama.

## Verification Plan

### Automated Tests
- Build the module to ensure no compilation errors.
- (Optional) Create unit tests for `OpenAiBaseProvider` logic if time permits, or rely on manual verification if integrated.

### Manual Verification
- N/A (no UI to test directly in this task).
