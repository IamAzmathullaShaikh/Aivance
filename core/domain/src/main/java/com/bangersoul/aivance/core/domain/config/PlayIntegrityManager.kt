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


