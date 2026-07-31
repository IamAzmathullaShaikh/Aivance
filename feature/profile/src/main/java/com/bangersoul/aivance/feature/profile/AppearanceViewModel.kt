package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.datastore.ThemeConfig
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppearanceUiState(
    val themeConfig: ThemeConfig = ThemeConfig.FOLLOW_SYSTEM,
    val accentSeed: String = "INDIGO",
    val dynamicColor: Boolean = true
)

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<AppearanceUiState> = userPreferencesRepository.userPreferences
        .map {
            AppearanceUiState(
                themeConfig = it.themeConfig,
                accentSeed = it.accentSeed,
                dynamicColor = it.dynamicColor
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppearanceUiState()
        )

    fun setThemeConfig(config: ThemeConfig) {
        viewModelScope.launch {
            userPreferencesRepository.updateThemeConfig(config)
        }
    }

    fun setAccentSeed(seed: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateAccentSeed(seed)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateDynamicColor(enabled)
        }
    }
}
