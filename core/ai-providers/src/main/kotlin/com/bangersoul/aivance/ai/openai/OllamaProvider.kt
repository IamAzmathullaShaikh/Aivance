package com.bangersoul.aivance.ai.openai

import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.ConfigField
import com.bangersoul.aivance.sdk.core.FieldType
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderType

/**
 * Ollama AI Provider implementation for local models.
 */
class OllamaProvider(
    config: ProviderConfiguration
) : OpenAiBaseProvider(
    metadata = ProviderMetadata(
        id = "ollama",
        name = "Ollama (Local)",
        type = ProviderType.AI,
        version = "1.0.0",
        description = "Run open-source large language models locally on your device or server.",
        author = "Ollama",
        configFields = listOf(
            ConfigField(
                key = "baseUrl",
                label = "Ollama Server URL",
                isRequired = true,
                hint = "Usually http://localhost:11434"
            )
        )
    ),
    config = config,
    defaultBaseUrl = "http://localhost:11434/v1/",
    defaultModel = "llama3.1",
    requiresApiKey = false
)
