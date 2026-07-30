package com.bangersoul.aivance.sdk.telemetry

import com.bangersoul.aivance.core.common.result.*
import com.bangersoul.aivance.sdk.exception.*

/**
 * Maps SDK/Provider specific exceptions to [CoreError].
 */
object ProviderErrorMapper {
    /**
     * Maps a [Throwable] to a [CoreError].
     *
     * @param throwable The exception to map.
     * @param providerId Optional provider ID associated with the error.
     * @return The mapped [CoreError].
     */
    fun map(throwable: Throwable, providerId: String? = null): CoreError {
        return when (throwable) {
            is ProviderNotReadyException -> ProviderError(
                providerId = providerId ?: throwable.providerId,
                message = throwable.message,
                cause = throwable
            )
            is CapabilityNotSupportedException -> ValidationError(
                field = "capability",
                message = throwable.message,
                cause = throwable
            )
            is ProviderRequestException -> ProviderError(
                providerId = throwable.providerId,
                statusCode = throwable.statusCode,
                message = throwable.message,
                cause = throwable
            )
            is ProviderAuthException -> AuthError(
                message = throwable.message,
                cause = throwable
            )
            is ProviderRateLimitException -> RateLimitError(
                retryAfterSeconds = throwable.retryAfterSeconds,
                message = throwable.message,
                cause = throwable
            )
            is ProviderNetworkException -> NetworkError(
                message = throwable.message,
                cause = throwable
            )
            is ProviderConfigurationException -> RepositoryError(
                message = throwable.message,
                cause = throwable
            )
            is AivanceSdkException -> ProviderError(
                providerId = providerId ?: "unknown",
                message = throwable.message,
                cause = throwable
            )
            else -> DomainError(
                message = throwable.message ?: "An unexpected error occurred in the SDK",
                cause = throwable
            )
        }
    }
}
