package com.bangersoul.aivance.core.domain.config

import com.bangersoul.aivance.core.common.result.CoreResult

/**
 * Privacy and consent management for regulatory compliance (GDPR, CCPA, etc.).
 *
 * Manages user consent for data collection, analytics, and personalization.
 */
interface PrivacyManager {

    /**
     * Current privacy consent preferences.
     */
    suspend fun getConsentPreferences(): ConsentPreferences

    /**
     * Update consent preferences after user opt-in/out.
     */
    suspend fun updateConsent(preferences: ConsentPreferences)

    /**
     * Whether analytics tracking is currently permitted.
     */
    suspend fun isAnalyticsPermitted(): Boolean

    /**
     * Whether crash reporting is currently permitted.
     */
    suspend fun isCrashReportingPermitted(): Boolean

    /**
     * Whether personalization features are permitted.
     */
    suspend fun isPersonalizationPermitted(): Boolean

    /**
     * Check if consent has been collected from the user.
     */
    suspend fun hasConsent(): Boolean

    /**
     * Reset all consent preferences to defaults (opt-out).
     */
    suspend fun resetConsent()

    /**
     * Export all stored user data for GDPR data portability requests.
     */
    suspend fun exportUserData(): CoreResult<UserDataExport>

    /**
     * Delete all stored user data for GDPR right-to-be-forgotten requests.
     */
    suspend fun deleteUserData(): CoreResult<Unit>

    /**
     * Get the privacy policy version the user has accepted.
     */
    suspend fun getAcceptedPrivacyPolicyVersion(): Int

    /**
     * Record that the user accepted a new privacy policy version.
     */
    suspend fun acceptPrivacyPolicyVersion(version: Int)

    data class ConsentPreferences(
        val analyticsEnabled: Boolean = false,
        val crashReportingEnabled: Boolean = true,
        val personalizationEnabled: Boolean = false,
        val thirdPartySharingEnabled: Boolean = false,
        val performanceMonitoringEnabled: Boolean = true,
        val lastUpdatedMillis: Long = System.currentTimeMillis()
    )

    data class UserDataExport(
        val profileData: Map<String, String> = emptyMap(),
        val preferences: Map<String, String> = emptyMap(),
        val savedItems: List<String> = emptyList(),
        val conversationCount: Int = 0,
        val jobApplicationCount: Int = 0,
        val exportTimestampMillis: Long = System.currentTimeMillis()
    )
}

/**
 * GDPR-compliance helper constants.
 */
object PrivacyConstants {
    const val PRIVACY_POLICY_URL = "https://aivance.app/privacy"
    const val TERMS_OF_SERVICE_URL = "https://aivance.app/terms"
    const val SUPPORT_EMAIL = "privacy@aivance.app"
    const val DATA_RETENTION_DAYS = 365
    const val CURRENT_POLICY_VERSION = 1
    const val MIN_AGE_REQUIRED = 16
}
