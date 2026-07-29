package com.bangersoul.aivance.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.bangersoul.aivance.core.database.AivanceDatabase
import com.bangersoul.aivance.core.database.model.ApplicationEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationDaoTest {

    private lateinit var database: AivanceDatabase
    private lateinit var applicationDao: ApplicationDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AivanceDatabase::class.java
        ).allowMainThreadQueries().build()
        applicationDao = database.applicationDao()
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun insertApplication_insertsItem() = runTest {
        val application = ApplicationEntity(
            id = 1,
            company = "Google",
            role = "Software Engineer",
            status = "Applied",
            dateApplied = 1000L,
            salaryRange = "150k - 200k",
            notes = "Test notes",
            lastModified = 1000L
        )
        applicationDao.insertApplication(application)

        val result = applicationDao.getApplicationById(1)
        assertThat(result).isEqualTo(application)
    }

    @Test
    fun getAllApplications_returnsFlow() = runTest {
        val app1 = ApplicationEntity(
            id = 1,
            company = "Google",
            role = "SWE",
            status = "Applied",
            dateApplied = 1000L,
            salaryRange = null,
            notes = null,
            lastModified = 1000L
        )
        val app2 = ApplicationEntity(
            id = 2,
            company = "Meta",
            role = "SWE",
            status = "Applied",
            dateApplied = 2000L,
            salaryRange = null,
            notes = null,
            lastModified = 2000L
        )

        applicationDao.insertApplication(app1)
        applicationDao.insertApplication(app2)

        applicationDao.getApplications().test {
            val list = awaitItem()
            assertThat(list).hasSize(2)
            assertThat(list[0]).isEqualTo(app2) // Sorted by dateApplied DESC
            assertThat(list[1]).isEqualTo(app1)
        }
    }

    @Test
    fun updateStatus_updatesItem() = runTest {
        val application = ApplicationEntity(
            id = 1,
            company = "Google",
            role = "SWE",
            status = "Applied",
            dateApplied = 1000L,
            salaryRange = null,
            notes = null,
            lastModified = 1000L
        )
        applicationDao.insertApplication(application)

        val newStatus = "Interviewing"
        val newLastModified = 2000L
        applicationDao.updateStatus(1, newStatus, newLastModified)

        val result = applicationDao.getApplicationById(1)
        assertThat(result?.status).isEqualTo(newStatus)
        assertThat(result?.lastModified).isEqualTo(newLastModified)
    }

    @Test
    fun deleteApplication_removesItem() = runTest {
        val application = ApplicationEntity(
            id = 1,
            company = "Google",
            role = "SWE",
            status = "Applied",
            dateApplied = 1000L,
            salaryRange = null,
            notes = null,
            lastModified = 1000L
        )
        applicationDao.insertApplication(application)
        applicationDao.deleteApplication(application)

        val result = applicationDao.getApplicationById(1)
        assertThat(result).isNull()
    }
}
