package com.ridecast.presentation.timeline

import com.ridecast.domain.model.RideWeather
import com.ridecast.presentation.weather.toEmoji
import com.ridecast.presentation.weather.toWeatherCondition
import java.time.format.DateTimeFormatter

private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

fun RideWeather.toTimelineItems(
    originName: String,
    destinationName: String,
): List<TimelineItem> {
    val total = weatherPoints.size
    return weatherPoints.mapIndexed { index, weatherPoint ->
        val distanceKm = (weatherPoint.routePoint.distanceFromStartMeters / 1000).toInt()
        val isOrigin = index == 0
        val isDestination = index == total - 1
        val locationLabel = when {
            isOrigin -> originName
            isDestination -> destinationName
            else -> "$distanceKm km"
        }
        val condition = weatherPoint.weather.conditionCode.toWeatherCondition()
        TimelineItem(
            index = index,
            timeFormatted = weatherPoint.routePoint.eta.format(TIME_FORMAT),
            locationLabel = locationLabel,
            distanceKm = distanceKm,
            condition = condition,
            conditionEmoji = condition.toEmoji(),
            conditionText = weatherPoint.weather.conditionText,
            temperatureCelsius = weatherPoint.weather.temperatureCelsius,
            feelsLikeCelsius = weatherPoint.weather.feelsLikeCelsius,
            rainProbabilityPercent = weatherPoint.weather.rainProbabilityPercent,
            rainAmountMm = weatherPoint.weather.rainAmountMm,
            windSpeedKph = weatherPoint.weather.windSpeedKph,
            windDirectionDegrees = weatherPoint.weather.windDirectionDegrees,
            humidityPercent = weatherPoint.weather.humidityPercent,
            visibilityKm = weatherPoint.weather.visibilityKm,
            isOrigin = isOrigin,
            isDestination = isDestination,
        )
    }
}
