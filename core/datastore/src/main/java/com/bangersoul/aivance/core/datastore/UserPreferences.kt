package com.bangersoul.aivance.core.datastore

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val themeConfig: ThemeConfig = ThemeConfig.FOLLOW_SYSTEM,
    val accentSeed: String = "INDIGO",
    val dynamicColor: Boolean = true,
    val geminiApiKey: String? = null
)

enum class ThemeConfig {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}
