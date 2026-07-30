package com.bangersoul.aivance.core.domain.service

import com.bangersoul.aivance.core.common.result.CoreResult

/**
 * Simple text generation service that replaces the deleted AiService.
 *
 * Feature modules use this for AI-powered text generation (cover letters,
 * resume analysis, career roadmaps). The implementation delegates to
 * the configured AI provider from the SDK.
 */
interface TextGenerationService {
    /**
     * Generate text from a prompt.
     * Returns the application's result type for consistent error handling.
     */
    suspend fun generateText(prompt: String): CoreResult<String>
}
