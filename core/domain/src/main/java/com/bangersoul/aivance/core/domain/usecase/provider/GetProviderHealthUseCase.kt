package com.bangersoul.aivance.core.domain.usecase.provider

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.usecase.UseCase
import com.bangersoul.aivance.sdk.core.ProviderStatus
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import javax.inject.Inject

data class ProviderHealth(
    val providerId: String,
    val status: ProviderStatus,
    val isOperational: Boolean
)

/**
 * Checks the health status of a specific provider.
 *
 * Business rules:
 * - Provider must be registered in the ProviderManager.
 * - Returns the provider's current status.
 * - A provider is operational if its status is Ready or Active.
 */
class GetProviderHealthUseCase @Inject constructor(
    private val providerManager: ProviderManager
) : UseCase<String, CoreResult<ProviderHealth>>() {

    override suspend operator fun invoke(providerId: String): CoreResult<ProviderHealth> {
        if (providerId.isBlank()) {
            return Result.Failure(ValidationError("providerId", "Provider ID cannot be blank."))
        }

        return runCatchingCore {
            // Run a live health probe so the reported status reflects the
            // provider's actual reachability + credentials, not a cached lifecycle state.
            providerManager.triggerHealthCheck(providerId)
            val status = providerManager.providerStatuses.value[providerId] ?: ProviderStatus.Uninitialized

            ProviderHealth(
                providerId = providerId,
                status = status,
                isOperational = status == ProviderStatus.Ready ||
                    status == ProviderStatus.Active ||
                    status == ProviderStatus.Healthy
            )
        }
    }
}
