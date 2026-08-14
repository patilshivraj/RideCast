package com.ridecast.domain.usecase

import com.ridecast.core.util.Result
import com.ridecast.domain.model.RideWeather
import com.ridecast.domain.model.Route
import com.ridecast.domain.model.RoutePoint
import com.ridecast.domain.model.WeatherPoint
import com.ridecast.domain.repository.WeatherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class GetRideWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    /**
     * Fetches weather for every [RoutePoint] in [samplePoints] in parallel.
     * Emits [Result.Loading] immediately, then [Result.Success<RideWeather>] or [Result.Error].
     */
    operator fun invoke(
        route: Route,
        samplePoints: List<RoutePoint>,
    ): Flow<Result<RideWeather>> = flow {
        emit(Result.Loading)
        try {
            val weatherPoints = coroutineScope {
                samplePoints.map { point ->
                    async {
                        val epochSeconds = point.eta.toEpochSecond()
                        when (val result = weatherRepository.getWeatherForPoint(
                            latitude = point.latitude,
                            longitude = point.longitude,
                            epochSeconds = epochSeconds,
                        )) {
                            is Result.Success -> WeatherPoint(point, result.data)
                            is Result.Error -> {
                                Timber.w("Weather failed for point at ${point.distanceFromStartMeters / 1000}km: ${result.message}")
                                null
                            }
                            is Result.Loading -> null
                        }
                    }
                }.awaitAll().filterNotNull()
            }
            emit(Result.Success(RideWeather(route = route, weatherPoints = weatherPoints)))
        } catch (e: Exception) {
            Timber.e(e, "GetRideWeatherUseCase failed")
            emit(Result.Error(e, e.localizedMessage ?: "Failed to fetch ride weather."))
        }
    }
}
