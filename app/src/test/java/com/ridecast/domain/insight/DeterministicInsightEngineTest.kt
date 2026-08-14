package com.ridecast.domain.insight

import com.ridecast.domain.model.RideWeather
import com.ridecast.domain.model.Route
import com.ridecast.domain.model.RoutePoint
import com.ridecast.domain.model.WeatherData
import com.ridecast.domain.model.WeatherPoint
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class DeterministicInsightEngineTest {

    private val engine = DeterministicInsightEngine()

    private fun makeWeather(
        conditionCode: Int = 1000,
        rainPercent: Int = 0,
        windKph: Double = 15.0,
        tempC: Double = 25.0,
        visibilityKm: Double = 10.0,
    ) = WeatherData(
        temperatureCelsius = tempC, feelsLikeCelsius = tempC - 2,
        conditionText = "Test", conditionCode = conditionCode,
        rainProbabilityPercent = rainPercent, rainAmountMm = 0.0,
        windSpeedKph = windKph, windDirectionDegrees = 90,
        humidityPercent = 60, visibilityKm = visibilityKm, uvIndex = 5.0,
    )

    private fun makePoint(distKm: Long, hourOffset: Long, weather: WeatherData): WeatherPoint {
        val eta = ZonedDateTime.of(2026, 6, 28, 9, 0, 0, 0, ZoneOffset.UTC).plusHours(hourOffset)
        return WeatherPoint(
            routePoint = RoutePoint(18.5, 73.8, distKm * 1000, eta),
            weather = weather,
        )
    }

    @Test
    fun `all sunny conditions produces positive insight`() = runBlocking {
        val rideWeather = RideWeather(
            route = Route(430_000L, 28_800L, "", emptyList()),
            weatherPoints = listOf(
                makePoint(0, 0, makeWeather(1000)),
                makePoint(50, 1, makeWeather(1000)),
                makePoint(100, 2, makeWeather(1003)),
            ),
        )
        val insights = engine.generateInsights(rideWeather, "Pune", "Goa")
        assertTrue(insights.any { it.contains("great", ignoreCase = true) || it.contains("✅") })
    }

    @Test
    fun `storm produces storm insight`() = runBlocking {
        val rideWeather = RideWeather(
            route = Route(430_000L, 28_800L, "", emptyList()),
            weatherPoints = listOf(
                makePoint(0, 0, makeWeather(1000)),
                makePoint(50, 1, makeWeather(1087)),  // thundery outbreaks
            ),
        )
        val insights = engine.generateInsights(rideWeather, "Pune", "Goa")
        assertTrue(insights.any { it.contains("Thunder") || it.contains("⛈") })
    }

    @Test
    fun `high temperature produces heat insight`() = runBlocking {
        val rideWeather = RideWeather(
            route = Route(430_000L, 28_800L, "", emptyList()),
            weatherPoints = listOf(makePoint(0, 0, makeWeather(1000, tempC = 38.0))),
        )
        val insights = engine.generateInsights(rideWeather, "Pune", "Goa")
        assertTrue(insights.any { it.contains("38") || it.contains("🌡") })
    }

    @Test
    fun `low visibility produces fog insight`() = runBlocking {
        val rideWeather = RideWeather(
            route = Route(430_000L, 28_800L, "", emptyList()),
            weatherPoints = listOf(makePoint(0, 0, makeWeather(1030, visibilityKm = 1.5))),
        )
        val insights = engine.generateInsights(rideWeather, "Pune", "Goa")
        assertTrue(insights.any { it.contains("visibility", ignoreCase = true) || it.contains("🌫") })
    }
}
