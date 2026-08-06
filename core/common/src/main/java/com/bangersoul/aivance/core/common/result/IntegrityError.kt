package com.bangersoul.aivance.core.common.result

/**
 * Play Integrity error types.
 *
 * Lives beside [CoreError] because sealed-interface implementors must share the
 * declaring package. Implements [CoreError] so failures flow through the shared
 * [CoreResult] channel while remaining a distinct type callers can branch on.
 */
sealed class IntegrityError(
    override val message: String,
    override val cause: Throwable? = null
) : CoreError {
    class PlayServicesNotAvailable(cause: Throwable? = null) :
        IntegrityError("Google Play Services not available", cause)
    class NetworkError(cause: Throwable? = null) :
        IntegrityError("Network error during integrity check", cause)
    class InternalError(cause: Throwable? = null) :
        IntegrityError("Internal integrity check error", cause)
    class TimeoutError(cause: Throwable? = null) :
        IntegrityError("Integrity check timed out", cause)
}
