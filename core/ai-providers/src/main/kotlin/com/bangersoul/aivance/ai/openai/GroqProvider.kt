package com.bangersoul.aivance.ai.openai

import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.ProviderMetadata

/**
 * Groq AI Provider implementation.
 */
class GroqProvider(
    config: ProviderConfiguration
) : OpenAiBaseProvider(
    metadata = ProviderMetadata(
        id = "groq",
        name = "Groq",
        version = "1.0.0",
        description = "Ultra-fast inference powered by Groq's LPU technology.",
        author = "Groq"
    ),
    config = config,
    defaultBaseUrl = "https://api.groq.com/openai/v1/"
)
