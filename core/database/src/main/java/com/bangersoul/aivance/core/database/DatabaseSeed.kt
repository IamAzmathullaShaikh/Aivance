package com.bangersoul.aivance.core.database

import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.dao.ProfileDao
import com.bangersoul.aivance.core.database.model.CompanyEntity
import com.bangersoul.aivance.core.database.model.JobEntity
import com.bangersoul.aivance.core.database.model.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeed @Inject constructor(
    private val profileDao: ProfileDao,
    private val companyDao: CompanyDao,
    private val jobDao: JobDao
) {
    suspend fun seed() = withContext(Dispatchers.IO) {
        seedProfile()
        val companyIds = seedCompanies()
        seedJobs(companyIds)
    }

    private suspend fun seedProfile() {
        val profile = UserProfileEntity(
            id = "user_123",
            name = "Jane Doe",
            email = "jane.doe@example.com",
            currentRole = "Senior Software Engineer",
            skills = listOf("Kotlin", "Android", "Jetpack Compose", "Room", "Dagger Hilt"),
            targetRole = "Mobile Architect",
            bio = "Passionate about building scalable mobile experiences and exploring the latest Android technologies.",
            profilePictureUrl = "https://example.com/profiles/jane_doe.jpg"
        )
        profileDao.insertProfile(profile)
    }

    private suspend fun seedCompanies(): List<Long> {
        val companies = listOf(
            CompanyEntity(name = "Google", domain = "google.com", logoUrl = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png", website = "https://google.com", industry = "Technology", headquarters = "Mountain View, CA"),
            CompanyEntity(name = "Meta", domain = "meta.com", logoUrl = "https://upload.wikimedia.org/wikipedia/commons/7/7b/Meta_Platforms_Inc._logo.svg", website = "https://meta.com", industry = "Social Media", headquarters = "Menlo Park, CA"),
            CompanyEntity(name = "Netflix", domain = "netflix.com", logoUrl = "https://upload.wikimedia.org/wikipedia/commons/0/08/Netflix_2015_logo.svg", website = "https://netflix.com", industry = "Entertainment", headquarters = "Los Gatos, CA"),
            CompanyEntity(name = "Amazon", domain = "amazon.com", logoUrl = "https://upload.wikimedia.org/wikipedia/commons/a/a9/Amazon_logo.svg", website = "https://amazon.com", industry = "E-commerce", headquarters = "Seattle, WA")
        )
        return companies.map { companyDao.insertCompany(it) }
    }

    private suspend fun seedJobs(companyIds: List<Long>) {
        val jobs = listOf(
            JobEntity(
                companyId = companyIds[0],
                title = "Android Engineer (Pixel)",
                location = "Mountain View, CA",
                type = "FULL_TIME",
                remoteType = "ON_SITE",
                experienceLevel = "SENIOR",
                salaryMin = 150000.0,
                salaryMax = 220000.0,
                currency = "USD",
                description = "Work on the core system apps for Pixel devices.",
                descriptionHtml = null,
                url = "https://google.com/jobs/1",
                sourceProviderId = "SEED",
                postedDate = System.currentTimeMillis()
            ),
            JobEntity(
                companyId = companyIds[1],
                title = "Mobile Product Designer",
                location = "Menlo Park, CA",
                type = "CONTRACT",
                remoteType = "HYBRID",
                experienceLevel = "MID_LEVEL",
                salaryMin = 100.0,
                salaryMax = 150.0,
                currency = "USD",
                description = "Design next-generation social experiences for mobile.",
                descriptionHtml = null,
                url = "https://meta.com/jobs/2",
                sourceProviderId = "SEED",
                postedDate = System.currentTimeMillis() - 86400000
            ),
            JobEntity(
                companyId = companyIds[2],
                title = "Senior Data Scientist",
                location = "Los Gatos, CA",
                type = "FULL_TIME",
                remoteType = "ON_SITE",
                experienceLevel = "SENIOR",
                salaryMin = 250000.0,
                salaryMax = 300000.0,
                currency = "USD",
                description = "Optimize recommendation algorithms for millions of users.",
                descriptionHtml = null,
                url = "https://netflix.com/jobs/3",
                sourceProviderId = "SEED",
                postedDate = System.currentTimeMillis() - 172800000
            ),
            JobEntity(
                companyId = companyIds[3],
                title = "AWS Solutions Architect",
                location = "Seattle, WA",
                type = "FULL_TIME",
                remoteType = "REMOTE",
                experienceLevel = "SENIOR",
                salaryMin = 160000.0,
                salaryMax = 240000.0,
                currency = "USD",
                description = "Help enterprise customers migrate to the cloud.",
                descriptionHtml = null,
                url = "https://amazon.com/jobs/4",
                sourceProviderId = "SEED",
                postedDate = System.currentTimeMillis() - 259200000
            )
        )
        jobDao.insertJobs(jobs)
    }
}
