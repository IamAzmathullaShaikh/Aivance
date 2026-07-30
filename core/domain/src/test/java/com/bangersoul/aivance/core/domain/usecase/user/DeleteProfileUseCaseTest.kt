package com.bangersoul.aivance.core.domain.usecase.user

import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteProfileUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: DeleteProfileUseCase

    @Before
    fun setUp() {
        userRepository = mockk()
        useCase = DeleteProfileUseCase(userRepository)
    }

    @Test
    fun `should clear profile data`() = runTest {
        val profile = UserProfile(fullName = "John Doe", email = "john@example.com")
        coEvery { userRepository.getProfile() } returns flowOf(Result.Success(profile))
        coEvery { userRepository.updateProfile(any()) } returns Result.Success(Unit)

        val result = useCase()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should handle already deleted profile`() = runTest {
        coEvery { userRepository.getProfile() } returns flowOf(
            Result.Failure(com.bangersoul.aivance.core.common.result.DatabaseError("Not found"))
        )
        val result = useCase()
        assertTrue(result.isFailure)
    }
}
