package com.ridecast.domain.model

/**
 * Provider-agnostic weather snapshot for a single location and time.
 *
 * All weather repositories ([com.ridecast.domain.repository.WeatherRepository]) must map
 * their provider-specific responses into this model. OpenWeather, WeatherAPI.com, and
 * Tomorrow.io can all be adapted without changing the domain or presentation layers.
 */
data class WeatherData(
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    /** Short human-readable description, e.g. "Partly Cloudy" or "Heavy Rain". */
    val conditionText: String,
    /**
     * Provider-specific condition code mapped to a [WeatherCondition] in the presentation layer.
     * Kept as an integer here so the domain remains provider-agnostic.
     */
    val conditionCode: Int,
    /** 0–100 probability of precipitation. */
    val rainProbabilityPercent: Int,
    /** Expected rainfall in millimetres. */
    val rainAmountMm: Double,
    val windSpeedKph: Double,
    /** Meteorological wind direction: 0 = North, 90 = East, 180 = South, 270 = West. */
    val windDirectionDegrees: Int,
    val humidityPercent: Int,
    val visibilityKm: Double,
    /** UV index on the standard 0–11+ scale. */
    val uvIndex: Double,
)
