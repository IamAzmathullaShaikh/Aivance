package com.bangersoul.aivance.feature.interview

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.ai.ClearConversationUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.SendMessageUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.StartConversationUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.StreamResponseUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.SummariseConversationUseCase
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
    private val mockStreamResponse: StreamResponseUseCase = mockk()
    private val mockSummariseConversation: SummariseConversationUseCase = mockk()
    private val mockClearConversation: ClearConversationUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any<TrackEventRequest>()) } returns Result.Success(Unit)
        coEvery { mockStartConversation(any()) } returns flowOf(Result.Success("conv_1"))
        coEvery { mockSendMessage(any()) } returns flowOf(Result.Success("AI response"))
        coEvery { mockClearConversation() } returns flowOf(Result.Success(Unit))
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is Idle`() {
        val viewModel = AiChatViewModel(
            startConversationUseCase = mockStartConversation,
            sendMessageUseCase = mockSendMessage,
            streamResponseUseCase = mockStreamResponse,
            summariseConversationUseCase = mockSummariseConversation,
            clearConversationUseCase = mockClearConversation,
            trackEventUseCase = mockTrackEvent
        )
        assertEquals(AiChatUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `startNewChat transitions to Chatting`() = runTest {
        val viewModel = AiChatViewModel(
            startConversationUseCase = mockStartConversation,
            sendMessageUseCase = mockSendMessage,
            streamResponseUseCase = mockStreamResponse,
            summariseConversationUseCase = mockSummariseConversation,
            clearConversationUseCase = mockClearConversation,
            trackEventUseCase = mockTrackEvent
        )
        viewModel.onEvent(AiChatUiEvent.StartNewChat)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AiChatUiState.Chatting)
    }

    @Test
    fun `sendMessage adds message to conversation`() = runTest {
        val viewModel = AiChatViewModel(
            startConversationUseCase = mockStartConversation,
            sendMessageUseCase = mockSendMessage,
            streamResponseUseCase = mockStreamResponse,
            summariseConversationUseCase = mockSummariseConversation,
            clearConversationUseCase = mockClearConversation,
            trackEventUseCase = mockTrackEvent
        )
        viewModel.onEvent(AiChatUiEvent.StartNewChat)
        advanceUntilIdle()
        viewModel.onEvent(AiChatUiEvent.SendMessage("Hello AI"))
        advanceUntilIdle()
        coVerify { mockSendMessage(any()) }
    }

    @Test
    fun `clearConversation resets to Idle`() = runTest {
        val viewModel = AiChatViewModel(
            startConversationUseCase = mockStartConversation,
            sendMessageUseCase = mockSendMessage,
            streamResponseUseCase = mockStreamResponse,
            summariseConversationUseCase = mockSummariseConversation,
            clearConversationUseCase = mockClearConversation,
            trackEventUseCase = mockTrackEvent
        )
        viewModel.onEvent(AiChatUiEvent.StartNewChat)
        advanceUntilIdle()
        viewModel.onEvent(AiChatUiEvent.ClearConversation)
        advanceUntilIdle()
        assertEquals(AiChatUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `sendMessage when failing shows error state`() = runTest {
        coEvery { mockSendMessage(any()) } returns flowOf(
            Result.Failure(com.bangersoul.aivance.core.common.result.DomainError("API Error"))
        )
        val viewModel = AiChatViewModel(
            startConversationUseCase = mockStartConversation,
            sendMessageUseCase = mockSendMessage,
            streamResponseUseCase = mockStreamResponse,
            summariseConversationUseCase = mockSummariseConversation,
            clearConversationUseCase = mockClearConversation,
            trackEventUseCase = mockTrackEvent
        )
        viewModel.onEvent(AiChatUiEvent.StartNewChat)
        advanceUntilIdle()
        viewModel.onEvent(AiChatUiEvent.SendMessage("Hello"))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AiChatUiState.Error)
        val error = viewModel.uiState.value as AiChatUiState.Error
        assertTrue(error.message.contains("API Error"))
    }
}
