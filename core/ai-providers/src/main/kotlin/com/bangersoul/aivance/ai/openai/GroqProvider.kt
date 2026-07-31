package com.bangersoul.aivance.ai.openai

import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.ConfigField
import com.bangersoul.aivance.sdk.core.FieldType
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderType

/**
 * Groq AI Provider implementation.
 */
class GroqProvider(
    config: ProviderConfiguration
) : OpenAiBaseProvider(
    metadata = ProviderMetadata(
        id = "groq",
        name = "Groq",
        type = ProviderType.AI,
        version = "1.0.0",
        description = "Ultra-fast inference powered by Groq's LPU technology.",
        author = "Groq",
        configFields = listOf(
            ConfigField(
                key = "apiKey",
                label = "Groq API Key",
                isSensitive = true,
                fieldType = FieldType.PASSWORD
            )
        ),
        supportedModels = listOf("llama3-8b-8192", "llama3-70b-8192", "mixtral-8x7b-32768", "gemma-7b-it")
    ),
    config = config,
    defaultBaseUrl = "https://api.groq.com/openai/v1/"
)
