package com.bangersoul.aivance.core.domain.usecase.user

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateProfileUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var useCase: CreateProfileUseCase

    @Before
    fun setUp() {
        userRepository = mockk()
        useCase = CreateProfileUseCase(userRepository)
    }

    @Test
    fun `should create profile successfully`() = runTest {
        coEvery { userRepository.updateProfile(any()) } returns Result.Success(Unit)

        val result = useCase(CreateProfileRequest(fullName = "John Doe", email = "john@example.com"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank name`() = runTest {
        val result = useCase(CreateProfileRequest(fullName = "", email = "john@example.com"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for blank email`() = runTest {
        val result = useCase(CreateProfileRequest(fullName = "John Doe", email = ""))
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for invalid email`() = runTest {
        val result = useCase(CreateProfileRequest(fullName = "John Doe", email = "not-an-email"))
        assertTrue(result.isFailure)
    }
}
