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
        supportedModels = listOf(
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant",
            "openai/gpt-oss-120b",
            "qwen/qwen3.6-27b",
            "allam-2-7b"
        )
    ),
    config = config,
    defaultBaseUrl = "https://api.groq.com/openai/v1/",
    defaultModel = "llama-3.3-70b-versatile"
)
