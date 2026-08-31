package com.ridecast.domain.model

data class RideSummaryStats(
    val originName: String,
    val destinationName: String,
    val distanceKm: Double,
    val durationFormatted: String,
    val highestTempCelsius: Double,
    val lowestTempCelsius: Double,
    val averageTempCelsius: Double,
    val rainExposureKm: Long,
    val rainExposureHours: Double,
    val strongWindPoints: Int,
    val insights: List<String>,
)
