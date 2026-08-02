package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.user.CreateProfileRequest
import com.bangersoul.aivance.core.domain.usecase.user.CreateProfileUseCase
import com.bangersoul.aivance.core.domain.usecase.user.DeleteProfileUseCase
import com.bangersoul.aivance.core.domain.usecase.user.LoadProfileUseCase
import com.bangersoul.aivance.core.domain.usecase.user.UpdateProfileRequest
import com.bangersoul.aivance.core.domain.usecase.user.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(
        val profile: UserProfile? = null,
        val fullName: String = "",
        val email: String = "",
        val phone: String = "",
        val currentRole: String = "",
        val company: String = "",
        val linkedinUrl: String = "",
        val githubUrl: String = "",
        val dateOfBirth: Long? = null,
        val profilePictureUrl: String? = null,
        val targetRole: String = "",
        val skills: String = "",
        val experienceYears: Int = 0,
        val apiKey: String = "",
        val isEditing: Boolean = false,
        val isDirty: Boolean = false,
        val isSaving: Boolean = false
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

sealed interface ProfileUiEvent {
    data class UpdateFullName(val name: String) : ProfileUiEvent
    data class UpdateEmail(val email: String) : ProfileUiEvent
    data class UpdatePhone(val phone: String) : ProfileUiEvent
    data class UpdateCurrentRole(val role: String) : ProfileUiEvent
    data class UpdateCompany(val company: String) : ProfileUiEvent
    data class UpdateLinkedIn(val url: String) : ProfileUiEvent
    data class UpdateGithub(val url: String) : ProfileUiEvent
    data class UpdateDateOfBirth(val date: Long?) : ProfileUiEvent
    data class UpdateProfilePicture(val url: String?) : ProfileUiEvent
    data class UpdateTargetRole(val role: String) : ProfileUiEvent
    data class UpdateSkills(val skills: String) : ProfileUiEvent
    data class UpdateExperience(val years: Int) : ProfileUiEvent
    data class UpdateApiKey(val key: String) : ProfileUiEvent
    data object ToggleEdit : ProfileUiEvent
    data object SaveProfile : ProfileUiEvent
    data object DeleteProfile : ProfileUiEvent
    data object LoadProfile : ProfileUiEvent
}

sealed interface ProfileUiEffect {
    data class ShowSnackbar(val message: String) : ProfileUiEffect
    data class ValidationError(val field: String, val message: String) : ProfileUiEffect
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val loadProfileUseCase: LoadProfileUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ProfileUiEffect>(Channel.BUFFERED)
    val effects: Flow<ProfileUiEffect> = _effects.receiveAsFlow()

    init { loadProfile() }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.UpdateFullName -> updateField { it.copy(fullName = event.name, isDirty = true) }
            is ProfileUiEvent.UpdateEmail -> updateField { it.copy(email = event.email, isDirty = true) }
            is ProfileUiEvent.UpdatePhone -> updateField { it.copy(phone = event.phone, isDirty = true) }
            is ProfileUiEvent.UpdateCurrentRole -> updateField { it.copy(currentRole = event.role, isDirty = true) }
            is ProfileUiEvent.UpdateCompany -> updateField { it.copy(company = event.company, isDirty = true) }
            is ProfileUiEvent.UpdateLinkedIn -> updateField { it.copy(linkedinUrl = event.url, isDirty = true) }
            is ProfileUiEvent.UpdateGithub -> updateField { it.copy(githubUrl = event.url, isDirty = true) }
            is ProfileUiEvent.UpdateDateOfBirth -> updateField { it.copy(dateOfBirth = event.date, isDirty = true) }
            is ProfileUiEvent.UpdateProfilePicture -> updateField { it.copy(profilePictureUrl = event.url, isDirty = true) }
            is ProfileUiEvent.UpdateTargetRole -> updateField { it.copy(targetRole = event.role, isDirty = true) }
            is ProfileUiEvent.UpdateSkills -> updateField { it.copy(skills = event.skills, isDirty = true) }
            is ProfileUiEvent.UpdateExperience -> updateField { it.copy(experienceYears = event.years, isDirty = true) }
            is ProfileUiEvent.UpdateApiKey -> updateApiKey(event.key)
            ProfileUiEvent.ToggleEdit -> toggleEdit()
            ProfileUiEvent.SaveProfile -> saveProfile()
            ProfileUiEvent.DeleteProfile -> deleteProfile()
            ProfileUiEvent.LoadProfile -> loadProfile()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                loadProfileUseCase.invoke().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val profile = result.data
                            _uiState.value = ProfileUiState.Success(
                                profile = profile,
                                fullName = profile.fullName,
                                email = profile.email,
                                phone = profile.phone,
                                currentRole = profile.currentRole,
                                company = profile.company,
                                linkedinUrl = profile.linkedinUrl,
                                githubUrl = profile.githubUrl,
                                dateOfBirth = profile.dateOfBirth,
                                profilePictureUrl = profile.profilePictureUrl,
                                targetRole = profile.targetRole,
                                skills = profile.skills.joinToString(", "),
                                experienceYears = profile.experienceYears
                            )
                        }
                        is Result.Failure -> {
                            _uiState.value = ProfileUiState.Success()
                        }
                    }
                }
            } catch (_: Exception) {
                _uiState.value = ProfileUiState.Error("Failed to load profile")
            }
        }
    }

    private fun toggleEdit() {
        val current = _uiState.value as? ProfileUiState.Success ?: return
        if (current.isEditing) {
            // Leaving edit mode without saving discards the local edits by
            // reloading the persisted profile.
            loadProfile()
        } else {
            _uiState.value = current.copy(isEditing = true, isDirty = false)
        }
    }

    private fun saveProfile() {
        val currentState = _uiState.value as? ProfileUiState.Success ?: return
        if (currentState.fullName.isBlank()) {
            viewModelScope.launch { _effects.send(ProfileUiEffect.ValidationError("name", "Name is required")) }
            return
        }
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "profile_save"))
            _uiState.value = currentState.copy(isSaving = true)

            val request = UpdateProfileRequest(
                fullName = currentState.fullName,
                email = currentState.email,
                phone = currentState.phone,
                targetRole = currentState.targetRole,
                currentRole = currentState.currentRole,
                company = currentState.company,
                linkedinUrl = currentState.linkedinUrl,
                githubUrl = currentState.githubUrl,
                dateOfBirth = currentState.dateOfBirth,
                profilePictureUrl = currentState.profilePictureUrl,
                skills = currentState.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                experienceYears = currentState.experienceYears
            )
            val result = updateProfileUseCase(request)
            when (result) {
                is Result.Success -> {
                    _uiState.value = currentState.copy(isSaving = false, isDirty = false, isEditing = false)
                    sendEffect(ProfileUiEffect.ShowSnackbar("Profile saved"))
                }
                is Result.Failure -> {
                    _uiState.value = currentState.copy(isSaving = false)
                    sendEffect(ProfileUiEffect.ShowSnackbar(result.error.message ?: "Failed"))
                }
            }
        }
    }

    private fun deleteProfile() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "profile_delete"))
            deleteProfileUseCase()
            _uiState.value = ProfileUiState.Success()
            sendEffect(ProfileUiEffect.ShowSnackbar("Profile deleted"))
        }
    }

    private fun updateApiKey(key: String) {
        viewModelScope.launch { userPreferencesRepository.updateGeminiApiKey(key) }
        updateField { it.copy(apiKey = key) }
    }

    private fun updateField(transform: (ProfileUiState.Success) -> ProfileUiState.Success) {
        val current = _uiState.value
        if (current is ProfileUiState.Success) _uiState.value = transform(current)
    }

    private fun sendEffect(effect: ProfileUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
