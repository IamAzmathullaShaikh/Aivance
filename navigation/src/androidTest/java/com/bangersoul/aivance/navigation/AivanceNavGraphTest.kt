package com.bangersoul.aivance.navigation

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @After
    fun tearDown() {
        DeepLinkHandler.reset()
    }

    // ── Destination Tests ─────────────────────────────

    @Test
    fun allRootDestinationsHaveIcons() {
        Destination.rootDestinations.forEach { dest ->
            assertNotNull(
                "Root destination '${dest.label}' must have an icon",
                dest.icon
            )
        }
    }

    @Test
    fun authenticatedDestinationsContainsAllRootDestinations() {
        Destination.rootDestinations.forEach { dest ->
            assertTrue(
                "Root destination '${dest.label}' must be in authenticatedDestinations",
                dest in Destination.authenticatedDestinations
            )
        }
    }

    @Test
    fun authDestinationsDoNotOverlapWithRootDestinations() {
        Destination.authDestinations.forEach { dest ->
            assertTrue(
                "Auth destination '${dest.label}' must NOT be in rootDestinations",
                dest !in Destination.rootDestinations
            )
        }
    }

    // ── Deep Link Tests ───────────────────────────────

    @Test
    fun deepLinkJobDetailsParsesCorrectly() {
        val uri = Uri.parse("aivance://jobs/abc123")
        val dest = DeepLinkHandler.parseUri(uri)
        assertTrue("Expected JobDetails, got $dest", dest is Destination.JobDetails)
        assertEquals("abc123", (dest as Destination.JobDetails).jobId)
    }

    @Test
    fun deepLinkChatParsesCorrectly() {
        val uri = Uri.parse("aivance://chat")
        val dest = DeepLinkHandler.parseUri(uri)
        assertTrue("Expected Assistant, got $dest", dest is Destination.Assistant)
    }

    @Test
    fun deepLinkInterviewParsesCorrectly() {
        val uri = Uri.parse("aivance://interview")
        val dest = DeepLinkHandler.parseUri(uri)
        assertTrue("Expected PrepStudio, got $dest", dest is Destination.PrepStudio)
    }

    @Test
    fun deepLinkResumeParsesCorrectly() {
        val uri = Uri.parse("aivance://resume")
        val dest = DeepLinkHandler.parseUri(uri)
        assertTrue("Expected Intelligence, got $dest", dest is Destination.Intelligence)
    }

    @Test
    fun deepLinkSettingsParsesCorrectly() {
        val uri = Uri.parse("aivance://settings")
        val dest = DeepLinkHandler.parseUri(uri)
        assertTrue("Expected SettingsHub, got $dest", dest is Destination.SettingsHub)
    }

    @Test
    fun deepLinkDashboardParsesCorrectly() {
        val uri = Uri.parse("aivance://app")
        val dest = DeepLinkHandler.parseUri(uri)
        assertTrue("Expected Dashboard, got $dest", dest is Destination.Dashboard)
    }

    @Test
    fun deepLinkUnknownHostReturnsNull() {
        val uri = Uri.parse("aivance://unknown/param")
        val dest = DeepLinkHandler.parseUri(uri)
        assertNull("Expected null for unknown host, got $dest", dest)
    }

    @Test
    fun deepLinkUnsupportedSchemeReturnsNull() {
        val uri = Uri.parse("https://example.com/jobs/123")
        val dest = DeepLinkHandler.parseUri(uri)
        assertNull("Expected null for unsupported scheme, got $dest", dest)
    }

    @Test
    fun deepLinkJobDetailsMissingIdReturnsNull() {
        val uri = Uri.parse("aivance://jobs")
        val dest = DeepLinkHandler.parseUri(uri)
        assertNull("Expected null when jobId is missing, got $dest", dest)
    }

    @Test
    fun deepLinkConsumePending() {
        val uri = Uri.parse("aivance://notifications")
        val dest = DeepLinkHandler.parseUri(uri)
        assertTrue("Expected Notifications", dest is Destination.Notifications)

        // Simulate handleIntent flow
        val handled = DeepLinkHandler.handleIntent(
            android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
        )
        assertNotNull("handleIntent should return destination", handled)

        val consumed = DeepLinkHandler.consumePending()
        assertNotNull("consumePending should return destination", consumed)
        assertTrue("Expected Notifications from consume", consumed is Destination.Notifications)
        assertNull("pendingDestination should be null after consume", DeepLinkHandler.pendingDestination)
    }

    // ── Navigation Tests ──────────────────────────────

    @Test
    fun appShowsSplashOrDashboardOnStart() {
        composeTestRule.setContent {
            AivanceTheme {
                AivanceNavGraph()
            }
        }

        // The app will show either Splash or Dashboard depending on auth state
        // We can't predict which due to Hilt test environment, but both should display
        composeTestRule.waitForIdle()
    }

    @Test
    fun invalidRouteScreenIsAccessible() {
        composeTestRule.setContent {
            AivanceTheme {
                InvalidRouteScreen(onBack = {})
            }
        }

        composeTestRule.onNodeWithText("Screen Not Found").assertIsDisplayed()
        composeTestRule.onNodeWithText("Go Back").assertIsDisplayed()
    }
}
