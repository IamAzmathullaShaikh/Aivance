package com.bangersoul.aivance.ai.openai

import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.ProviderMetadata

/**
 * OpenRouter AI Provider implementation.
 */
class OpenRouterProvider(
    config: ProviderConfiguration
) : OpenAiBaseProvider(
    metadata = ProviderMetadata(
        id = "openrouter",
        name = "OpenRouter",
        version = "1.0.0",
        description = "A unified interface for accessing multiple AI models from different providers.",
        author = "OpenRouter"
    ),
    config = config,
    defaultBaseUrl = "https://openrouter.ai/api/v1/"
)
