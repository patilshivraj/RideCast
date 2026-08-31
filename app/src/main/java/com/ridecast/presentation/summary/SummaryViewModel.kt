package com.ridecast.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridecast.core.util.Result
import com.ridecast.domain.insight.RideInsightEngine
import com.ridecast.domain.model.RideSummaryStats
import com.ridecast.domain.model.RideWeather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val insightEngine: RideInsightEngine,
) : ViewModel() {

    private val _summaryState = MutableStateFlow<Result<RideSummaryStats>>(Result.Loading)
    val summaryState: StateFlow<Result<RideSummaryStats>> = _summaryState.asStateFlow()

    fun computeSummary(
        rideWeather: RideWeather,
        distanceKm: Double,
        durationFormatted: String,
        originName: String,
        destinationName: String,
        departureTime: java.time.ZonedDateTime,
        durationSeconds: Long,
        travelMode: com.ridecast.domain.model.TravelMode,
    ) {
        viewModelScope.launch {
            _summaryState.value = Result.Loading
            try {
                val points = rideWeather.weatherPoints
                val temps = points.map { it.weather.temperatureCelsius }
                val rainPoints = points.filter { it.weather.rainProbabilityPercent > 50 }
                val rainKm = rainPoints.sumOf { it.routePoint.distanceFromStartMeters } / 1000
                val avgIntervalHours = if (points.size > 1) {
                    val totalSeconds = java.time.Duration.between(
                        points.first().routePoint.eta, points.last().routePoint.eta
                    ).seconds
                    totalSeconds.toDouble() / 3600.0 / (points.size - 1)
                } else 0.0
                val rainHours = rainPoints.size * avgIntervalHours

                val insights = insightEngine.generateInsights(rideWeather, originName, destinationName)

                _summaryState.value = Result.Success(
                    RideSummaryStats(
                        originName = originName,
                        destinationName = destinationName,
                        departureTime = departureTime,
                        arrivalTime = departureTime.plusSeconds(durationSeconds),
                        distanceKm = distanceKm,
                        durationFormatted = durationFormatted,
                        highestTempCelsius = temps.maxOrNull() ?: 0.0,
                        lowestTempCelsius = temps.minOrNull() ?: 0.0,
                        averageTempCelsius = if (temps.isEmpty()) 0.0 else temps.average(),
                        rainExposureKm = rainKm,
                        rainExposureHours = rainHours,
                        strongWindPoints = points.count { it.weather.windSpeedKph > 40 },
                        insights = insights,
                        travelMode = travelMode,
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Summary computation failed")
                _summaryState.value = Result.Error(e, e.localizedMessage)
            }
        }
    }

    fun clearSummary() {
        _summaryState.value = Result.Loading
    }
}
