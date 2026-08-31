package com.ridecast.domain.model

import java.time.ZonedDateTime
import com.ridecast.domain.model.TravelMode

data class RideSummaryStats(
    val originName: String,
    val destinationName: String,
    val departureTime: ZonedDateTime,
    val arrivalTime: ZonedDateTime,
    val distanceKm: Double,
    val durationFormatted: String,
    val highestTempCelsius: Double,
    val lowestTempCelsius: Double,
    val averageTempCelsius: Double,
    val rainExposureKm: Long,
    val rainExposureHours: Double,
    val strongWindPoints: Int,
    val insights: List<String>,
    val travelMode: TravelMode,
)
