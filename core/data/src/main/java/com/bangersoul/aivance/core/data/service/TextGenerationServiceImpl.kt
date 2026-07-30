package com.bangersoul.aivance.core.data.service

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.service.TextGenerationService
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderStatus
import com.bangersoul.aivance.sdk.infrastructure.ProviderRegistry
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [TextGenerationService] that delegates to the
 * first available AI provider from the SDK's [ProviderRegistry].
 */
@Singleton
class TextGenerationServiceImpl @Inject constructor(
    private val registry: ProviderRegistry
) : TextGenerationService {

    override suspend fun generateText(prompt: String): CoreResult<String> {
        return try {
            val provider = findAvailableProvider()
            if (provider == null) {
                Timber.w("No AI provider available for text generation")
                return Result.Failure(DomainError("No AI provider configured"))
            }
            provider.generateText(prompt)
        } catch (e: Exception) {
            Timber.e(e, "Text generation failed")
            Result.Failure(DomainError("Text generation failed: ${e.message}", e))
        }
    }

    private fun findAvailableProvider(): AIProvider? {
        val providers = registry.getAllProviders()
        return providers.firstOrNull {
            it is AIProvider && it.status == ProviderStatus.Ready
        } as? AIProvider
    }
}
