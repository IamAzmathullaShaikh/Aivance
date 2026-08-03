package com.bangersoul.aivance.core.datastore

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val themeConfig: ThemeConfig = ThemeConfig.FOLLOW_SYSTEM,
    val accentSeed: String = "INDIGO",
    val dynamicColor: Boolean = true,
    val biometricLockEnabled: Boolean = false,
    val geminiApiKey: String? = null,

    /** Notification preferences (Settings Hub toggles). */
    val jobAlertsEnabled: Boolean = true,
    val interviewRemindersEnabled: Boolean = true,
    val followUpRemindersEnabled: Boolean = true,

    /**
     * Persisted identity-provider subject for the v2 auth flow. SplashScreen
     * uses this to auto-login returning users without re-hitting the provider
     * on every cold start.
     */
    val userId: String? = null,
    val userEmail: String? = null,
    val userFirstName: String? = null,

    /** ISO-639 language code selected in Settings (default: English). */
    val language: String = "en"
)

enum class ThemeConfig {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}
