package com.bangersoul.aivance.core.domain.usecase.ai

import com.bangersoul.aivance.core.common.model.AIConversation
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AiRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartConversationUseCaseTest {

    private lateinit var aiRepository: AiRepository
    private lateinit var useCase: StartConversationUseCase

    @Before
    fun setUp() {
        aiRepository = mockk()
        useCase = StartConversationUseCase(aiRepository)
    }

    @Test
    fun `should start conversation successfully`() = runTest {
        val conversation = AIConversation(id = "1", title = "Chat", providerId = "GEMINI", modelName = "gemini-1.5-flash")
        coEvery { aiRepository.startChatSession(any(), any()) } returns Result.Success(conversation)

        val result = useCase(StartConversationRequest(title = "Chat", providerId = "GEMINI", modelName = "gemini-1.5-flash"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank provider ID`() = runTest {
        val result = useCase(StartConversationRequest(providerId = ""))
        assertTrue(result.isFailure)
    }
}
