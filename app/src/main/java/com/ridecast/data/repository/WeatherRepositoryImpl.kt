package com.ridecast.data.repository

import com.ridecast.BuildConfig
import com.ridecast.core.util.Result
import com.ridecast.data.network.WeatherApiService
import com.ridecast.data.network.dto.HourDto
import com.ridecast.domain.model.WeatherData
import com.ridecast.domain.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
) : WeatherRepository {

    private val cache = ConcurrentHashMap<String, WeatherData>()

    override suspend fun getWeatherForPoint(
        latitude: Double,
        longitude: Double,
        epochSeconds: Long,
    ): Result<WeatherData> {
        val cacheKey = buildCacheKey(latitude, longitude, epochSeconds)
        cache[cacheKey]?.let { return Result.Success(it) }

        return try {
            val instant = Instant.ofEpochSecond(epochSeconds)
            val localDate = instant.atZone(ZoneId.of("UTC")).toLocalDate()
            val hour = instant.atZone(ZoneId.of("UTC")).hour

            val response = withContext(Dispatchers.IO) {
                weatherApiService.getForecast(
                    apiKey = BuildConfig.WEATHER_API_KEY,
                    location = "${String.format("%.4f", latitude)},${String.format("%.4f", longitude)}",
                    date = localDate.toString(),
                    hour = hour,
                )
            }

            val hourData = response.forecast?.forecastday?.firstOrNull()?.hour?.firstOrNull()
                ?: return Result.Error(Exception("No hour data"), "Weather data unavailable for this time.")

            val weatherData = hourData.toDomain()
            cache[cacheKey] = weatherData
            Result.Success(weatherData)
        } catch (e: Exception) {
            Timber.e(e, "Weather fetch failed for $latitude,$longitude @ $epochSeconds")
            Result.Error(e, e.localizedMessage ?: "Failed to fetch weather.")
        }
    }

    fun clearCache() = cache.clear()

    private fun buildCacheKey(lat: Double, lon: Double, epochSeconds: Long): String {
        val latRounded = String.format("%.3f", lat)
        val lonRounded = String.format("%.3f", lon)
        val epochHour = epochSeconds / 3600
        return "${latRounded}_${lonRounded}_${epochHour}"
    }
}

private fun HourDto.toDomain(): WeatherData = WeatherData(
    temperatureCelsius     = tempC ?: 0.0,
    feelsLikeCelsius       = feelsLikeC ?: 0.0,
    conditionText          = condition?.text ?: "Unknown",
    conditionCode          = condition?.code ?: 0,
    rainProbabilityPercent = chanceOfRain ?: 0,
    rainAmountMm           = precipMm ?: 0.0,
    windSpeedKph           = windKph ?: 0.0,
    windDirectionDegrees   = windDegree ?: 0,
    humidityPercent        = humidity ?: 0,
    visibilityKm           = visKm ?: 0.0,
    uvIndex                = uv ?: 0.0,
)
