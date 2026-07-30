package com.bangersoul.aivance.job.cache

import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.data.cache.CacheManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryJobCache @Inject constructor(
    private val cacheManager: CacheManager<String, List<JobListing>>
) : JobCache {

    private val CACHE_KEY = "jobs_search_results"

    override suspend fun getJobs(): List<JobListing> {
        return cacheManager.get(CACHE_KEY) ?: emptyList()
    }

    override suspend fun saveJobs(jobs: List<JobListing>) {
        cacheManager.put(CACHE_KEY, jobs)
    }

    override suspend fun clear() {
        cacheManager.evict(CACHE_KEY)
    }
}
