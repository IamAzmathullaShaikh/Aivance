package com.bangersoul.aivance.core.domain.usecase.user

import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateProfileUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: UpdateProfileUseCase

    @Before
    fun setUp() {
        userRepository = mockk()
        useCase = UpdateProfileUseCase(userRepository)
    }

    @Test
    fun `should update profile with partial data`() = runTest {
        val existing = UserProfile(fullName = "John Doe", email = "john@example.com", targetRole = "Developer")
        coEvery { userRepository.getProfile() } returns flowOf(Result.Success(existing))
        coEvery { userRepository.updateProfile(any()) } returns Result.Success(Unit)

        val result = useCase(UpdateProfileRequest(fullName = "Jane Doe", targetRole = "Senior Dev"))
        assertTrue(result.isSuccess)
        val profile = (result as Result.Success).data
        assertEquals("Jane Doe", profile.fullName)
        assertEquals("Senior Dev", profile.targetRole)
    }

    @Test
    fun `should fail for invalid email`() = runTest {
        val result = useCase(UpdateProfileRequest(email = "invalid"))
        assertTrue(result.isFailure)
    }
}
