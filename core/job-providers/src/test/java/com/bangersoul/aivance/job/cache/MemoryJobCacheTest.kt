package com.bangersoul.aivance.job.cache

import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.data.cache.CacheManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MemoryJobCacheTest {

    private lateinit var cacheManager: CacheManager<String, List<JobListing>>
    private lateinit var cache: MemoryJobCache

    @Before
    fun setUp() {
        cacheManager = mockk(relaxed = true)
        cache = MemoryJobCache(cacheManager)
    }

    @Test
    fun `getJobs delegates to cacheManager`() = runTest {
        val jobs = listOf(
            JobListing(id = "1", title = "Dev", company = "Co", description = "", url = "", sourceProvider = "test"),
            JobListing(id = "2", title = "Sr Dev", company = "Co", description = "", url = "", sourceProvider = "test")
        )
        coEvery { cacheManager.get("jobs_search_results") } returns jobs

        val result = cache.getJobs()

        assertEquals(2, result.size)
        assertEquals("Dev", result[0].title)
        coVerify { cacheManager.get("jobs_search_results") }
    }

    @Test
    fun `getJobs returns empty list when cache miss`() = runTest {
        coEvery { cacheManager.get("jobs_search_results") } returns null

        val result = cache.getJobs()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `saveJobs delegates to cacheManager`() = runTest {
        val jobs = listOf(
            JobListing(id = "1", title = "Dev", company = "Co", description = "", url = "", sourceProvider = "test")
        )

        cache.saveJobs(jobs)

        coVerify { cacheManager.put("jobs_search_results", jobs) }
    }

    @Test
    fun `clear delegates to cacheManager`() = runTest {
        cache.clear()

        coVerify { cacheManager.evict("jobs_search_results") }
    }
}
