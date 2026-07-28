package com.bangersoul.aivance.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey {
    val label: String
    val icon: ImageVector

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
    data object Ats : Destination {
        override val label = "ATS"
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
    data object Jobs : Destination {
        override val label = "Jobs"
        override val icon = Icons.Rounded.WorkOutline
    }

    @Serializable
    data object Profile : Destination {
        override val label = "Profile"
        override val icon = Icons.Rounded.PersonOutline
    }

    @Serializable
    data object Tracker : Destination {
        override val label = "Tracker"
        override val icon = Icons.Rounded.Assessment
    }

    companion object {
        val rootDestinations = listOf(
            Dashboard,
            Resume,
            Tracker,
            Jobs,
            Profile
        )
    }
}
