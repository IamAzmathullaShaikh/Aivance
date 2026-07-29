package com.bangersoul.aivance.feature.jobs.data

import com.bangersoul.aivance.feature.jobs.domain.JobListing
import com.bangersoul.aivance.feature.jobs.domain.JobSearchRepository
import java.util.Date
import javax.inject.Inject

class JobSearchRepositoryImpl @Inject constructor() : JobSearchRepository {
    private val mockJobs = listOf(
        JobListing(
            id = "1",
            company = "Google",
            title = "Android Engineer",
            location = "Mountain View, CA",
            type = "Full-time",
            salary = "$150k - $220k",
            description = "Lead the development of next-gen Android experiences.",
            postedDate = Date()
        ),
        JobListing(
            id = "2",
            company = "RemoteTech",
            title = "Senior Kotlin Developer",
            location = "Remote",
            type = "Remote",
            salary = "$130k - $180k",
            description = "Build high-performance distributed systems using Kotlin Coroutines.",
            postedDate = Date()
        ),
        JobListing(
            id = "3",
            company = "StartupX",
            title = "Mobile Lead",
            location = "New York, NY",
            type = "Full-time",
            salary = "$160k - $200k",
            description = "Define the mobile strategy for a fast-growing Fintech startup.",
            postedDate = Date()
        ),
        JobListing(
            id = "4",
            company = "GlobalConnect",
            title = "Software Architect",
            location = "Remote",
            type = "Remote",
            salary = "$180k - $250k",
            description = "Design scalable cloud-native architectures.",
            postedDate = Date()
        ),
        JobListing(
            id = "5",
            company = "DesignHub",
            title = "Product Designer",
            location = "San Francisco, CA",
            type = "Full-time",
            salary = "$120k - $170k",
            description = "Create beautiful and intuitive user interfaces.",
            postedDate = Date()
        )
    )

    override suspend fun searchJobs(query: String, filters: List<String>): List<JobListing> {
        return mockJobs.filter { job ->
            val matchesQuery = job.title.contains(query, ignoreCase = true) ||
                    job.company.contains(query, ignoreCase = true) ||
                    job.description.contains(query, ignoreCase = true)
            
            val matchesFilters = if (filters.isEmpty()) {
                true
            } else {
                filters.any { filter -> job.type.contains(filter, ignoreCase = true) }
            }

            matchesQuery && matchesFilters
        }
    }
}
