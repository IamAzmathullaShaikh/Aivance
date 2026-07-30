package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.LoadSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.SaveSettingsUseCase
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

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.GENERAL
)

enum class NotificationType {
    GENERAL,
    APPLICATION_UPDATE,
    INTERVIEW_REMINDER,
    JOB_ALERT,
    ROADMAP_MILESTONE
}

sealed interface NotificationsUiState {
    data object Loading : NotificationsUiState
    data class Success(
        val notifications: List<NotificationItem> = emptyList(),
        val unreadCount: Int = 0,
        val jobAlertsEnabled: Boolean = true,
        val followUpRemindersEnabled: Boolean = true
    ) : NotificationsUiState
    data object Empty : NotificationsUiState
    data class Error(val message: String) : NotificationsUiState
}

sealed interface NotificationsUiEvent {
    data class MarkAsRead(val id: String) : NotificationsUiEvent
    data object MarkAllAsRead : NotificationsUiEvent
    data class ToggleJobAlerts(val enabled: Boolean) : NotificationsUiEvent
    data class ToggleFollowUpReminders(val enabled: Boolean) : NotificationsUiEvent
    data class DeleteNotification(val id: String) : NotificationsUiEvent
    data object Refresh : NotificationsUiEvent
}

sealed interface NotificationsUiEffect {
    data class ShowSnackbar(val message: String) : NotificationsUiEffect
    data class NavigateToTarget(val targetId: String) : NotificationsUiEffect
    data object RequestNotificationPermission : NotificationsUiEffect
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val loadSettingsUseCase: LoadSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<NotificationsUiEffect>(Channel.BUFFERED)
    val effects: Flow<NotificationsUiEffect> = _effects.receiveAsFlow()

    init {
        loadNotifications()
    }

    fun onEvent(event: NotificationsUiEvent) {
        when (event) {
            is NotificationsUiEvent.MarkAsRead -> markAsRead(event.id)
            NotificationsUiEvent.MarkAllAsRead -> markAllAsRead()
            is NotificationsUiEvent.ToggleJobAlerts -> toggleJobAlerts(event.enabled)
            is NotificationsUiEvent.ToggleFollowUpReminders -> toggleFollowUpReminders(event.enabled)
            is NotificationsUiEvent.DeleteNotification -> delete(event.id)
            NotificationsUiEvent.Refresh -> loadNotifications()
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationsUiState.Loading
            trackEventUseCase(TrackEventRequest(eventName = "notifications_load"))

            val settings = loadSettingsUseCase.invoke().firstOrNull()
            val jobAlerts = settings is com.bangersoul.aivance.core.common.result.Result.Success<*>

            _uiState.value = NotificationsUiState.Success(
                notifications = emptyList(), // Would be loaded from repo
                unreadCount = 0,
                jobAlertsEnabled = true,
                followUpRemindersEnabled = true
            )
        }
    }

    private fun markAsRead(id: String) {
        val currentState = _uiState.value
        if (currentState is NotificationsUiState.Success) {
            val updated = currentState.notifications.map {
                if (it.id == id) it.copy(isRead = true) else it
            }
            _uiState.value = currentState.copy(
                notifications = updated,
                unreadCount = updated.count { !it.isRead }
            )
        }
    }

    private fun markAllAsRead() {
        val currentState = _uiState.value
        if (currentState is NotificationsUiState.Success) {
            val updated = currentState.notifications.map { it.copy(isRead = true) }
            _uiState.value = currentState.copy(notifications = updated, unreadCount = 0)
            viewModelScope.launch {
                _effects.send(NotificationsUiEffect.ShowSnackbar("All marked as read"))
            }
        }
    }

    private fun toggleJobAlerts(enabled: Boolean) {
        val currentState = _uiState.value
        if (currentState is NotificationsUiState.Success) {
            _uiState.value = currentState.copy(jobAlertsEnabled = enabled)
            viewModelScope.launch {
                trackEventUseCase(TrackEventRequest(eventName = "notifications_toggle_job_alerts_$enabled"))
            }
        }
    }

    private fun toggleFollowUpReminders(enabled: Boolean) {
        val currentState = _uiState.value
        if (currentState is NotificationsUiState.Success) {
            _uiState.value = currentState.copy(followUpRemindersEnabled = enabled)
            viewModelScope.launch {
                trackEventUseCase(TrackEventRequest(eventName = "notifications_toggle_followup_$enabled"))
            }
        }
    }

    private fun delete(id: String) {
        val currentState = _uiState.value
        if (currentState is NotificationsUiState.Success) {
            val updated = currentState.notifications.filter { it.id != id }
            _uiState.value = currentState.copy(
                notifications = updated,
                unreadCount = updated.count { !it.isRead }
            )
        }
    }
}
