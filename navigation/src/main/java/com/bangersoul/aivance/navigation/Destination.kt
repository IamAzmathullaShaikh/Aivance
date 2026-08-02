package com.bangersoul.aivance.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * All navigation destinations in the application.
 */
@Serializable
sealed interface Destination : NavKey {
    val label: String

    @Serializable
    data object Splash : Destination {
        override val label = "Splash"
    }

    @Serializable
    data object Welcome : Destination {
        override val label = "Welcome"
    }

    @Serializable
    data object Login : Destination {
        override val label = "Login"
    }

    @Serializable
    data object Onboarding : Destination {
        override val label = "Onboarding"
    }

    @Serializable
    data object Dashboard : Destination {
        override val label = "Dashboard"
    }

    @Serializable
    data object Assistant : Destination {
        override val label = "Assistant"
    }

    @Serializable
    data object Resume : Destination {
        override val label = "Resume"
    }

    @Serializable
    data object Jobs : Destination {
        override val label = "Jobs"
    }

    @Serializable
    data object Profile : Destination {
        override val label = "Profile"
    }

    @Serializable
    data class Ats(val jobDescription: String? = null) : Destination {
        override val label = "ATS Scanner"
    }

    @Serializable
    data class CoverLetter(val jobId: Long? = null) : Destination {
        override val label = "Cover Letter"
    }

    @Serializable
    data class JobDetails(val jobId: String) : Destination {
        override val label = "Job Details"
    }

    @Serializable
    data class RecruiterDashboard(val jobId: String) : Destination {
        override val label = "Recruiter Discovery"
    }

    @Serializable
    data object SavedJobs : Destination {
        override val label = "Saved Jobs"
    }

    @Serializable
    data object Appearance : Destination {
        override val label = "Appearance"
    }

    @Serializable
    data object AiSettings : Destination {
        override val label = "AI Settings"
    }

    @Serializable
    data object ProviderManagement : Destination {
        override val label = "Providers"
    }

    @Serializable
    data object Notifications : Destination {
        override val label = "Notifications"
    }

    @Serializable
    data object PrivacyCenter : Destination {
        override val label = "Privacy & Security"
    }

    // ── v2 Career Operating System destinations ───────────────────────────

    /** Sign-in / create-account screen (replaces the legacy Login flow). */
    @Serializable
    data object Auth : Destination {
        override val label = "Sign In"
    }

    /** Provider configuration flow — reachable from first launch AND Settings. */
    @Serializable
    data object ProviderSetup : Destination {
        override val label = "Provider Setup"
    }

    /** Merged Interview + Learning intelligence engine. */
    @Serializable
    data object PrepStudio : Destination {
        override val label = "Prep Studio"
    }

    /** Kanban application pipeline (replaces the legacy Tracker tab). */
    @Serializable
    data object Pipeline : Destination {
        override val label = "Pipeline"
    }

    @Serializable
    data class CompanyDetail(val companyId: String) : Destination {
        override val label = "Company"
    }

    @Serializable
    data class ResumeDetail(val resumeId: Long) : Destination {
        override val label = "Resume Detail"
    }

    @Serializable
    data object Analytics : Destination {
        override val label = "Analytics"
    }

    @Serializable
    data object SettingsHub : Destination {
        override val label = "Settings"
    }

    /** About screen — contact, licenses, and how the app is made. */
    @Serializable
    data object About : Destination {
        override val label = "About"
    }

    companion object {
        /**
         * Bottom-navigation tabs of the Main graph. Profile is deliberately NOT
         * here — it is reached via the avatar in the Dashboard top bar so the
         * primary navigation stays focused on the career operating loop.
         */
        val rootDestinations = listOf(
            Dashboard, Assistant, Resume, Jobs, Pipeline
        )

        val authenticatedDestinations = setOf(
            Dashboard, Assistant, Resume, Jobs, Profile,
            SavedJobs, AiSettings,
            ProviderManagement, Notifications,
            PrepStudio, Pipeline, Analytics, SettingsHub, About
        )

        val authDestinations = setOf(
            Splash, Welcome, Login, Onboarding, Auth, ProviderSetup
        )
    }
}

