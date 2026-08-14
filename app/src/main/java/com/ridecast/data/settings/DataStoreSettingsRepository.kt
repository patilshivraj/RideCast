package com.ridecast.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ridecast.domain.model.AppSettings
import com.ridecast.domain.model.DarkMode
import com.ridecast.domain.model.MapDisplayType
import com.ridecast.domain.model.SamplingConfig
import com.ridecast.domain.model.TemperatureUnit
import com.ridecast.domain.model.WindUnit
import com.ridecast.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    companion object {
        val KEY_SAMPLING_INTERVAL = intPreferencesKey("sampling_interval")
        val KEY_TEMPERATURE_UNIT  = stringPreferencesKey("temperature_unit")
        val KEY_WIND_UNIT         = stringPreferencesKey("wind_unit")
        val KEY_DARK_MODE         = stringPreferencesKey("dark_mode")
        val KEY_MAP_TYPE          = stringPreferencesKey("map_type")
    }

    override val settings: Flow<AppSettings> = dataStore.data
        .catch { e ->
            Timber.e(e, "Error reading DataStore settings")
            emit(emptyPreferences())
        }
        .map { prefs ->
            AppSettings(
                samplingIntervalKm = prefs[KEY_SAMPLING_INTERVAL] ?: SamplingConfig.DEFAULT_INTERVAL_KM,
                temperatureUnit    = TemperatureUnit.entries.firstOrNull { it.name == prefs[KEY_TEMPERATURE_UNIT] } ?: TemperatureUnit.CELSIUS,
                windUnit           = WindUnit.entries.firstOrNull { it.name == prefs[KEY_WIND_UNIT] } ?: WindUnit.KMH,
                darkMode           = DarkMode.entries.firstOrNull { it.name == prefs[KEY_DARK_MODE] } ?: DarkMode.SYSTEM,
                mapType            = MapDisplayType.entries.firstOrNull { it.name == prefs[KEY_MAP_TYPE] } ?: MapDisplayType.NORMAL,
            )
        }

    override suspend fun updateSamplingInterval(intervalKm: Int) {
        dataStore.edit { it[KEY_SAMPLING_INTERVAL] = intervalKm }
    }

    override suspend fun updateTemperatureUnit(unit: TemperatureUnit) {
        dataStore.edit { it[KEY_TEMPERATURE_UNIT] = unit.name }
    }

    override suspend fun updateWindUnit(unit: WindUnit) {
        dataStore.edit { it[KEY_WIND_UNIT] = unit.name }
    }

    override suspend fun updateDarkMode(mode: DarkMode) {
        dataStore.edit { it[KEY_DARK_MODE] = mode.name }
    }

    override suspend fun updateMapType(type: MapDisplayType) {
        dataStore.edit { it[KEY_MAP_TYPE] = type.name }
    }
}
