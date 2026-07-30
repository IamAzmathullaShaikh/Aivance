package com.bangersoul.aivance.core.domain.config

/**
 * Feature flags enabling gradual rollout and A/B testing.
 *
 * Flags are resolved through a priority chain:
 * 1. Remote config (Firebase) — if available
 * 2. Local override (DataStore) — user settings or developer menu
 * 3. Default value — from this file
 */
enum class FeatureFlag(
    val key: String,
    val defaultEnabled: Boolean = false,
    val description: String
) {
    // ── AI Features ─────────────────────────────────────────────
    AI_STREAMING_RESPONSES(
        key = "ai_streaming_responses",
        defaultEnabled = true,
        description = "Enable streaming responses in AI chat"
    ),
    AI_CODE_GENERATION(
        key = "ai_code_generation",
        defaultEnabled = true,
        description = "Enable code block generation in AI responses"
    ),
    AI_VISION_SUPPORT(
        key = "ai_vision_support",
        defaultEnabled = false,
        description = "Enable image analysis via AI providers"
    ),
    AI_MULTI_MODEL(
        key = "ai_multi_model",
        defaultEnabled = true,
        description = "Allow switching between AI models in chat"
    ),

    // ── Job Features ────────────────────────────────────────────
    JOB_REMOTE_FILTER(
        key = "job_remote_filter",
        defaultEnabled = true,
        description = "Enable remote-only job search filter"
    ),
    JOB_SALARY_FILTER(
        key = "job_salary_filter",
        defaultEnabled = true,
        description = "Enable salary range filter in job search"
    ),
    JOB_EXPERIENCE_FILTER(
        key = "job_experience_filter",
        defaultEnabled = false,
        description = "Enable experience level filter in job search"
    ),
    JOB_APIFY_PROVIDERS(
        key = "job_apify_providers",
        defaultEnabled = true,
        description = "Enable Apify-backed job providers (LinkedIn, Indeed)"
    ),
    JOB_ATS_PROVIDERS(
        key = "job_ats_providers",
        defaultEnabled = true,
        description = "Enable ATS-backed job providers (Greenhouse, Lever)"
    ),

    // ── Resume Features ─────────────────────────────────────────
    RESUME_AI_ANALYSIS(
        key = "resume_ai_analysis",
        defaultEnabled = true,
        description = "Enable AI-powered resume analysis"
    ),
    RESUME_ATS_SCORING(
        key = "resume_ats_scoring",
        defaultEnabled = true,
        description = "Enable ATS compatibility scoring"
    ),
    RESUME_EXPORT_PDF(
        key = "resume_export_pdf",
        defaultEnabled = true,
        description = "Enable PDF export for resumes"
    ),

    // ── Interview Features ──────────────────────────────────────
    INTERVIEW_LIVE_SESSION(
        key = "interview_live_session",
        defaultEnabled = true,
        description = "Enable live mock interview sessions"
    ),
    INTERVIEW_VOICE_INPUT(
        key = "interview_voice_input",
        defaultEnabled = false,
        description = "Enable voice input for interview answers"
    ),
    INTERVIEW_FEEDBACK_AI(
        key = "interview_feedback_ai",
        defaultEnabled = true,
        description = "Enable AI-generated interview feedback"
    ),

    // ── Analytics Features ──────────────────────────────────────
    ANALYTICS_SESSION_TRACKING(
        key = "analytics_session_tracking",
        defaultEnabled = true,
        description = "Enable session duration tracking"
    ),
    ANALYTICS_PERFORMANCE_METRICS(
        key = "analytics_performance_metrics",
        defaultEnabled = true,
        description = "Enable performance metric collection"
    ),
    ANALYTICS_CRASH_REPORTING(
        key = "analytics_crash_reporting",
        defaultEnabled = true,
        description = "Enable crash reporting"
    ),

    // ── Experimental Features ───────────────────────────────────
    EXPERIMENTAL_DYNAMIC_THEME(
        key = "experimental_dynamic_theme",
        defaultEnabled = true,
        description = "Enable Material You dynamic color theming"
    ),
    EXPERIMENTAL_ADAPTIVE_LAYOUT(
        key = "experimental_adaptive_layout",
        defaultEnabled = true,
        description = "Enable adaptive layout for tablets and foldables"
    ),
    EXPERIMENTAL_PDF_VIEWER(
        key = "experimental_pdf_viewer",
        defaultEnabled = false,
        description = "Enable built-in PDF viewer (alternative to external apps)"
    ),

    // ── Developer Features ──────────────────────────────────────
    DEV_LOGGING(
        key = "dev_logging",
        defaultEnabled = false,
        description = "Enable verbose debug logging"
    ),
    DEV_PROVIDER_DIAGNOSTICS(
        key = "dev_provider_diagnostics",
        defaultEnabled = false,
        description = "Show provider health diagnostics in UI"
    ),
    DEV_MOCK_PROVIDERS(
        key = "dev_mock_providers",
        defaultEnabled = false,
        description = "Use mock AI providers for testing"
    );

    /**
     * Resolve whether this flag is enabled.
     * Override with [withOverride] for testing.
     */
    fun isEnabled(
        remoteConfig: Map<String, Boolean> = emptyMap(),
        localOverrides: Map<String, Boolean> = emptyMap()
    ): Boolean {
        return localOverrides[key]
            ?: remoteConfig[key]
            ?: defaultEnabled
    }

    companion object {
        private val overrides = mutableMapOf<String, Boolean>()

        /**
         * Set a test override for a specific flag.
         * Call [clearOverrides] after each test.
         */
        fun withOverride(flag: FeatureFlag, enabled: Boolean) {
            overrides[flag.key] = enabled
        }

        fun clearOverrides() {
            overrides.clear()
        }
    }
}
