package com.ridecast.domain.repository

import com.ridecast.core.util.Result
import com.ridecast.domain.model.WeatherData

/**
 * Contract for weather data providers.
 *
 * The default implementation targets WeatherAPI.com. The interface is designed so that
 * OpenWeatherMap, Tomorrow.io, or any other provider can be substituted via Hilt bindings
 * in [com.ridecast.di.AppModule] without touching the domain or presentation layers.
 */
interface WeatherRepository {

    /**
     * Fetches the weather forecast for a specific [latitude]/[longitude] at the given
     * [epochSeconds] timestamp.
     *
     * Implementations should cache responses to avoid redundant network calls when
     * nearby route points share a forecast hour.
     *
     * @param latitude     WGS-84 latitude of the waypoint.
     * @param longitude    WGS-84 longitude of the waypoint.
     * @param epochSeconds Unix epoch timestamp (seconds) of the expected arrival.
     */
    suspend fun getWeatherForPoint(
        latitude: Double,
        longitude: Double,
        epochSeconds: Long,
    ): Result<WeatherData>
}
