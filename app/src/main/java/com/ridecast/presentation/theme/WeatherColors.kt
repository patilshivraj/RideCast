package com.ridecast.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ridecast.presentation.weather.WeatherCondition

/** Maps [WeatherCondition] to semantic palette colours for UI accents. */
fun WeatherCondition.toSemanticColor(): Color = when (this) {
    WeatherCondition.SUNNY, WeatherCondition.PARTLY_CLOUDY -> WeatherClear
    WeatherCondition.CLOUDY -> WeatherCloudy
    WeatherCondition.FOG -> WeatherVisibility
    WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> WeatherRain
    WeatherCondition.STORM -> WeatherExtreme
    WeatherCondition.SNOW -> WeatherSnow
    WeatherCondition.UNKNOWN -> WeatherVisibility
}

@Composable
fun WeatherCondition.toAccentContainerColor(): Color =
    toSemanticColor().copy(alpha = 0.12f)
