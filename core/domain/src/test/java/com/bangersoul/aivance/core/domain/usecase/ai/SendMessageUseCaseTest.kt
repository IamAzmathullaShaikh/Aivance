package com.bangersoul.aivance.core.domain.usecase.ai

import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AiRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SendMessageUseCaseTest {

    private lateinit var aiRepository: AiRepository
    private lateinit var useCase: SendMessageUseCase

    @Before
    fun setUp() {
        aiRepository = mockk()
        useCase = SendMessageUseCase(aiRepository)
    }

    @Test
    fun `should send message successfully`() = runTest {
        val msg = AIMessage(id = "1", conversationId = "conv1", role = com.bangersoul.aivance.core.common.enums.MessageRole.USER, content = "Hello")
        coEvery { aiRepository.sendMessage(any(), any()) } returns Result.Success(msg)

        val result = useCase(SendMessageRequest(conversationId = "conv1", message = "Hello"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank conversation ID`() = runTest {
        val result = useCase(SendMessageRequest(conversationId = "", message = "Hello"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for blank message`() = runTest {
        val result = useCase(SendMessageRequest(conversationId = "conv1", message = ""))
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for oversized message`() = runTest {
        val result = useCase(SendMessageRequest(conversationId = "conv1", message = "A".repeat(50001)))
        assertTrue(result.isFailure)
    }
}
