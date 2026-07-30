package com.bangersoul.aivance.ai.openai

import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.ProviderMetadata

/**
 * OpenAI AI Provider implementation.
 */
class OpenAIProvider(
    config: ProviderConfiguration
) : OpenAiBaseProvider(
    metadata = ProviderMetadata(
        id = "openai",
        name = "OpenAI",
        version = "1.0.0",
        description = "OpenAI's state-of-the-art language models like GPT-4o.",
        author = "OpenAI"
    ),
    config = config,
    defaultBaseUrl = "https://api.openai.com/v1/"
)
