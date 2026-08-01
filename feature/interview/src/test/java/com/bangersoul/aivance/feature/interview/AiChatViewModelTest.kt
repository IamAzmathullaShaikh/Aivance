package com.bangersoul.aivance.feature.interview

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.enums.MessageRole
import com.bangersoul.aivance.core.common.model.AIConversation
import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.ai.ClearConversationUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.RegenerateResponseUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.SendMessageUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.StartConversationUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.SummariseConversationUseCase
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockStartConversation: StartConversationUseCase = mockk()
    private val mockSendMessage: SendMessageUseCase = mockk()
    private val mockRegenerateResponse: RegenerateResponseUseCase = mockk()
    private val mockSummariseConversation: SummariseConversationUseCase = mockk()
    private val mockClearConversation: ClearConversationUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private val conversation = AIConversation(
        id = "conv_1",
        title = "New Chat",
        providerId = "GEMINI",
        modelName = "gemini-1.5-flash"
    )

    private val aiMessage = AIMessage(
        id = "m1",
        conversationId = "conv_1",
        role = MessageRole.ASSISTANT,
        content = "AI response"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent.invoke(any()) } returns Result.Success(Unit)
        coEvery { mockStartConversation.invoke(any()) } returns Result.Success(conversation)
        coEvery { mockSendMessage.invoke(any()) } returns Result.Success(aiMessage)
        coEvery { mockRegenerateResponse.invoke(any()) } returns Result.Success(aiMessage)
        coEvery { mockSummariseConversation.invoke(any()) } returns Result.Success("Summary")
        coEvery { mockClearConversation.invoke(any()) } returns Result.Success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AiChatViewModel(
        startConversationUseCase = mockStartConversation,
        sendMessageUseCase = mockSendMessage,
        regenerateResponseUseCase = mockRegenerateResponse,
        summariseConversationUseCase = mockSummariseConversation,
        clearConversationUseCase = mockClearConversation,
        trackEventUseCase = mockTrackEvent
    )

    @Test
    fun `initial state is Idle`() {
        assertEquals(AiChatUiState.Idle, createViewModel().uiState.value)
    }

    @Test
    fun `startNewChat transitions to Chatting`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.onEvent(AiChatUiEvent.StartNewChat)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AiChatUiState.Chatting)
    }

    @Test
    fun `sendMessage adds message to conversation`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.onEvent(AiChatUiEvent.StartNewChat)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(AiChatUiEvent.SendMessage("Hello AI"))
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { mockSendMessage.invoke(any()) }
        val state = viewModel.uiState.value as AiChatUiState.Chatting
        assertEquals(2, state.messages.size) // user + assistant
    }

    @Test
    fun `clearConversation resets to Idle`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.onEvent(AiChatUiEvent.StartNewChat)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(AiChatUiEvent.ClearConversation)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(AiChatUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `sendMessage when failing shows snackbar and stays in Chatting`() = runTest(testDispatcher) {
        coEvery { mockSendMessage.invoke(any()) } returns Result.Failure(DomainError("API Error"))
        val viewModel = createViewModel()
        viewModel.onEvent(AiChatUiEvent.StartNewChat)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(AiChatUiEvent.SendMessage("Hello"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AiChatUiState.Chatting)
        viewModel.effects.test {
            // ScrollToBottom is emitted optimistically first
            skipItems(1)
            val effect = awaitItem()
            assertTrue(effect is AiChatUiEffect.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
