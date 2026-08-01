package com.bangersoul.aivance.ai.openai

import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.ConfigField
import com.bangersoul.aivance.sdk.core.FieldType
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderType

/**
 * OpenAI AI Provider implementation.
 */
class OpenAIProvider(
    config: ProviderConfiguration
) : OpenAiBaseProvider(
    metadata = ProviderMetadata(
        id = "openai",
        name = "OpenAI",
        type = ProviderType.AI,
        version = "1.0.0",
        description = "OpenAI's state-of-the-art language models like GPT-4o.",
        author = "OpenAI",
        configFields = listOf(
            ConfigField(
                key = "apiKey",
                label = "OpenAI API Key",
                isSensitive = true,
                fieldType = FieldType.PASSWORD
            )
        ),
        supportedModels = listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo")
    ),
    config = config,
    defaultBaseUrl = "https://api.openai.com/v1/",
    defaultModel = "gpt-4o-mini"
)
