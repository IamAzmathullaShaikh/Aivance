package com.bangersoul.aivance.core.common.exception

abstract class BaseException(
    open val errorCode: String,
    override val message: String,
    override val cause: Throwable? = null,
    open val recoverable: Boolean = false,
    open val metadata: Map<String, Any> = emptyMap()
) : Exception(message, cause)

class DomainException(
    errorCode: String = "DOMAIN_ERROR",
    message: String,
    cause: Throwable? = null,
    recoverable: Boolean = false,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)

class ValidationException(
    val field: String? = null,
    errorCode: String = "VALIDATION_ERROR",
    message: String,
    cause: Throwable? = null,
    recoverable: Boolean = true,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)

class ProviderException(
    val providerId: String,
    val statusCode: Int = 0,
    errorCode: String = "PROVIDER_ERROR",
    message: String,
    cause: Throwable? = null,
    recoverable: Boolean = true,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)

class RepositoryException(
    errorCode: String = "REPOSITORY_ERROR",
    message: String,
    cause: Throwable? = null,
    recoverable: Boolean = false,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)

class NetworkException(
    val statusCode: Int = 0,
    errorCode: String = "NETWORK_ERROR",
    message: String,
    cause: Throwable? = null,
    recoverable: Boolean = true,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)

class DatabaseException(
    errorCode: String = "DATABASE_ERROR",
    message: String,
    cause: Throwable? = null,
    recoverable: Boolean = false,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)

class SecurityException(
    errorCode: String = "SECURITY_ERROR",
    message: String,
    cause: Throwable? = null,
    recoverable: Boolean = false,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)

class SerializationException(
    errorCode: String = "SERIALIZATION_ERROR",
    message: String,
    cause: Throwable? = null,
    recoverable: Boolean = false,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)

class AuthenticationException(
    errorCode: String = "AUTH_ERROR",
    message: String = "Authentication failed or token invalid.",
    cause: Throwable? = null,
    recoverable: Boolean = true,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)

class RateLimitException(
    val retryAfterSeconds: Long = 60L,
    errorCode: String = "RATE_LIMIT_EXCEEDED",
    message: String = "Rate limit exceeded. Please try again later.",
    cause: Throwable? = null,
    recoverable: Boolean = true,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)

class QuotaExceededException(
    errorCode: String = "QUOTA_EXCEEDED",
    message: String = "API Quota exceeded.",
    cause: Throwable? = null,
    recoverable: Boolean = false,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)

class UnknownException(
    errorCode: String = "UNKNOWN_ERROR",
    message: String = "An unexpected error occurred.",
    cause: Throwable? = null,
    recoverable: Boolean = false,
    metadata: Map<String, Any> = emptyMap()
) : BaseException(errorCode, message, cause, recoverable, metadata)
