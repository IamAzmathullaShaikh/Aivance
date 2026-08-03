package com.bangersoul.aivance.feature.profile

/** Functional category shown in the provider manager. */
enum class ProviderCategory {
    AI,
    JOB,
    ENRICHMENT
}

data class ProviderInfo(
    val id: String,
    val name: String,
    val category: ProviderCategory = ProviderCategory.AI,
    val description: String = "",
    val isEnabled: Boolean = false,
    val isConnected: Boolean = false,
    val selectedModel: String = "",
    val availableModels: List<String> = emptyList(),
    val apiKeyConfigured: Boolean = false,
    /** Masked preview of the configured credential, e.g. `sk-…abcd` — never the full key. */
    val maskedApiKey: String = "",
    val healthStatus: ProviderHealthStatus = ProviderHealthStatus.UNKNOWN
)

enum class ProviderHealthStatus {
    HEALTHY,
    DEGRADED,
    UNHEALTHY,
    UNKNOWN
}

data class AppSettings(
    val themeMode: String = "system",
    val dynamicColorEnabled: Boolean = true,
    val language: String = "en",
    val analyticsEnabled: Boolean = true,
    val localProcessingOnly: Boolean = false,
    val autoSave: Boolean = true,
    val biometricLockEnabled: Boolean = false,
    val jobAlertsEnabled: Boolean = true,
    val interviewRemindersEnabled: Boolean = true,
    val followUpRemindersEnabled: Boolean = true
)
