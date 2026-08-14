package com.ridecast.domain.insight

import com.ridecast.domain.model.RideWeather
import com.ridecast.domain.model.WeatherPoint
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.roundToInt

class DeterministicInsightEngine @Inject constructor() : RideInsightEngine {

    override suspend fun generateInsights(
        rideWeather: RideWeather,
        originName: String,
        destinationName: String,
    ): List<String> {
        val insights = mutableListOf<String>()
        val points = rideWeather.weatherPoints

        if (points.isEmpty()) {
            return listOf("✅ No weather data available for this route.")
        }

        // 1. Rain start — first point where rainProbabilityPercent > 50
        val firstRainPoint = points.firstOrNull { it.weather.rainProbabilityPercent > 50 }
        if (firstRainPoint != null) {
            val hoursIn = durationHours(points.first().routePoint.eta, firstRainPoint.routePoint.eta)
            val km = firstRainPoint.routePoint.distanceFromStartMeters / 1000
            insights += "🌧 Rain likely approximately ${formatHours(hoursIn)} into your ride (~$km km from $originName). Consider rain gear before leaving."
        }

        // 2. Thunderstorm warning
        val stormPoints = points.filter { isStorm(it.weather.conditionCode) }
        if (stormPoints.isNotEmpty()) {
            val time = stormPoints.first().routePoint.eta.format(DateTimeFormatter.ofPattern("h:mm a"))
            insights += "⛈ Thunderstorms expected around $time. Strongly consider delaying departure or finding shelter."
        }

        // 3. Peak temperature
        val hottest = points.maxByOrNull { it.weather.temperatureCelsius }
        if (hottest != null && hottest.weather.temperatureCelsius >= 35) {
            val time = hottest.routePoint.eta.format(DateTimeFormatter.ofPattern("h:mm a"))
            insights += "🌡️ Peak temperature of ${hottest.weather.temperatureCelsius.roundToInt()}°C expected around $time. Stay hydrated."
        }

        // 4. Strong crosswind (> 40 km/h)
        val windyPoints = points.filter { it.weather.windSpeedKph > 40 }
        if (windyPoints.isNotEmpty()) {
            val km = windyPoints.first().routePoint.distanceFromStartMeters / 1000
            insights += "💨 Strong winds (${windyPoints.first().weather.windSpeedKph.roundToInt()} km/h) expected after $km km. Ride cautiously."
        }

        // 5. Best riding window — first consecutive stretch of ≥ 2 SUNNY/PARTLY_CLOUDY points
        val goodStretch = findBestWindow(points)
        if (goodStretch != null) {
            val startTime = goodStretch.first().routePoint.eta.format(DateTimeFormatter.ofPattern("h:mm a"))
            val endTime = goodStretch.last().routePoint.eta.format(DateTimeFormatter.ofPattern("h:mm a"))
            insights += "☀️ Best riding window: $startTime–$endTime. Clear skies and comfortable temperatures."
        }

        // 6. Heavy rain exposure
        val heavyRainPoints = points.filter { isHeavyRainOrStorm(it.weather.conditionCode) }
        if (heavyRainPoints.size >= 2) {
            insights += "🌊 Heavy rain or storms expected at ${heavyRainPoints.size} waypoints. Full waterproof gear recommended."
        }

        // 7. Low visibility warning (< 3 km)
        val fogPoints = points.filter { isLowVisibility(it.weather.visibilityKm) }
        if (fogPoints.isNotEmpty()) {
            insights += "🌫 Low visibility (< 3 km) at ${fogPoints.size} point(s). Use headlights and reduce speed."
        }

        // 8. Comfortable ride (no bad weather — positive insight)
        if (insights.isEmpty()) {
            insights += "✅ Conditions look great for your ride! Enjoy the journey from $originName to $destinationName."
        }

        return insights
    }

    private fun findBestWindow(points: List<WeatherPoint>): List<WeatherPoint>? {
        var best: List<WeatherPoint>? = null
        var current = mutableListOf<WeatherPoint>()
        for (point in points) {
            if (isGoodWeather(point.weather.conditionCode)) {
                current.add(point)
                if (current.size > (best?.size ?: 0)) best = current.toList()
            } else {
                current.clear()
            }
        }
        return if ((best?.size ?: 0) >= 2) best else null
    }

    private fun durationHours(from: ZonedDateTime, to: ZonedDateTime): Double {
        val seconds = java.time.Duration.between(from, to).seconds
        return seconds / 3600.0
    }

    private fun formatHours(hours: Double): String {
        val h = hours.toInt()
        val m = ((hours - h) * 60).toInt()
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}
