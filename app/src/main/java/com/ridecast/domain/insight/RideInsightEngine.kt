package com.ridecast.domain.insight

import com.ridecast.domain.model.RideWeather

/**
 * Pluggable engine for generating human-readable ride insights.
 *
 * The default implementation uses deterministic rules based on weather thresholds.
 * Future implementations can call OpenAI, Gemini, Ollama, or Claude without
 * any changes to the UI or domain layer.
 */
interface RideInsightEngine {
    /**
     * Analyses the ride weather data and returns a list of insight strings
     * to display to the rider. Each string is a complete, actionable sentence.
     *
     * @param rideWeather The complete ride + weather data.
     * @param originName  Human-readable origin name (e.g. "Pune").
     * @param destinationName Human-readable destination name (e.g. "Goa").
     * @return A list of insight strings (may be empty if conditions are benign).
     */
    suspend fun generateInsights(
        rideWeather: RideWeather,
        originName: String,
        destinationName: String,
    ): List<String>
}
