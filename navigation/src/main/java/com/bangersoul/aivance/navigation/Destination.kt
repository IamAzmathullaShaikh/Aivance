package com.bangersoul.aivance.navigation

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
    data object Tracker : Destination {
        override val label = "Tracker"
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
    data object Ats : Destination {
        override val label = "ATS Scanner"
    }

    @Serializable
    data object CoverLetter : Destination {
        override val label = "Cover Letter"
    }

    @Serializable
    data object Interview : Destination {
        override val label = "Interview"
    }

    @Serializable
    data object AiChat : Destination {
        override val label = "AI Chat"
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
    data object CareerRoadmap : Destination {
        override val label = "Career Roadmap"
    }

    @Serializable
    data object LearningHub : Destination {
        override val label = "Learning Hub"
    }

    @Serializable
    data object Settings : Destination {
        override val label = "Settings"
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
    data object AnalyticsDashboard : Destination {
        override val label = "Analytics"
    }

    @Serializable
    data object PrivacyCenter : Destination {
        override val label = "Privacy & Security"
    }

    companion object {
        val rootDestinations = listOf(
            Dashboard, Assistant, Resume, Tracker, Jobs, Profile
        )

        val authenticatedDestinations = setOf(
            Dashboard, Assistant, Resume, Tracker, Jobs, Profile,
            Ats, CoverLetter, Interview, AiChat, SavedJobs,
            CareerRoadmap, LearningHub, Settings, AiSettings,
            ProviderManagement, Notifications, AnalyticsDashboard
        )

        val authDestinations = setOf(
            Splash, Welcome, Login, Onboarding
        )
    }
}

val Destination.icon: ImageVector?
    get() = when (this) {
        Destination.Splash -> null
        Destination.Welcome -> null
        Destination.Login -> null
        Destination.Onboarding -> null
        Destination.Dashboard -> Icons.Rounded.GridView
        Destination.Assistant -> Icons.Rounded.AutoAwesome
        Destination.Resume -> Icons.Rounded.Description
        Destination.Tracker -> Icons.Rounded.Assessment
        Destination.Jobs -> Icons.Rounded.WorkOutline
        Destination.Profile -> Icons.Rounded.PersonOutline
        Destination.Ats -> Icons.Rounded.Assessment
        Destination.CoverLetter -> Icons.Rounded.Assignment
        Destination.Interview -> Icons.Rounded.QuestionAnswer
        Destination.AiChat -> Icons.Rounded.Chat
        is Destination.JobDetails -> null
        is Destination.RecruiterDashboard -> Icons.Rounded.PersonSearch
        Destination.SavedJobs -> Icons.Rounded.BookmarkBorder
        Destination.CareerRoadmap -> Icons.Rounded.Route
        Destination.LearningHub -> Icons.Rounded.School
        Destination.Settings -> Icons.Rounded.Settings
        Destination.Appearance -> Icons.Rounded.Palette
        Destination.AiSettings -> Icons.Rounded.AutoAwesome
        Destination.ProviderManagement -> Icons.Rounded.Tune
        Destination.Notifications -> Icons.Rounded.Notifications
        Destination.AnalyticsDashboard -> Icons.Rounded.BarChart
        Destination.PrivacyCenter -> Icons.Rounded.PrivacyTip
    }
