package com.bangersoul.aivance.ai.openai

import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.ConfigField
import com.bangersoul.aivance.sdk.core.FieldType
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderType

/**
 * OpenRouter AI Provider implementation.
 */
class OpenRouterProvider(
    config: ProviderConfiguration
) : OpenAiBaseProvider(
    metadata = ProviderMetadata(
        id = "openrouter",
        name = "OpenRouter",
        type = ProviderType.AI,
        version = "1.0.0",
        description = "A unified interface for accessing multiple AI models from different providers.",
        author = "OpenRouter",
        configFields = listOf(
            ConfigField(
                key = "apiKey",
                label = "OpenRouter API Key",
                isSensitive = true,
                fieldType = FieldType.PASSWORD
            )
        ),
        supportedModels = listOf("anthropic/claude-3.5-sonnet", "google/gemini-pro-1.5", "meta-llama/llama-3.1-405b-instruct")
    ),
    config = config,
    defaultBaseUrl = "https://openrouter.ai/api/v1/"
)
