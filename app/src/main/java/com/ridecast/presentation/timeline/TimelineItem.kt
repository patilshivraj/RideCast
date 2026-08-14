package com.ridecast.presentation.timeline

import com.ridecast.presentation.weather.WeatherCondition

data class TimelineItem(
    val index: Int,
    val timeFormatted: String,
    val locationLabel: String,
    val distanceKm: Int,
    val condition: WeatherCondition,
    val conditionEmoji: String,
    val conditionText: String,
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val rainProbabilityPercent: Int,
    val rainAmountMm: Double,
    val windSpeedKph: Double,
    val windDirectionDegrees: Int,
    val humidityPercent: Int,
    val visibilityKm: Double,
    val isOrigin: Boolean,
    val isDestination: Boolean,
)
