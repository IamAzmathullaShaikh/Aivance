package com.bangersoul.aivance.feature.assistant

import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AssistantRepository
import com.bangersoul.aivance.core.domain.usecase.assistant.AssistantRequest
import com.bangersoul.aivance.core.domain.usecase.assistant.GetAssistantResponseUseCase
import com.bangersoul.aivance.core.domain.usecase.user.LoadProfileUseCase
import com.bangersoul.aivance.sdk.core.ProviderStatus
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
class AssistantViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: AssistantRepository = mockk()
    private val mockResponseUseCase: GetAssistantResponseUseCase = mockk()
    private val mockProviderManager: ProviderManager = mockk()
    private val mockLoadProfile: LoadProfileUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRepository.saveMessage(any(), any(), any()) } returns Result.Success(1L)
        every { mockProviderManager.providerStatuses } returns MutableStateFlow(
            mapOf("groq" to ProviderStatus.Active)
        )
        every { mockLoadProfile.invoke() } returns flowOf(
            Result.Success(UserProfile(fullName = "Azmath", email = "a@b.c"))
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AssistantViewModel(
        mockRepository,
        mockResponseUseCase,
        mockProviderManager,
        mockLoadProfile
    )

    @Test
    fun `provider status surfaces ready provider`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        // WhileSubscribed(5s) StateFlows only materialize once subscribed.
        backgroundScope.launch { viewModel.providerStatus.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.providerStatus.value.isReady)
        assertEquals("Groq", viewModel.providerStatus.value.providerName)
    }

    @Test
    fun `user name is first name only`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        // WhileSubscribed(5s) StateFlows only materialize once subscribed.
        backgroundScope.launch { viewModel.userName.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Azmath", viewModel.userName.value)
    }

    @Test
    fun `sendMessage streams chunks into the bubble and commits on completion`() = runTest(testDispatcher) {
        every { mockResponseUseCase.stream(any()) } returns flowOf("Hello ", "world!")

        val viewModel = createViewModel()
        viewModel.sendMessage("Hi")

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as AssistantUiState.Chatting
        assertEquals(2, state.messages.size) // user + assistant
        assertEquals("Hello world!", state.messages.last().content)
        assertEquals(null, state.streamingContent)
        assertEquals(false, state.streamFailed)
    }

    @Test
    fun `single-flight ignores a second send while streaming`() = runTest(testDispatcher) {
        every { mockResponseUseCase.stream(any()) } returns flowOf("a", "b", "c")

        val viewModel = createViewModel()
        viewModel.sendMessage("first")
        viewModel.sendMessage("second") // ignored while in flight

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as AssistantUiState.Chatting
        // Only ONE user message should be present (first); the second was dropped.
        assertEquals(2, state.messages.size)
        assertEquals("first", state.messages.first().content)
    }

    @Test
    fun `stream failure with partial text persists partial and sets streamFailed`() = runTest(testDispatcher) {
        every { mockResponseUseCase.stream(any()) } returns flow {
            emit("partial ")
            throw RuntimeException("connection lost")
        }

        val viewModel = createViewModel()
        viewModel.sendMessage("hi")

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AssistantUiState.Chatting)
        val chatting = state as AssistantUiState.Chatting
        assertEquals(true, chatting.streamFailed)
        assertEquals("partial ", chatting.streamingContent)
    }

    @Test
    fun `stream failure with no text shows error`() = runTest(testDispatcher) {
        every { mockResponseUseCase.stream(any()) } returns flow<String> { throw RuntimeException("boom") }

        val viewModel = createViewModel()
        viewModel.sendMessage("hi")

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AssistantUiState.Error)
    }

    @Test
    fun `retry resends the last user message`() = runTest(testDispatcher) {
        var calls = 0
        every { mockResponseUseCase.stream(any()) } answers {
            calls++
            if (calls == 1) flow<String> { throw RuntimeException("boom") } else flowOf("ok")
        }

        val viewModel = createViewModel()
        viewModel.sendMessage("please")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AssistantUiState.Error)

        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as AssistantUiState.Chatting
        assertEquals("ok", state.messages.last().content)
    }
}
