package com.bangersoul.aivance.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.bangersoul.aivance.core.database.AivanceDatabase
import com.bangersoul.aivance.core.database.model.AtsResultEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AtsDaoTest {

    private lateinit var database: AivanceDatabase
    private lateinit var atsDao: AtsDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AivanceDatabase::class.java
        ).allowMainThreadQueries().build()
        atsDao = database.atsDao()
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun insertAtsResult_insertsItem() = runTest {
        val atsResult = AtsResultEntity(
            id = 1,
            score = 85,
            date = 1000L,
            resumeName = "resume.pdf",
            missingKeywords = "Kotlin, Room",
            feedback = "Good match"
        )
        atsDao.insertAtsResult(atsResult)

        val result = atsDao.getAtsResultById(1)
        assertThat(result).isEqualTo(atsResult)
    }

    @Test
    fun getAllAtsResults_returnsFlow() = runTest {
        val result1 = AtsResultEntity(
            id = 1,
            score = 80,
            date = 1000L,
            resumeName = "r1.pdf",
            missingKeywords = "",
            feedback = ""
        )
        val result2 = AtsResultEntity(
            id = 2,
            score = 90,
            date = 2000L,
            resumeName = "r2.pdf",
            missingKeywords = "",
            feedback = ""
        )

        atsDao.insertAtsResult(result1)
        atsDao.insertAtsResult(result2)

        atsDao.getAtsResults().test {
            val list = awaitItem()
            assertThat(list).hasSize(2)
            assertThat(list[0]).isEqualTo(result2) // Sorted by date DESC
            assertThat(list[1]).isEqualTo(result1)
        }
    }

    @Test
    fun getLatestAtsResult_returnsLatest() = runTest {
        val result1 = AtsResultEntity(
            id = 1,
            score = 80,
            date = 1000L,
            resumeName = "r1.pdf",
            missingKeywords = "",
            feedback = ""
        )
        val result2 = AtsResultEntity(
            id = 2,
            score = 90,
            date = 2000L,
            resumeName = "r2.pdf",
            missingKeywords = "",
            feedback = ""
        )

        atsDao.insertAtsResult(result1)
        atsDao.insertAtsResult(result2)

        atsDao.getLatestAtsResult().test {
            val item = awaitItem()
            assertThat(item).isEqualTo(result2)
        }
    }
}
