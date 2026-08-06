package com.bangersoul.aivance.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DestinationTest {

    @Test
    fun `root destinations are exactly the five main tabs`() {
        assertEquals(
            listOf(
                Destination.Dashboard,
                Destination.Intelligence,
                Destination.Discovery,
                Destination.Pipeline,
                Destination.PrepStudio
            ),
            Destination.rootDestinations
        )
    }

    @Test
    fun `every root destination is authenticated`() {
        Destination.rootDestinations.forEach { dest ->
            assertTrue("${dest.label} must be authenticated", dest.isAuthenticatedDestination())
        }
    }

    @Test
    fun `every root destination has an icon`() {
        Destination.rootDestinations.forEach { dest ->
            assertNotNull("${dest.label} must have an icon", dest.icon)
        }
    }

    @Test
    fun `auth graph destinations are not authenticated and have no icon`() {
        Destination.authDestinations.forEach { dest ->
            assertFalse("${dest.label} must not be authenticated", dest.isAuthenticatedDestination())
            assertNull("${dest.label} must not have a nav icon", dest.icon)
        }
    }

    @Test
    fun `parameterized detail destinations resolve as authenticated by type`() {
        assertTrue(Destination.Ats(jobDescription = "JD").isAuthenticatedDestination())
        assertTrue(Destination.CoverLetter(jobId = 1L).isAuthenticatedDestination())
        assertTrue(Destination.JobDetails("job-1").isAuthenticatedDestination())
        assertTrue(Destination.RecruiterDashboard("job-1").isAuthenticatedDestination())
        assertTrue(Destination.CompanyDetail("acme").isAuthenticatedDestination())
        assertTrue(Destination.ResumeDetail(1L).isAuthenticatedDestination())
    }

    @Test
    fun `auth and authenticated destination sets do not overlap`() {
        val overlap = Destination.authDestinations.intersect(Destination.authenticatedDestinations)
        assertTrue("No overlap expected but found $overlap", overlap.isEmpty())
    }

    @Test
    fun `default ATS and cover letter arguments are null`() {
        assertEquals(null, Destination.Ats().jobDescription)
        assertEquals(null, Destination.CoverLetter().jobId)
    }

    @Test
    fun `v2 career destinations carry labels`() {
        assertEquals("Prep Studio", Destination.PrepStudio.label)
        assertEquals("Pipeline", Destination.Pipeline.label)
        assertEquals("Identity Hub", Destination.IdentityHub.label)
        assertEquals("Company", Destination.CompanyDetail("x").label)
        assertEquals("Provider Setup", Destination.ProviderSetup.label)
        assertEquals("Sign In", Destination.Auth.label)
    }
}
