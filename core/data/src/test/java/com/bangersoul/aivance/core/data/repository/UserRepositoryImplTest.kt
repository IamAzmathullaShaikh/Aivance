package com.bangersoul.aivance.core.data.repository

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.data.cache.CacheManager
import com.bangersoul.aivance.core.data.source.UserLocalDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryImplTest {

    private lateinit var repository: UserRepositoryImpl
    private val localDataSource: UserLocalDataSource = mockk()
    private val profileCache: CacheManager<String, UserProfile> = mockk(relaxed = true)

    @Before
    fun setUp() {
        repository = UserRepositoryImpl(localDataSource, profileCache)
    }

    @Test
    fun `getProfile returns profile and updates cache`() = runTest {
        val profile = UserProfile(fullName = "John Doe", email = "john@example.com")
        every { localDataSource.getUserProfile() } returns flowOf(profile)

        repository.getProfile().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(profile, (result as Result.Success).data)
            coVerify { profileCache.put("user_profile", profile) }
            awaitComplete()
        }
    }

    @Test
    fun `getProfile returns failure when profile is null`() = runTest {
        every { localDataSource.getUserProfile() } returns flowOf(null)

        repository.getProfile().test {
            val result = awaitItem()
            assertTrue(result is Result.Failure)
            assertEquals("Profile not found", (result as Result.Failure).error.message)
            awaitComplete()
        }
    }

    @Test
    fun `updateProfile saves to localDataSource and cache`() = runTest {
        val profile = UserProfile(fullName = "John Doe", email = "john@example.com")
        coEvery { localDataSource.saveUserProfile(profile) } returns Unit

        val result = repository.updateProfile(profile)

        assertTrue(result is Result.Success)
        coVerify { 
            localDataSource.saveUserProfile(profile)
            profileCache.put("user_profile", profile)
        }
    }
}
