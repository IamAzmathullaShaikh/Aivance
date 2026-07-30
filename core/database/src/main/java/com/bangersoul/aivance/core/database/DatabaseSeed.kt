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
            CompanyEntity(name = "Google", logoUrl = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png", website = "https://google.com", industry = "Technology"),
            CompanyEntity(name = "Meta", logoUrl = "https://upload.wikimedia.org/wikipedia/commons/7/7b/Meta_Platforms_Inc._logo.svg", website = "https://meta.com", industry = "Social Media"),
            CompanyEntity(name = "Netflix", logoUrl = "https://upload.wikimedia.org/wikipedia/commons/0/08/Netflix_2015_logo.svg", website = "https://netflix.com", industry = "Entertainment"),
            CompanyEntity(name = "Amazon", logoUrl = "https://upload.wikimedia.org/wikipedia/commons/a/a9/Amazon_logo.svg", website = "https://amazon.com", industry = "E-commerce")
        )
        return companies.map { companyDao.insertCompany(it) }
    }

    private suspend fun seedJobs(companyIds: List<Long>) {
        val jobs = listOf(
            JobEntity(
                companyId = companyIds[0],
                title = "Android Engineer (Pixel)",
                location = "Mountain View, CA",
                type = "Full-time",
                salary = "$150,000 - $220,000",
                description = "Work on the core system apps for Pixel devices.",
                postedDate = System.currentTimeMillis()
            ),
            JobEntity(
                companyId = companyIds[1],
                title = "Mobile Product Designer",
                location = "Menlo Park, CA",
                type = "Contract",
                salary = "$100 - $150 / hr",
                description = "Design next-generation social experiences for mobile.",
                postedDate = System.currentTimeMillis() - 86400000 // 1 day ago
            ),
            JobEntity(
                companyId = companyIds[2],
                title = "Senior Data Scientist",
                location = "Los Gatos, CA",
                type = "Full-time",
                salary = "$250,000+",
                description = "Optimize recommendation algorithms for millions of users.",
                postedDate = System.currentTimeMillis() - 172800000 // 2 days ago
            ),
            JobEntity(
                companyId = companyIds[3],
                title = "AWS Solutions Architect",
                location = "Seattle, WA",
                type = "Remote",
                salary = "$160,000 - $240,000",
                description = "Help enterprise customers migrate to the cloud.",
                postedDate = System.currentTimeMillis() - 259200000 // 3 days ago
            )
        )
        jobDao.insertJobs(jobs)
    }
}
