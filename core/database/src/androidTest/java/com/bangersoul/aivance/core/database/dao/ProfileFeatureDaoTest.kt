package com.bangersoul.aivance.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.bangersoul.aivance.core.database.AivanceDatabase
import com.bangersoul.aivance.core.database.model.RoadmapEntity
import com.bangersoul.aivance.core.database.model.RoadmapStepEntity
import com.bangersoul.aivance.core.database.model.UserProfileEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileFeatureDaoTest {

    private lateinit var db: AivanceDatabase
    private lateinit var profileDao: ProfileDao
    private lateinit var roadmapDao: RoadmapDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AivanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        profileDao = db.profileDao()
        roadmapDao = db.roadmapDao()
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun profileAndRoadmapIntegration() = runTest {
        val profile = UserProfileEntity(
            id = "user123",
            name = "John Doe",
            email = "john@example.com",
            bio = "Android enthusiast",
            skills = listOf("Kotlin", "Compose"),
            targetRole = "Senior Android Developer",
            currentRole = "Android Developer",
            profilePictureUrl = null
        )
        profileDao.insertProfile(profile)

        val roadmap = RoadmapEntity(
            id = 1,
            targetRole = "Senior Android Developer",
            currentLevel = "Senior",
            description = "Path to mastery",
            dateCreated = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
        val steps = listOf(
            RoadmapStepEntity(roadmapId = 1, title = "Master Coroutines", description = "Deep dive", stepOrder = 0, isCompleted = false),
            RoadmapStepEntity(roadmapId = 1, title = "Master Compose", description = "UI excellence", stepOrder = 1, isCompleted = false)
        )
        roadmapDao.insertRoadmapWithSteps(roadmap, steps)

        profileDao.getUserProfile().test {
            val savedProfile = awaitItem()
            assertThat(savedProfile?.id).isEqualTo("user123")
        }

        roadmapDao.getRoadmapsWithSteps().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list[0].roadmap.targetRole).isEqualTo("Senior Android Developer")
            assertThat(list[0].steps).hasSize(2)
        }
    }
}
