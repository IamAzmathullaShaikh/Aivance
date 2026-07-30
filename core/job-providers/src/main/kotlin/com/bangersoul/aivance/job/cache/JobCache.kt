package com.bangersoul.aivance.job.cache

import com.bangersoul.aivance.core.common.model.JobListing

/**
 * Interface for job results caching.
 */
interface JobCache {
    /**
     * Retrieves cached jobs.
     */
    suspend fun getJobs(): List<JobListing>

    /**
     * Saves jobs to the cache.
     */
    suspend fun saveJobs(jobs: List<JobListing>)

    /**
     * Clears all cached jobs.
     */
    suspend fun clear()
}
