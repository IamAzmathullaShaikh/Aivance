package com.bangersoul.aivance.job.di

import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.job.cache.RoomJobCache
import com.bangersoul.aivance.job.greenhouse.GreenhouseProvider
import com.bangersoul.aivance.job.indeed.IndeedProvider
import com.bangersoul.aivance.job.lever.LeverProvider
import com.bangersoul.aivance.job.linkedin.LinkedInProvider
import com.bangersoul.aivance.job.remoteok.RemoteOKProvider
import com.bangersoul.aivance.job.remotive.RemotiveProvider
import com.bangersoul.aivance.sdk.api.JobProvider
import io.mockk.mockk
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class JobProvidersModuleTest {

    private lateinit var jobCache: JobCache
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var retrofit: Retrofit

    @Before
    fun setUp() {
        jobCache = mockk(relaxed = true)
        okHttpClient = mockk(relaxed = true)
        retrofit = mockk(relaxed = true)
    }

    @Test
    fun `provideRemoteOKProvider returns correct type`() {
        val provider = JobProvidersModule.provideRemoteOKProvider(jobCache, okHttpClient, retrofit)

        assertNotNull(provider)
        assertTrue(provider is RemoteOKProvider)
        assertEquals("remoteok", provider.metadata.id)
    }

    @Test
    fun `provideRemotiveProvider returns correct type`() {
        val provider = JobProvidersModule.provideRemotiveProvider(jobCache, okHttpClient, retrofit)

        assertNotNull(provider)
        assertTrue(provider is RemotiveProvider)
        assertEquals("remotive", provider.metadata.id)
    }

    @Test
    fun `provideLinkedInProvider returns correct type`() {
        val provider = JobProvidersModule.provideLinkedInProvider(jobCache, okHttpClient, retrofit)

        assertNotNull(provider)
        assertTrue(provider is LinkedInProvider)
        assertEquals("linkedin", provider.metadata.id)
    }

    @Test
    fun `provideIndeedProvider returns correct type`() {
        val provider = JobProvidersModule.provideIndeedProvider(jobCache, okHttpClient, retrofit)

        assertNotNull(provider)
        assertTrue(provider is IndeedProvider)
        assertEquals("indeed", provider.metadata.id)
    }

    @Test
    fun `provideGreenhouseProvider returns correct type`() {
        val provider = JobProvidersModule.provideGreenhouseProvider(jobCache, okHttpClient, retrofit)

        assertNotNull(provider)
        assertTrue(provider is GreenhouseProvider)
        assertEquals("greenhouse", provider.metadata.id)
    }

    @Test
    fun `provideLeverProvider returns correct type`() {
        val provider = JobProvidersModule.provideLeverProvider(jobCache, okHttpClient, retrofit)

        assertNotNull(provider)
        assertTrue(provider is LeverProvider)
        assertEquals("lever", provider.metadata.id)
    }

    @Test
    fun `all providers implement JobProvider`() {
        val providers = listOf(
            JobProvidersModule.provideRemoteOKProvider(jobCache, okHttpClient, retrofit),
            JobProvidersModule.provideRemotiveProvider(jobCache, okHttpClient, retrofit),
            JobProvidersModule.provideLinkedInProvider(jobCache, okHttpClient, retrofit),
            JobProvidersModule.provideIndeedProvider(jobCache, okHttpClient, retrofit),
            JobProvidersModule.provideGreenhouseProvider(jobCache, okHttpClient, retrofit),
            JobProvidersModule.provideLeverProvider(jobCache, okHttpClient, retrofit)
        )

        assertEquals(6, providers.size)
        providers.forEach { provider ->
            assertTrue("Provider ${provider.metadata.id} should be a JobProvider", provider is JobProvider)
        }
    }
}
