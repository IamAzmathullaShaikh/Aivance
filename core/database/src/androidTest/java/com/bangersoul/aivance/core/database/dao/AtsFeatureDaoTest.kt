package com.bangersoul.aivance.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.bangersoul.aivance.core.database.AivanceDatabase
import com.bangersoul.aivance.core.database.model.ResumeAnalysisEntity
import com.bangersoul.aivance.core.database.model.ResumeEntity
import com.bangersoul.aivance.core.database.model.ResumeSectionEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AtsFeatureDaoTest {

    private lateinit var db: AivanceDatabase
    private lateinit var atsDao: AtsDao
    private lateinit var resumeDao: ResumeDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AivanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        atsDao = db.atsDao()
        resumeDao = db.resumeDao()
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun resumeAndAtsResultIntegration() = runTest {
        // 1. Insert Resume
        val resume = ResumeEntity(
            id = 1,
            name = "John_Doe_Resume.pdf",
            text = "Experienced Android Developer...",
            dateCreated = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
        resumeDao.insertResume(resume)

        // 2. Insert Resume Sections
        val sections = listOf(
            ResumeSectionEntity(resumeId = 1, title = "Experience", content = "...", sectionOrder = 0),
            ResumeSectionEntity(resumeId = 1, title = "Skills", content = "...", sectionOrder = 1)
        )
        resumeDao.insertSections(sections)

        // 3. Perform ATS Analysis and save result
        val analysis = ResumeAnalysisEntity(
            id = 1,
            resumeId = 1,
            jobDescription = "Senior Android Engineer",
            score = 92,
            matchedKeywords = "Kotlin, Coroutines, Room",
            missingKeywords = "KMP",
            feedback = "Excellent match for the role.",
            date = System.currentTimeMillis()
        )
        atsDao.insertAtsResult(analysis)

        // 4. Verify data
        val savedResume = resumeDao.getResumeById(1)
        assertThat(savedResume?.name).isEqualTo("John_Doe_Resume.pdf")

        resumeDao.getSectionsForResume(1).test {
            val savedSections = awaitItem()
            assertThat(savedSections).hasSize(2)
            assertThat(savedSections[0].title).isEqualTo("Experience")
        }

        val savedAnalysis = atsDao.getAtsResultById(1)
        assertThat(savedAnalysis?.score).isEqualTo(92)
        assertThat(savedAnalysis?.resumeId).isEqualTo(1)
    }

    @Test
    fun deleteResume_cascadesToAnalysis() = runTest {
        val resume = ResumeEntity(id = 1, name = "test.pdf", text = "", dateCreated = 0, lastModified = 0)
        resumeDao.insertResume(resume)

        val analysis = ResumeAnalysisEntity(
            id = 1,
            resumeId = 1,
            jobDescription = "Job",
            score = 80,
            matchedKeywords = "",
            missingKeywords = "",
            feedback = "",
            date = 1000L
        )
        atsDao.insertAtsResult(analysis)

        // Delete resume
        resumeDao.deleteResume(resume)

        // Analysis should be gone due to ForeignKey CASCADE
        val result = atsDao.getAtsResultById(1)
        assertThat(result).isNull()
    }
}
