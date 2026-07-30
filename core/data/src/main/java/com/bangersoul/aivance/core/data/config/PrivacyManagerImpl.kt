package com.bangersoul.aivance.core.data.config

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.config.PrivacyManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [PrivacyManager] backed by encrypted DataStore.
 *
 * Stores consent preferences in DataStore and provides
 * GDPR-compliant data export and deletion capabilities.
 */
@Singleton
class PrivacyManagerImpl @Inject constructor(
    private val preferencesManager: com.bangersoul.aivance.core.datastore.PreferencesManager
) : PrivacyManager {

    private val prefs = preferencesManager

    override suspend fun getConsentPreferences(): PrivacyManager.ConsentPreferences {
        val analytics = prefs.getBoolean("privacy_analytics", false)
        val crash = prefs.getBoolean("privacy_crash_reporting", true)
        val personalization = prefs.getBoolean("privacy_personalization", false)
        val thirdParty = prefs.getBoolean("privacy_third_party", false)
        val performance = prefs.getBoolean("privacy_performance", true)
        val lastUpdated = prefs.getLong("privacy_last_updated", 0L)

        return PrivacyManager.ConsentPreferences(
            analyticsEnabled = analytics,
            crashReportingEnabled = crash,
            personalizationEnabled = personalization,
            thirdPartySharingEnabled = thirdParty,
            performanceMonitoringEnabled = performance,
            lastUpdatedMillis = lastUpdated
        )
    }

    override suspend fun updateConsent(preferences: PrivacyManager.ConsentPreferences) {
        prefs.putBoolean("privacy_analytics", preferences.analyticsEnabled)
        prefs.putBoolean("privacy_crash_reporting", preferences.crashReportingEnabled)
        prefs.putBoolean("privacy_personalization", preferences.personalizationEnabled)
        prefs.putBoolean("privacy_third_party", preferences.thirdPartySharingEnabled)
        prefs.putBoolean("privacy_performance", preferences.performanceMonitoringEnabled)
        prefs.putLong("privacy_last_updated", System.currentTimeMillis())
    }

    override suspend fun isAnalyticsPermitted(): Boolean {
        return prefs.getBoolean("privacy_analytics", false)
    }

    override suspend fun isCrashReportingPermitted(): Boolean {
        return prefs.getBoolean("privacy_crash_reporting", true)
    }

    override suspend fun isPersonalizationPermitted(): Boolean {
        return prefs.getBoolean("privacy_personalization", false)
    }

    override suspend fun hasConsent(): Boolean {
        return prefs.getBoolean("privacy_analytics", false) ||
            prefs.getBoolean("privacy_crash_reporting", true) ||
            prefs.getBoolean("privacy_personalization", false)
    }

    override suspend fun resetConsent() {
        prefs.putBoolean("privacy_analytics", false)
        prefs.putBoolean("privacy_crash_reporting", true)
        prefs.putBoolean("privacy_personalization", false)
        prefs.putBoolean("privacy_third_party", false)
        prefs.putBoolean("privacy_performance", true)
        prefs.putLong("privacy_last_updated", 0L)
    }

    override suspend fun exportUserData(): CoreResult<PrivacyManager.UserDataExport> {
        return try {
            val consent = getConsentPreferences()
            Result.Success(
                PrivacyManager.UserDataExport(
                    profileData = mapOf(
                        "analytics_enabled" to consent.analyticsEnabled.toString(),
                        "crash_reporting_enabled" to consent.crashReportingEnabled.toString(),
                        "personalization_enabled" to consent.personalizationEnabled.toString()
                    ),
                    exportTimestampMillis = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.Failure(
                DomainError(
                    message = "Failed to export user data",
                    cause = e
                )
            )
        }
    }

    override suspend fun deleteUserData(): CoreResult<Unit> {
        return try {
            resetConsent()
            // Clear all stored preferences
            prefs.clearAll()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(
                DomainError(
                    message = "Failed to delete user data",
                    cause = e
                )
            )
        }
    }

    override suspend fun getAcceptedPrivacyPolicyVersion(): Int {
        return prefs.getInt("privacy_policy_version", 0)
    }

    override suspend fun acceptPrivacyPolicyVersion(version: Int) {
        prefs.putInt("privacy_policy_version", version)
        prefs.putLong("privacy_policy_accepted_at", System.currentTimeMillis())
    }
}
