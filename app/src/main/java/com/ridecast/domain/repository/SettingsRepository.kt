package com.ridecast.domain.repository

import com.ridecast.domain.model.AppSettings
import com.ridecast.domain.model.DarkMode
import com.ridecast.domain.model.MapDisplayType
import com.ridecast.domain.model.TemperatureUnit
import com.ridecast.domain.model.WindUnit
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun updateSamplingInterval(intervalKm: Int)
    suspend fun updateTemperatureUnit(unit: TemperatureUnit)
    suspend fun updateWindUnit(unit: WindUnit)
    suspend fun updateDarkMode(mode: DarkMode)
    suspend fun updateMapType(type: MapDisplayType)
}
