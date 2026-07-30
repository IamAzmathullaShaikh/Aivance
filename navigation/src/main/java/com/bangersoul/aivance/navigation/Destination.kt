package com.bangersoul.aivance.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * All navigation destinations in the application.
 * Uses Jetpack Navigation 3 type-safe routing with kotlinx.serialization.
 *
 * Deep-linking structure:
 *   aivance://jobs/{jobId}
 *   aivance://chat/{conversationId}
 *   aivance://interview/{sessionId}
 *   aivance://roadmap/{roadmapId}
 *   aivance://resume/{resumeId}
 */
@Serializable
sealed interface Destination : NavKey {
    val label: String
    val icon: ImageVector?

    // ──────────────────────────────────────────────
    // Auth Flow — no bottom-nav, full-screen
    // ──────────────────────────────────────────────

    @Serializable
    data object Splash : Destination {
        override val label = "Splash"
        override val icon: ImageVector? = null
    }

    @Serializable
    data object Welcome : Destination {
        override val label = "Welcome"
        override val icon: ImageVector? = null
    }

    @Serializable
    data object Login : Destination {
        override val label = "Login"
        override val icon: ImageVector? = null
    }

    @Serializable
    data object Onboarding : Destination {
        override val label = "Onboarding"
        override val icon: ImageVector? = null
    }

    // ──────────────────────────────────────────────
    // Bottom-Nav Root Destinations
    // ──────────────────────────────────────────────

    @Serializable
    data object Dashboard : Destination {
        override val label = "Dashboard"
        override val icon = Icons.Rounded.GridView
    }

    @Serializable
    data object Resume : Destination {
        override val label = "Resume"
        override val icon = Icons.Rounded.Description
    }

    @Serializable
    data object Tracker : Destination {
        override val label = "Tracker"
        override val icon = Icons.Rounded.Assessment
    }

    @Serializable
    data object Jobs : Destination {
        override val label = "Jobs"
        override val icon = Icons.Rounded.WorkOutline
    }

    @Serializable
    data object Profile : Destination {
        override val label = "Profile"
        override val icon = Icons.Rounded.PersonOutline
    }

    // ──────────────────────────────────────────────
    // Feature Screens (pushed onto backstack)
    // ──────────────────────────────────────────────

    @Serializable
    data object Ats : Destination {
        override val label = "ATS Scanner"
        override val icon = Icons.Rounded.Assessment
    }

    @Serializable
    data object CoverLetter : Destination {
        override val label = "Cover Letter"
        override val icon = Icons.Rounded.Assignment
    }

    @Serializable
    data object Interview : Destination {
        override val label = "Interview"
        override val icon = Icons.Rounded.QuestionAnswer
    }

    @Serializable
    data object AiChat : Destination {
        override val label = "AI Chat"
        override val icon = Icons.Rounded.Chat
    }

    @Serializable
    data class JobDetails(val jobId: String) : Destination {
        override val label = "Job Details"
        override val icon: ImageVector? = null
    }

    @Serializable
    data object SavedJobs : Destination {
        override val label = "Saved Jobs"
        override val icon = Icons.Rounded.BookmarkBorder
    }

    @Serializable
    data object CareerRoadmap : Destination {
        override val label = "Career Roadmap"
        override val icon = Icons.Rounded.Route
    }

    @Serializable
    data object LearningHub : Destination {
        override val label = "Learning Hub"
        override val icon = Icons.Rounded.School
    }

    @Serializable
    data object Settings : Destination {
        override val label = "Settings"
        override val icon = Icons.Rounded.Settings
    }

    @Serializable
    data object AiSettings : Destination {
        override val label = "AI Settings"
        override val icon = Icons.Rounded.AutoAwesome
    }

    @Serializable
    data object ProviderManagement : Destination {
        override val label = "Providers"
        override val icon = Icons.Rounded.Tune
    }

    @Serializable
    data object Notifications : Destination {
        override val label = "Notifications"
        override val icon = Icons.Rounded.Notifications
    }

    @Serializable
    data object AnalyticsDashboard : Destination {
        override val label = "Analytics"
        override val icon = Icons.Rounded.BarChart
    }

    companion object {
        /** Destinations shown in bottom navigation / navigation rail. */
        val rootDestinations = listOf(
            Dashboard,
            Resume,
            Tracker,
            Jobs,
            Profile
        )

        /** Destinations that require authentication (api key configured). */
        val authenticatedDestinations = setOf(
            Dashboard, Resume, Tracker, Jobs, Profile,
            Ats, CoverLetter, Interview, AiChat, SavedJobs,
            CareerRoadmap, LearningHub, Settings, AiSettings,
            ProviderManagement, Notifications, AnalyticsDashboard
        )

        /** Auth-flow destinations (no bottom nav). */
        val authDestinations = setOf(
            Splash, Welcome, Login, Onboarding
        )
    }
}
