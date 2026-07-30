package com.bangersoul.aivance.sdk.exception

import com.bangersoul.aivance.core.common.exception.BaseException

/**
 * Base class for all Aivance SDK exceptions.
 */
sealed class AivanceSdkException(
    errorCode: String,
    message: String,
    cause: Throwable? = null,
    recoverable: Boolean = false,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)

/**
 * Exception thrown when a provider is not in a ready state for operations.
 */
class ProviderNotReadyException(
    val providerId: String,
    val status: String,
    cause: Throwable? = null
) : AivanceSdkException(
    errorCode = "PROVIDER_NOT_READY",
    message = "Provider '$providerId' is not ready (current status: $status)",
    cause = cause,
    recoverable = true
)

/**
 * Exception thrown when a requested capability is not supported by the provider.
 */
class CapabilityNotSupportedException(
    val providerId: String,
    val capability: String,
    cause: Throwable? = null
) : AivanceSdkException(
    errorCode = "CAPABILITY_NOT_SUPPORTED",
    message = "Provider '$providerId' does not support capability: $capability",
    cause = cause,
    recoverable = false
)

/**
 * Exception thrown when a request to a provider fails.
 */
class ProviderRequestException(
    val providerId: String,
    val statusCode: Int = 0,
    message: String,
    cause: Throwable? = null
) : AivanceSdkException(
    errorCode = "PROVIDER_REQUEST_FAILED",
    message = "Provider '$providerId' request failed: $message",
    cause = cause,
    recoverable = true,
    metadata = mapOf("providerId" to providerId, "statusCode" to statusCode)
)

/**
 * Exception thrown when authentication with a provider fails.
 */
class ProviderAuthException(
    val providerId: String,
    message: String = "Authentication failed",
    cause: Throwable? = null
) : AivanceSdkException(
    errorCode = "PROVIDER_AUTH_FAILED",
    message = "Provider '$providerId' auth failed: $message",
    cause = cause,
    recoverable = true,
    metadata = mapOf("providerId" to providerId)
)

/**
 * Exception thrown when a provider rate limit is exceeded.
 */
class ProviderRateLimitException(
    val providerId: String,
    val retryAfterSeconds: Long = 60L,
    message: String = "Rate limit exceeded",
    cause: Throwable? = null
) : AivanceSdkException(
    errorCode = "PROVIDER_RATE_LIMIT",
    message = "Provider '$providerId' rate limit exceeded: $message",
    cause = cause,
    recoverable = true,
    metadata = mapOf("providerId" to providerId, "retryAfterSeconds" to retryAfterSeconds)
)

/**
 * Exception thrown when a network error occurs during provider interaction.
 */
class ProviderNetworkException(
    val providerId: String,
    message: String,
    cause: Throwable? = null
) : AivanceSdkException(
    errorCode = "PROVIDER_NETWORK_ERROR",
    message = "Provider '$providerId' network error: $message",
    cause = cause,
    recoverable = true,
    metadata = mapOf("providerId" to providerId)
)

/**
 * Exception thrown when provider configuration is invalid or missing.
 */
class ProviderConfigurationException(
    val providerId: String,
    message: String,
    cause: Throwable? = null
) : AivanceSdkException(
    errorCode = "PROVIDER_CONFIG_ERROR",
    message = "Provider '$providerId' configuration error: $message",
    cause = cause,
    recoverable = false,
    metadata = mapOf("providerId" to providerId)
)
