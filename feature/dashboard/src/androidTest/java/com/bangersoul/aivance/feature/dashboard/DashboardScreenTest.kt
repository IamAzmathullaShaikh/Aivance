package com.bangersoul.aivance.feature.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.feature.dashboard.domain.DashboardData
import com.bangersoul.aivance.feature.dashboard.domain.ResumeStatus
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboardDisplaysProfileCompletion() {
        val data = DashboardData(
            profileCompletion = 75,
            resumeStatus = ResumeStatus("test.pdf", LocalDate.now()),
            atsScore = 80,
            activeApplications = 5,
            interviewPrepStatus = "Ready"
        )

        composeTestRule.setContent {
            AivanceTheme {
                DashboardContent(
                    data = data,
                    onNavigateToResume = {},
                    onNavigateToTracker = {},
                    onNavigateToProfile = {},
                    onNavigateToInterview = {}
                )
            }
        }

        composeTestRule.onNodeWithText("75% Complete").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile Progress").assertIsDisplayed()
    }

    @Test
    fun dashboardDisplaysResumeStatus() {
        val data = DashboardData(
            profileCompletion = 75,
            resumeStatus = ResumeStatus("my_resume.pdf", LocalDate.now()),
            atsScore = 80,
            activeApplications = 5,
            interviewPrepStatus = "Ready"
        )

        composeTestRule.setContent {
            AivanceTheme {
                DashboardContent(
                    data = data,
                    onNavigateToResume = {},
                    onNavigateToTracker = {},
                    onNavigateToProfile = {},
                    onNavigateToInterview = {}
                )
            }
        }

        composeTestRule.onNodeWithText("my_resume.pdf").assertIsDisplayed()
        composeTestRule.onNodeWithText("Resume Uploaded").assertIsDisplayed()
    }

    @Test
    fun dashboardDisplaysAtsScore() {
        val data = DashboardData(
            profileCompletion = 75,
            resumeStatus = ResumeStatus("test.pdf", LocalDate.now()),
            atsScore = 92,
            activeApplications = 5,
            interviewPrepStatus = "Ready"
        )

        composeTestRule.setContent {
            AivanceTheme {
                DashboardContent(
                    data = data,
                    onNavigateToResume = {},
                    onNavigateToTracker = {},
                    onNavigateToProfile = {},
                    onNavigateToInterview = {}
                )
            }
        }

        composeTestRule.onNodeWithText("92").assertIsDisplayed()
        composeTestRule.onNodeWithText("ATS Score").assertIsDisplayed()
    }
}
