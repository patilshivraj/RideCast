package com.ridecast.presentation.weather

/** Maps WeatherAPI.com condition codes to a simple category for UI rendering. */
enum class WeatherCondition { SUNNY, PARTLY_CLOUDY, CLOUDY, RAIN, HEAVY_RAIN, STORM, SNOW, FOG, UNKNOWN }

fun Int.toWeatherCondition(): WeatherCondition = when (this) {
    1000 -> WeatherCondition.SUNNY
    1003 -> WeatherCondition.PARTLY_CLOUDY
    1006, 1009 -> WeatherCondition.CLOUDY
    1030, 1135, 1147 -> WeatherCondition.FOG
    1063, 1150, 1153, 1168, 1171, 1180, 1183, 1186, 1189, 1192, 1195, 1198, 1201,
    1240, 1243, 1246 -> WeatherCondition.RAIN
    1273, 1276 -> WeatherCondition.HEAVY_RAIN
    1087, 1279, 1282 -> WeatherCondition.STORM
    1066, 1069, 1072, 1114, 1117, 1204, 1207, 1210, 1213, 1216, 1219, 1222,
    1225, 1237, 1249, 1252, 1255, 1258, 1261, 1264 -> WeatherCondition.SNOW
    else -> WeatherCondition.UNKNOWN
}

fun WeatherCondition.toEmoji(): String = when (this) {
    WeatherCondition.SUNNY         -> "☀️"
    WeatherCondition.PARTLY_CLOUDY -> "🌤"
    WeatherCondition.CLOUDY        -> "☁️"
    WeatherCondition.FOG           -> "🌫"
    WeatherCondition.RAIN          -> "🌧"
    WeatherCondition.HEAVY_RAIN    -> "🌧"
    WeatherCondition.STORM         -> "⛈"
    WeatherCondition.SNOW          -> "❄️"
    WeatherCondition.UNKNOWN       -> "🌡"
}
