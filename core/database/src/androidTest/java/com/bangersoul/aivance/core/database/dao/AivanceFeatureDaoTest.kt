package com.bangersoul.aivance.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.bangersoul.aivance.core.database.AivanceDatabase
import com.bangersoul.aivance.core.database.model.AIConversationEntity
import com.bangersoul.aivance.core.database.model.AIMessageEntity
import com.bangersoul.aivance.core.database.model.AivanceEntity
import com.bangersoul.aivance.core.database.model.AnalyticsEventEntity
import com.bangersoul.aivance.core.database.model.ProviderConfigurationEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class AivanceFeatureDaoTest {

    private lateinit var db: AivanceDatabase
    private lateinit var aivanceDao: AivanceDao
    private lateinit var aiAnalyticsDao: AiAnalyticsDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AivanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        aivanceDao = db.aivanceDao()
        aiAnalyticsDao = db.aiAnalyticsDao()
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun aivanceEntityIntegration() = runTest {
        val entity = AivanceEntity(id = 1, name = "Test Entity")
        aivanceDao.insertEntity(entity)

        aivanceDao.getAllEntities().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list[0].name).isEqualTo("Test Entity")
        }
    }

    @Test
    fun aiAnalyticsIntegration() = runTest {
        val now = Instant.now()
        val conversation = AIConversationEntity(id = "conv1", title = "Resume Help", createdAt = now, updatedAt = now)
        aiAnalyticsDao.insertConversation(conversation)

        val message = AIMessageEntity(id = "msg1", conversationId = "conv1", role = "USER", content = "Hello", timestamp = now)
        aiAnalyticsDao.insertMessage(message)

        aiAnalyticsDao.getConversationsWithMessages().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list[0].conversation.title).isEqualTo("Resume Help")
            assertThat(list[0].messages).hasSize(1)
        }
    }

    @Test
    fun providerConfigIntegration() = runTest {
        val config = ProviderConfigurationEntity(
            provider = "GEMINI",
            apiKey = "key123",
            baseUrl = null,
            settings = emptyMap()
        )
        aiAnalyticsDao.insertProviderConfig(config)

        val savedConfig = aiAnalyticsDao.getProviderConfig("GEMINI")
        assertThat(savedConfig?.apiKey).isEqualTo("key123")
    }

    @Test
    fun analyticsEventIntegration() = runTest {
        val event = AnalyticsEventEntity(
            id = 1,
            eventName = "app_open",
            params = mapOf("source" to "launcher"),
            timestamp = Instant.now()
        )
        aiAnalyticsDao.insertAnalyticsEvent(event)

        aiAnalyticsDao.getAnalyticsEvents().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list[0].eventName).isEqualTo("app_open")
        }
    }
}
