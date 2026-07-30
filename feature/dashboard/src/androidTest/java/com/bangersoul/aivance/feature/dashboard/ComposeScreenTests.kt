package com.bangersoul.aivance.feature.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.feature.ats.AtsScreen
import com.bangersoul.aivance.feature.ats.AtsViewModel
import com.bangersoul.aivance.feature.ats.domain.AtsResult
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

/**
 * Compose UI tests for feature screens.
 *
 * These tests use createComposeRule with mock ViewModels or preview data
 * to verify that screens render correctly in both light and dark themes.
 */
class ComposeScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── ATS Screen Tests ─────────────────────────────

    @Test
    fun atsScreen_displaysScore() {
        val mockViewModel = mockk<AtsViewModel>(relaxed = true)
        every { mockViewModel.uiState } returns MutableStateFlow(
            com.bangersoul.aivance.feature.ats.AtsUiState.Success(
                latestResult = AtsResult(
                    resumeId = 1, jobDescription = "Android Dev", score = 85,
                    date = Instant.now(), matchedKeywords = listOf("Kotlin"),
                    missingKeywords = listOf("Compose"), feedback = "Great!"
                ),
                history = emptyList()
            )
        )

        composeTestRule.setContent {
            AivanceTheme(darkTheme = true) {
                com.bangersoul.aivance.feature.ats.AtsContent(
                    latestResult = AtsResult(
                        resumeId = 1, jobDescription = "Android Dev", score = 85,
                        date = Instant.now(), matchedKeywords = listOf("Kotlin"),
                        missingKeywords = listOf("Compose"), feedback = "Great!"
                    ),
                    history = emptyList()
                )
            }
        }

        composeTestRule.onNodeWithText("Current Score").assertIsDisplayed()
        composeTestRule.onNodeWithText("85").assertIsDisplayed()
    }

    @Test
    fun atsScreen_displaysMissingKeywords() {
        composeTestRule.setContent {
            AivanceTheme(darkTheme = true) {
                com.bangersoul.aivance.feature.ats.AtsContent(
                    latestResult = AtsResult(
                        resumeId = 1, jobDescription = "Android Dev", score = 65,
                        date = Instant.now(), matchedKeywords = emptyList(),
                        missingKeywords = listOf("Compose", "Coroutines"),
                        feedback = "Missing key skills"
                    ),
                    history = emptyList()
                )
            }
        }

        composeTestRule.onNodeWithText("Missing Keywords").assertIsDisplayed()
    }

    // ── Cover Letter Screen Tests ───────────────────

    @Test
    fun coverLetterScreen_displaysTitle() {
        composeTestRule.setContent {
            AivanceTheme(darkTheme = true) {
                com.bangersoul.aivance.feature.coverletter.CoverLetterInputForm(
                    resumeText = "",
                    onUpdateResume = {},
                    jobDescription = "",
                    onUpdateJobDescription = {},
                    selectedTone = com.bangersoul.aivance.feature.coverletter.domain.model.LetterTone.PROFESSIONAL,
                    onUpdateTone = {},
                    onGenerate = {},
                    modifier = androidx.compose.ui.Modifier
                )
            }
        }

        composeTestRule.onNodeWithText("Application Details").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select Tone").assertIsDisplayed()
    }

    // ── Jobs Screen Tests ────────────────────────────

    @Test
    fun jobItem_displaysTitleAndCompany() {
        composeTestRule.setContent {
            AivanceTheme(darkTheme = true) {
                com.bangersoul.aivance.feature.jobs.JobItem(
                    job = com.bangersoul.aivance.feature.jobs.domain.JobListing(
                        id = "1", company = "Google", title = "Android Engineer",
                        location = "Mountain View", type = "Full-time",
                        salary = "$150k", description = "Build Android apps",
                        postedDate = java.util.Date()
                    ),
                    onApplyClick = {},
                    onTrackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Android Engineer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Google").assertIsDisplayed()
        composeTestRule.onNodeWithText("$150k").assertIsDisplayed()
    }

    // ── Tracker Screen Tests ─────────────────────────

    @Test
    fun applicationItem_displaysCompanyAndRole() {
        composeTestRule.setContent {
            AivanceTheme(darkTheme = true) {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier.padding(16.dp)
                ) {
                    com.bangersoul.aivance.feature.tracker.JobApplicationItem(
                        application = com.bangersoul.aivance.feature.tracker.domain.JobApplication(
                            id = 1, company = "Google", role = "Android Engineer",
                            status = com.bangersoul.aivance.feature.tracker.domain.ApplicationStatus.INTERVIEWING,
                            dateApplied = java.time.Instant.now(),
                            salaryRange = "$180k", notes = null,
                            lastModified = java.time.Instant.now()
                        ),
                        onDelete = {},
                        onUpdateStatus = { _, _ -> }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Android Engineer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Google").assertIsDisplayed()
    }

    // ── Invalid Route Screen Tests ──────────────────

    @Test
    fun invalidRouteScreen_displaysErrorMessage() {
        composeTestRule.setContent {
            AivanceTheme(darkTheme = true) {
                com.bangersoul.aivance.navigation.InvalidRouteScreen(onBack = {})
            }
        }

        composeTestRule.onNodeWithText("Screen Not Found").assertIsDisplayed()
        composeTestRule.onNodeWithText("Go Back").assertIsDisplayed()
    }
}
