package com.bangersoul.aivance.core.domain.usecase.user

import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoadProfileUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: LoadProfileUseCase

    @Before
    fun setUp() {
        userRepository = mockk()
        useCase = LoadProfileUseCase(userRepository)
    }

    @Test
    fun `should load existing profile`() = runTest {
        val profile = UserProfile(fullName = "John Doe", email = "john@example.com")
        every { userRepository.getProfile() } returns flowOf(Result.Success(profile))

        val result = useCase.invoke().first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return default profile on error`() = runTest {
        every { userRepository.getProfile() } returns flowOf(
            Result.Failure(com.bangersoul.aivance.core.common.result.DatabaseError("Not found"))
        )

        val result = useCase.invoke().first()
        assertTrue(result.isSuccess)
    }
}