/**
 * True when a destination lives inside the authenticated Main graph.
 *
 * Parameterized detail destinations ([CompanyDetail], [ResumeDetail],
 * [JobDetails], [RecruiterDashboard]) can never be compared by value against a
 * [Set], so they are resolved by type here. Declared at top level (like
 * [Destination.icon]) so call sites in the same package resolve it directly.
 */
fun Destination.isAuthenticatedDestination(): Boolean =
    this in Destination.authenticatedDestinations ||
        this is Destination.CompanyDetail ||
        this is Destination.ResumeDetail ||
        this is Destination.JobDetails ||
        this is Destination.RecruiterDashboard ||
        this is Destination.Ats ||
        this is Destination.CoverLetter

val Destination.icon: ImageVector?
    get() = when (this) {
        Destination.Splash -> null
        Destination.Welcome -> null
        Destination.Login -> null
        Destination.Onboarding -> null
        Destination.Dashboard -> Icons.Rounded.GridView
        Destination.Assistant -> Icons.Rounded.AutoAwesome
        Destination.Resume -> Icons.Rounded.Description
        Destination.Jobs -> Icons.Rounded.WorkOutline
        Destination.Profile -> Icons.Rounded.PersonOutline
        is Destination.Ats -> Icons.Rounded.Assessment
        is Destination.CoverLetter -> Icons.Rounded.Assignment
        is Destination.JobDetails -> null
        is Destination.RecruiterDashboard -> Icons.Rounded.PersonSearch
        Destination.SavedJobs -> Icons.Rounded.BookmarkBorder
        Destination.Appearance -> Icons.Rounded.Palette
        Destination.AiSettings -> Icons.Rounded.AutoAwesome
        Destination.ProviderManagement -> Icons.Rounded.Tune
        Destination.Notifications -> Icons.Rounded.Notifications
        Destination.PrivacyCenter -> Icons.Rounded.PrivacyTip
        Destination.Auth -> null
        Destination.ProviderSetup -> null
        Destination.PrepStudio -> Icons.Rounded.School
        Destination.Pipeline -> Icons.Rounded.ViewKanban
        is Destination.CompanyDetail -> null
        is Destination.ResumeDetail -> null
        Destination.Analytics -> Icons.Rounded.BarChart
        Destination.SettingsHub -> Icons.Rounded.Settings
        Destination.About -> Icons.Rounded.Info
    }

/**
 * Localized label resource for each destination. The plain [Destination.label]
 * stays as the stable English identifier (used by tests and logs), while this
 * maps to a translatable string resource for the UI (bottom-nav labels,
 * screen titles).
 */
val Destination.labelRes: Int
    @StringRes get() = when (this) {
        Destination.Splash -> R.string.dest_splash
        Destination.Welcome -> R.string.dest_welcome
        Destination.Login -> R.string.dest_login
        Destination.Onboarding -> R.string.dest_onboarding
        Destination.Dashboard -> R.string.dest_dashboard
        Destination.Assistant -> R.string.dest_assistant
        Destination.Resume -> R.string.dest_resume
        Destination.Jobs -> R.string.dest_jobs
        Destination.Profile -> R.string.dest_profile
        is Destination.Ats -> R.string.dest_ats
        is Destination.CoverLetter -> R.string.dest_cover_letter
        is Destination.JobDetails -> R.string.dest_job_details
        is Destination.RecruiterDashboard -> R.string.dest_recruiter_discovery
        Destination.SavedJobs -> R.string.dest_saved_jobs
        Destination.Appearance -> R.string.dest_appearance
        Destination.AiSettings -> R.string.dest_ai_settings
        Destination.ProviderManagement -> R.string.dest_providers
        Destination.Notifications -> R.string.dest_notifications
        Destination.PrivacyCenter -> R.string.dest_privacy
        Destination.Auth -> R.string.dest_sign_in
        Destination.ProviderSetup -> R.string.dest_provider_setup
        Destination.PrepStudio -> R.string.dest_prep_studio
        Destination.Pipeline -> R.string.dest_pipeline
        is Destination.CompanyDetail -> R.string.dest_company
        is Destination.ResumeDetail -> R.string.dest_resume_detail
        Destination.Analytics -> R.string.dest_analytics
        Destination.SettingsHub -> R.string.dest_settings
        Destination.About -> R.string.dest_about
    }
