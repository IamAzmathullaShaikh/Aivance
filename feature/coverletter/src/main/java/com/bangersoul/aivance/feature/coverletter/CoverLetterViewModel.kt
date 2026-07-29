package com.bangersoul.aivance.feature.coverletter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.util.ClipboardHelper
import com.bangersoul.aivance.feature.coverletter.domain.model.CoverLetter
import com.bangersoul.aivance.feature.coverletter.domain.model.LetterTone
import com.bangersoul.aivance.feature.coverletter.domain.repository.CoverLetterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CoverLetterUiState {
    data object Idle : CoverLetterUiState
    data object Generating : CoverLetterUiState
    data class Success(val content: String) : CoverLetterUiState
    data class Error(val message: String) : CoverLetterUiState
}

@HiltViewModel
class CoverLetterViewModel @Inject constructor(
    private val repository: CoverLetterRepository,
    private val clipboardHelper: ClipboardHelper
) : ViewModel() {

    var resumeText by mutableStateOf("")
        private set

    var jobDescription by mutableStateOf("")
        private set

    var selectedTone by mutableStateOf(LetterTone.PROFESSIONAL)
        private set

    var uiState by mutableStateOf<CoverLetterUiState>(CoverLetterUiState.Idle)
        private set

    fun onResumeChange(text: String) {
        resumeText = text
    }

    fun onJobDescriptionChange(text: String) {
        jobDescription = text
    }

    fun onToneChange(tone: LetterTone) {
        selectedTone = tone
    }

    fun generate() {
        if (resumeText.isBlank() || jobDescription.isBlank()) {
            uiState = CoverLetterUiState.Error("Please fill in both resume and job description.")
            return
        }

        viewModelScope.launch {
            repository.generateCoverLetter(resumeText, jobDescription, selectedTone)
                .onStart { uiState = CoverLetterUiState.Generating }
                .catch { uiState = CoverLetterUiState.Error(it.message ?: "Failed to generate cover letter") }
                .collect { uiState = CoverLetterUiState.Success(it) }
        }
    }

    fun save(company: String, role: String) {
        val currentState = uiState
        if (currentState is CoverLetterUiState.Success) {
            viewModelScope.launch {
                val coverLetter = CoverLetter(
                    company = company,
                    role = role,
                    content = currentState.content,
                    dateCreated = System.currentTimeMillis(),
                    tone = selectedTone
                )
                repository.saveCoverLetter(coverLetter)
                    .catch { /* Handle error */ }
                    .collect { /* Handle success */ }
            }
        }
    }

    fun reset() {
        uiState = CoverLetterUiState.Idle
    }

    fun copyToClipboard(text: String) {
        clipboardHelper.copyToClipboard("Cover Letter", text)
    }
}
