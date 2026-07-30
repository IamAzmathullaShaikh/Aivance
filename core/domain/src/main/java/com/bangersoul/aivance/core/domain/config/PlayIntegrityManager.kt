package com.bangersoul.aivance.core.domain.config

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result

/**
 * Abstraction over Play Integrity API for device attestation.
 *
 * In production, this delegates to the Play Integrity SDK.
 * In testing, provide a fake implementation.
 */
interface PlayIntegrityManager {

    /**
     * Verify the device integrity and return a verdict.
     */
    suspend fun verifyIntegrity(): CoreResult<IntegrityVerdict>

    /**
     * Check if the app's APK is genuine (not tampered with).
     */
    suspend fun verifyAppIntegrity(): CoreResult<AppIntegrityStatus>

    data class IntegrityVerdict(
        val deviceIntegrity: Boolean,
        val appRecognitionVerdict: Boolean,
        val accountDetails: Boolean = false,
        val timestampMillis: Long = System.currentTimeMillis()
    )

    data class AppIntegrityStatus(
        val isGenuine: Boolean,
        val packageName: String,
        val versionCode: Long,
        val certificateHash: String? = null
    )
}

/**
 * Default Play Integrity error types.
 * Standalone sealed hierarchy (not extending DomainError since DomainError is a data class).
 */
sealed class IntegrityError(message: String, cause: Throwable? = null) {
    class PlayServicesNotAvailable(cause: Throwable? = null) :
        IntegrityError("Google Play Services not available", cause)
    class NetworkError(cause: Throwable? = null) :
        IntegrityError("Network error during integrity check", cause)
    class InternalError(cause: Throwable? = null) :
        IntegrityError("Internal integrity check error", cause)
    class TimeoutError(cause: Throwable? = null) :
        IntegrityError("Integrity check timed out", cause)
}

/**
 * Stub implementation for development/testing.
 * In production, replace with real Play Integrity SDK integration.
 */
class PlayIntegrityManagerStub : PlayIntegrityManager {
    override suspend fun verifyIntegrity(): CoreResult<PlayIntegrityManager.IntegrityVerdict> {
        return Result.Success(
            PlayIntegrityManager.IntegrityVerdict(
                deviceIntegrity = true,
                appRecognitionVerdict = true
            )
        )
    }

    override suspend fun verifyAppIntegrity(): CoreResult<PlayIntegrityManager.AppIntegrityStatus> {
        return Result.Success(
            PlayIntegrityManager.AppIntegrityStatus(
                isGenuine = true,
                packageName = "com.bangersoul.aivance",
                versionCode = 1
            )
        )
    }
}
