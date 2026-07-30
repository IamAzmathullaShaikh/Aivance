package com.bangersoul.aivance.ai.openai

import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.ProviderMetadata

/**
 * Ollama AI Provider implementation for local models.
 */
class OllamaProvider(
    config: ProviderConfiguration
) : OpenAiBaseProvider(
    metadata = ProviderMetadata(
        id = "ollama",
        name = "Ollama (Local)",
        version = "1.0.0",
        description = "Run open-source large language models locally on your device or server.",
        author = "Ollama"
    ),
    config = config,
    defaultBaseUrl = "http://localhost:11434/v1/"
)
