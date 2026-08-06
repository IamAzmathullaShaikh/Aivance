package com.bangersoul.aivance.core.database.dao

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bangersoul.aivance.core.database.AivanceDatabase
import com.bangersoul.aivance.core.database.buildTestDatabase
import com.bangersoul.aivance.core.database.model.InterviewMessageEntity
import com.bangersoul.aivance.core.database.model.InterviewSessionEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class InterviewFeatureDaoTest {

    private lateinit var db: AivanceDatabase
    private lateinit var interviewDao: InterviewDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = buildTestDatabase(context)
        interviewDao = db.interviewDao()
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun interviewSessionAndMessagesIntegration() = runTest {
        val sessionId = interviewDao.insertSession(
            InterviewSessionEntity(
                id = 1,
                targetRole = "Android Developer",
                type = "BEHAVIORAL",
                difficulty = "Medium",
                dateStarted = Instant.now(),
                isCompleted = false,
                overallFeedback = null
            )
        )

        interviewDao.insertMessage(
            InterviewMessageEntity(sessionId = sessionId, role = "AI", text = "Tell me about yourself", timestamp = Instant.now())
        )
        interviewDao.insertMessage(
            InterviewMessageEntity(sessionId = sessionId, role = "USER", text = "I am a developer", timestamp = Instant.now())
        )

        val sessionWithMessages = interviewDao.getInterviewSessionWithMessagesById(sessionId)
        assertThat(sessionWithMessages).isNotNull()
        assertThat(sessionWithMessages?.session?.targetRole).isEqualTo("Android Developer")
        assertThat(sessionWithMessages?.messages).hasSize(2)
    }
}
