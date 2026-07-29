package com.bangersoul.aivance.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AivanceNavGraphTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun appStartsAtDashboardDestination() {
        composeTestRule.setContent {
            AivanceTheme {
                AivanceNavGraph()
            }
        }

        // Verify that the Dashboard content is displayed
        composeTestRule.onNodeWithText("Profile Progress").assertIsDisplayed()
        
        // Verify that the greeting/welcome message appears in the TopAppBar
        composeTestRule.onNodeWithText("Welcome back.").assertIsDisplayed()
    }

    @Test
    fun navigationToResumeWorks() {
        composeTestRule.setContent {
            AivanceTheme {
                AivanceNavGraph()
            }
        }

        // Trigger navigation to Resume by clicking the "Open" button in the Resume card
        // This verifies both the DashboardScreen interaction and the NavGraph routing
        composeTestRule.onNodeWithText("Open").performClick()

        // Verify that the Resume screen is displayed
        composeTestRule.onNodeWithText("Resume Optimizer").assertIsDisplayed()
    }
}
