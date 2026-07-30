package com.bangersoul.aivance.core.common.result

import com.bangersoul.aivance.core.common.exception.BaseException

sealed interface CoreError {
    val message: String
    val cause: Throwable?
}

data class DomainError(override val message: String, override val cause: Throwable? = null) : CoreError
data class RepositoryError(override val message: String, override val cause: Throwable? = null) : CoreError
data class ValidationError(val field: String? = null, override val message: String, override val cause: Throwable? = null) : CoreError
data class NetworkError(val statusCode: Int = 0, override val message: String, override val cause: Throwable? = null) : CoreError
data class DatabaseError(override val message: String, override val cause: Throwable? = null) : CoreError
data class ProviderError(val providerId: String, val statusCode: Int = 0, override val message: String, override val cause: Throwable? = null) : CoreError
data class AuthError(override val message: String = "Authentication failed", override val cause: Throwable? = null) : CoreError
data class RateLimitError(val retryAfterSeconds: Long = 60L, override val message: String = "Rate limit exceeded", override val cause: Throwable? = null) : CoreError

sealed interface Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>
    data class Right<out R>(val value: R) : Either<Nothing, R>

    val isLeft: Boolean get() = this is Left
    val isRight: Boolean get() = this is Right
}

inline fun <L, R, T> Either<L, R>.fold(onLeft: (L) -> T, onRight: (R) -> T): T = when (this) {
    is Either.Left -> onLeft(value)
    is Either.Right -> onRight(value)
}

inline fun <L, R, T> Either<L, R>.map(transform: (R) -> T): Either<L, T> = when (this) {
    is Either.Left -> this
    is Either.Right -> Either.Right(transform(value))
}

inline fun <L, R, T> Either<L, R>.flatMap(transform: (R) -> Either<L, T>): Either<L, T> = when (this) {
    is Either.Left -> this
    is Either.Right -> transform(value)
}

fun <L, R> Either<L, R>.getOrNull(): R? = when (this) {
    is Either.Left -> null
    is Either.Right -> value
}

fun <L, R> Either<L, R>.getOrElse(default: (L) -> R): R = when (this) {
    is Either.Left -> default(value)
    is Either.Right -> value
}

sealed interface Result<out T> {
    data class Success<out T>(val data: T) : Result<T>
    data class Failure(val error: CoreError) : Result<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}

typealias CoreResult<T> = Result<T>

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Failure -> this
}

inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
    is Result.Success -> transform(data)
    is Result.Failure -> this
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onFailure(action: (CoreError) -> Unit): Result<T> {
    if (this is Result.Failure) action(error)
    return this
}

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Failure -> null
}

fun <T> Result<T>.getOrElse(default: (CoreError) -> T): T = when (this) {
    is Result.Success -> data
    is Result.Failure -> default(error)
}

inline fun <T> runCatchingCore(block: () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: BaseException) {
        Result.Failure(
            when (e) {
                is com.bangersoul.aivance.core.common.exception.DomainException -> DomainError(e.message, e)
                is com.bangersoul.aivance.core.common.exception.ValidationException -> ValidationError(e.field, e.message, e)
                is com.bangersoul.aivance.core.common.exception.NetworkException -> NetworkError(e.statusCode, e.message, e)
                is com.bangersoul.aivance.core.common.exception.DatabaseException -> DatabaseError(e.message, e)
                is com.bangersoul.aivance.core.common.exception.ProviderException -> ProviderError(e.providerId, e.statusCode, e.message, e)
                is com.bangersoul.aivance.core.common.exception.AuthenticationException -> AuthError(e.message, e)
                is com.bangersoul.aivance.core.common.exception.RateLimitException -> RateLimitError(e.retryAfterSeconds, e.message, e)
                else -> DomainError(e.message, e)
            }
        )
    } catch (e: Throwable) {
        Result.Failure(DomainError(e.message ?: "Unknown error", e))
    }
}
