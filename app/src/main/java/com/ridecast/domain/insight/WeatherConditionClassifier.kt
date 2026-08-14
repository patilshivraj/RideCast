package com.ridecast.domain.insight

/** Domain-layer weather classification — mirrors presentation/weather/WeatherCondition without coupling. */
internal fun isRainy(conditionCode: Int): Boolean = conditionCode in setOf(
    1063, 1150, 1153, 1168, 1171, 1180, 1183, 1186, 1189, 1192, 1195, 1198, 1201,
    1240, 1243, 1246, 1273, 1276,
)

internal fun isStorm(conditionCode: Int): Boolean = conditionCode in setOf(1087, 1273, 1276, 1279, 1282)

internal fun isGoodWeather(conditionCode: Int): Boolean = conditionCode in setOf(1000, 1003)

internal fun isLowVisibility(visibilityKm: Double): Boolean = visibilityKm < 3.0

internal fun isHeavyRainOrStorm(conditionCode: Int): Boolean = conditionCode in setOf(
    1273, 1276, 1087, 1279, 1282,
)
