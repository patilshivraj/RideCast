package com.ridecast.domain.model

data class AppSettings(
    val samplingIntervalKm: Int = SamplingConfig.DEFAULT_INTERVAL_KM,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windUnit: WindUnit = WindUnit.KMH,
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val mapType: MapDisplayType = MapDisplayType.NORMAL,
)

enum class TemperatureUnit(val label: String) {
    CELSIUS("°C"), FAHRENHEIT("°F")
}

enum class WindUnit(val label: String) {
    KMH("km/h"), MPH("mph"), MS("m/s"), KNOTS("kn")
}

enum class DarkMode(val label: String) {
    SYSTEM("Follow System"), LIGHT("Light"), DARK("Dark")
}

enum class MapDisplayType(val label: String) {
    NORMAL("Normal"), SATELLITE("Satellite"), TERRAIN("Terrain"), HYBRID("Hybrid")
}
