package com.bangersoul.aivance.job.apify

import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.job.indeed.IndeedProvider
import com.bangersoul.aivance.job.linkedin.LinkedInProvider
import com.bangersoul.aivance.sdk.core.ProviderStatus
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class ApifyJobProviderTest {

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
    fun `LinkedInProvider has correct metadata`() {
        val provider = LinkedInProvider("test-key", jobCache, okHttpClient, retrofit)
        
        assertEquals("linkedin", provider.metadata.id)
        assertEquals("LinkedIn", provider.metadata.name)
    }

    @Test
    fun `IndeedProvider has correct metadata`() {
        val provider = IndeedProvider("test-key", jobCache, okHttpClient, retrofit)
        
        assertEquals("indeed", provider.metadata.id)
        assertEquals("Indeed", provider.metadata.name)
    }

    @Test
    fun `Apify provider lifecycle transitions correctly`() = runTest {
        val provider = LinkedInProvider("test-key", jobCache, okHttpClient, retrofit)
        
        assertEquals(ProviderStatus.Uninitialized, provider.status)
        
        provider.onInitialize()
        assertEquals(ProviderStatus.Ready, provider.status)
        
        provider.onStart()
        assertEquals(ProviderStatus.Active, provider.status)
        
        provider.onStop()
        assertEquals(ProviderStatus.Ready, provider.status)
        
        provider.onDispose()
        assertEquals(ProviderStatus.Disposed, provider.status)
    }
}
