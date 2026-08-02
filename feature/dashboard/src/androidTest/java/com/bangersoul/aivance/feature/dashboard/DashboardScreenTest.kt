package com.bangersoul.aivance.feature.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun sampleState() = DashboardUiState(
        isLoading = false,
        greeting = "Good Morning, Azmath",
        userDesignation = "Software Engineer",
        careerScore = 78,
        atsScore = 92,
        activeApplications = 5,
        savedJobs = 3,
        nextInterview = "Fri 10:00",
        aiRecommendation = "Tailor your resume for senior roles.",
        recentActivity = listOf(
            ActivityItem("1", "Applied to Acme", "Aug 1")
        )
    )

    private fun renderContent(state: DashboardUiState) {
        composeTestRule.setContent {
            AivanceTheme {
                DashboardContent(
                    state = state,
                    onNavigateToResume = {},
                    onNavigateToJobs = {},
                    onNavigateToInterview = {},
                    onNavigateToAssistant = {},
                    onNavigateToTracker = {},
                    onNavigateToProfile = {},
                    onNavigateToAnalytics = {}
                )
            }
        }
    }

    @Test
    fun dashboardDisplaysCareerScore() {
        renderContent(sampleState())

        composeTestRule.onNodeWithText("Career Score").assertIsDisplayed()
        composeTestRule.onNodeWithText("78").assertIsDisplayed()
        composeTestRule.onNodeWithText("Strong profile").assertIsDisplayed()
    }

    @Test
    fun dashboardDisplaysQuickStats() {
        renderContent(sampleState())

        composeTestRule.onNodeWithText("ATS Score").assertIsDisplayed()
        composeTestRule.onNodeWithText("92").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active Apps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved Jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun dashboardDisplaysNextInterviewAndRecommendation() {
        renderContent(sampleState())

        composeTestRule.onNodeWithText("Next Interview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fri 10:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("AI Recommendation").assertIsDisplayed()
    }

    @Test
    fun dashboardDisplaysRecentActivity() {
        renderContent(sampleState())

        composeTestRule.onNodeWithText("Recent Activity").assertIsDisplayed()
        composeTestRule.onNodeWithText("Applied to Acme").assertIsDisplayed()
    }
}
