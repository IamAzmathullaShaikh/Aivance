package com.bangersoul.aivance.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.bangersoul.aivance.core.database.AivanceDatabase
import com.bangersoul.aivance.core.database.model.CompanyEntity
import com.bangersoul.aivance.core.database.model.JobApplicationEntity
import com.bangersoul.aivance.core.database.model.JobEntity
import com.bangersoul.aivance.core.database.model.SavedSearchEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.time.Instant

@RunWith(AndroidJUnit4::class)
class JobFeatureDaoTest {

    private lateinit var db: AivanceDatabase
    private lateinit var jobDao: JobDao
    private lateinit var companyDao: CompanyDao
    private lateinit var trackerDao: TrackerDao
    private lateinit var searchDao: SearchDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AivanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        jobDao = db.jobDao()
        companyDao = db.companyDao()
        trackerDao = db.trackerDao()
        searchDao = db.searchDao()
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun jobAndCompanyIntegration() = runTest {
        val company = CompanyEntity(
            id = 1,
            name = "Google",
            domain = null,
            logoUrl = "logo.png",
            website = "google.com",
            industry = "Tech",
            headquarters = null
        )
        companyDao.insertCompany(company)

        val job = JobEntity(
            id = 1,
            companyId = 1,
            title = "Android Developer",
            location = "Mountain View",
            type = "FULL_TIME",
            remoteType = null,
            experienceLevel = null,
            salaryMin = 150_000.0,
            salaryMax = 200_000.0,
            currency = "USD",
            description = "Develop cool apps",
            descriptionHtml = null,
            url = "https://jobs.google.com/1",
            sourceProviderId = "GREENHOUSE",
            postedDate = System.currentTimeMillis()
        )
        jobDao.insertJob(job)

        jobDao.getJobsWithDetails().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list[0].job.title).isEqualTo("Android Developer")
            assertThat(list[0].company.name).isEqualTo("Google")
        }
    }

    @Test
    fun trackerIntegration() = runTest {
        val companyId = companyDao.insertCompany(
            CompanyEntity(
                name = "Meta",
                domain = null,
                logoUrl = "",
                website = "",
                industry = "",
                headquarters = null
            )
        )
        val jobId = jobDao.insertJob(
            JobEntity(
                companyId = companyId,
                title = "Software Engineer",
                location = "",
                type = "FULL_TIME",
                remoteType = null,
                experienceLevel = null,
                salaryMin = null,
                salaryMax = null,
                currency = null,
                description = "",
                descriptionHtml = null,
                url = "",
                sourceProviderId = "UNKNOWN",
                postedDate = 1000L
            )
        )

        val application = JobApplicationEntity(
            id = 1,
            jobId = jobId,
            status = "Applied",
            dateApplied = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis(),
            salaryRange = "$100k-$150k",
            notes = "Referred by friend"
        )
        trackerDao.insertApplication(application)

        trackerDao.getApplications().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list[0].application.status).isEqualTo("Applied")
            assertThat(list[0].job.job.title).isEqualTo("Software Engineer")
            assertThat(list[0].job.company.name).isEqualTo("Meta")
        }
    }

    @Test
    fun savedSearchIntegration() = runTest {
        val search = SavedSearchEntity(
            id = 1,
            query = "Android Developer",
            filters = mapOf("salary" to "100000"),
            dateCreated = Instant.now()
        )
        searchDao.insertSavedSearch(search)

        searchDao.getSavedSearches().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list[0].query).isEqualTo("Android Developer")
        }
    }
}
