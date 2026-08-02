package com.bangersoul.aivance.feature.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.InterviewQuestion
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.InterviewKnowledgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface QuestionBankUiState {
    data object Loading : QuestionBankUiState
    data class Content(
        val category: String = "ALL",
        val questions: List<InterviewQuestion> = emptyList(),
        val idealAnswerFor: Long? = null,
        val idealAnswer: String? = null,
        val isIdealAnswerLoading: Boolean = false,
        val idealAnswerError: String? = null
    ) : QuestionBankUiState
    data class Error(val message: String) : QuestionBankUiState
}

sealed interface QuestionBankUiEvent {
    data class SelectCategory(val category: String) : QuestionBankUiEvent
    data class ToggleFavorite(val questionId: Long) : QuestionBankUiEvent
    data class ViewIdealAnswer(val questionId: Long) : QuestionBankUiEvent
    data object Retry : QuestionBankUiEvent
}

@HiltViewModel
class QuestionBankViewModel @Inject constructor(
    private val knowledgeRepository: InterviewKnowledgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuestionBankUiState>(QuestionBankUiState.Loading)
    val uiState: StateFlow<QuestionBankUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadQuestions("ALL")
    }

    fun onEvent(event: QuestionBankUiEvent) {
        when (event) {
            is QuestionBankUiEvent.SelectCategory -> loadQuestions(event.category)
            is QuestionBankUiEvent.ToggleFavorite -> toggleFavorite(event.questionId)
            is QuestionBankUiEvent.ViewIdealAnswer -> viewIdealAnswer(event.questionId)
            QuestionBankUiEvent.Retry -> loadQuestions((_uiState.value as? QuestionBankUiState.Content)?.category ?: "ALL")
        }
    }

    private fun loadQuestions(category: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = QuestionBankUiState.Loading
            val flow = if (category == "FAVORITES") {
                knowledgeRepository.getFavoriteQuestions()
            } else {
                knowledgeRepository.getCommonQuestions(category)
            }
            flow
                .catch { emit(Result.Failure(com.bangersoul.aivance.core.common.result.DomainError(it.message ?: "Failed to load questions"))) }
                .collect { result ->
                    when (result) {
                        is Result.Success -> _uiState.value = QuestionBankUiState.Content(
                            category = category,
                            questions = result.data
                        )
                        is Result.Failure -> _uiState.value = QuestionBankUiState.Error(result.error.message)
                    }
                }
        }
    }

    private fun toggleFavorite(questionId: Long) {
        viewModelScope.launch {
            // The library/favorites flows are live Room flows, so the DB write
            // triggers an automatic re-emission that reconciles the UI — no
            // optimistic flip needed (and an in-place flip could double-flip
            // if the flow re-emits before this coroutine resumes).
            knowledgeRepository.toggleFavorite(questionId)
        }
    }

    private fun viewIdealAnswer(questionId: Long) {
        val current = _uiState.value as? QuestionBankUiState.Content ?: return
        // Guard against duplicate AI calls while one is already in flight.
        if (current.isIdealAnswerLoading) return
        // Collapse only when the answer is already shown; after an error the
        // next tap retries the AI call instead of silently collapsing.
        if (current.idealAnswerFor == questionId && current.idealAnswerError == null) {
            _uiState.value = current.copy(idealAnswerFor = null, idealAnswer = null, isIdealAnswerLoading = false, idealAnswerError = null)
            return
        }
        _uiState.value = current.copy(idealAnswerFor = questionId, isIdealAnswerLoading = true, idealAnswer = null, idealAnswerError = null)
        viewModelScope.launch {
            val result = knowledgeRepository.getIdealAnswer(questionId)
            val state = _uiState.value as? QuestionBankUiState.Content ?: return@launch
            _uiState.value = when (result) {
                is Result.Success -> state.copy(idealAnswer = result.data, isIdealAnswerLoading = false, idealAnswerError = null)
                is Result.Failure -> state.copy(idealAnswer = null, isIdealAnswerLoading = false, idealAnswerError = result.error.message)
            }
        }
    }
}
