package com.ridecast.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridecast.domain.model.AppSettings
import com.ridecast.domain.model.DarkMode
import com.ridecast.domain.model.MapDisplayType
import com.ridecast.domain.model.TemperatureUnit
import com.ridecast.domain.model.WindUnit
import com.ridecast.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setSamplingInterval(intervalKm: Int) {
        viewModelScope.launch { settingsRepository.updateSamplingInterval(intervalKm) }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch { settingsRepository.updateTemperatureUnit(unit) }
    }

    fun setWindUnit(unit: WindUnit) {
        viewModelScope.launch { settingsRepository.updateWindUnit(unit) }
    }

    fun setDarkMode(mode: DarkMode) {
        viewModelScope.launch { settingsRepository.updateDarkMode(mode) }
    }

    fun setMapType(type: MapDisplayType) {
        viewModelScope.launch { settingsRepository.updateMapType(type) }
    }
}
