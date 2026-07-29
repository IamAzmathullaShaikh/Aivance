package com.bangersoul.aivance.feature.jobs.domain

interface JobSearchRepository {
    suspend fun searchJobs(query: String, filters: List<String>): List<JobListing>
}
