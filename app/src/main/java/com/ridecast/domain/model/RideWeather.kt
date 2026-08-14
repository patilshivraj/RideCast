package com.ridecast.domain.model

/**
 * The complete result of a ride weather calculation — the route paired with
 * a weather reading at every sampled waypoint.
 */
data class RideWeather(
    val route: Route,
    val weatherPoints: List<WeatherPoint>,
)

/**
 * A single weather-annotated waypoint: the geographic/temporal point and the
 * forecast for that exact location and time.
 */
data class WeatherPoint(
    val routePoint: RoutePoint,
    val weather: WeatherData,
)
