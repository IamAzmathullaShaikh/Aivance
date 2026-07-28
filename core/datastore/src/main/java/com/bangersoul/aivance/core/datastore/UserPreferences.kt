package com.bangersoul.aivance.core.datastore

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val themeConfig: ThemeConfig = ThemeConfig.FOLLOW_SYSTEM
)

enum class ThemeConfig {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK
}
