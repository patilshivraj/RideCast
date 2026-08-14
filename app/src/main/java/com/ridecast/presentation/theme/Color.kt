package com.ridecast.presentation.theme

import androidx.compose.ui.graphics.Color

// ─── Brand palette ────────────────────────────────────────────────────────────
// Warm, adventure-oriented — olive primary, gold accent, cream backgrounds.

val RideCastOlive = Color(0xFF5C6B4A)
val RideCastOliveLight = Color(0xFF7A8B66)
/** Brand sheet forest green — launcher icon dark variant, primary dark accent */
val RideCastOliveDark = Color(0xFF2F4530)

val RideCastGold = Color(0xFFC4A035)
val RideCastGoldLight = Color(0xFFD4B85A)
val RideCastGoldDark = Color(0xFF9A7B28)

/** Brand sheet cream — launcher icon light background */
val RideCastCream = Color(0xFFF2F1E6)
val RideCastSurfaceElevated = Color(0xFFFFFBF7)
val RideCastCharcoal = Color(0xFF2D2D2A)
val RideCastCharcoalMuted = Color(0xFF5C5A56)

// ─── Semantic weather colours ─────────────────────────────────────────────────
// Used for timeline accents, map markers, and condition indicators.

val WeatherClear = Color(0xFF6B9B6E)
val WeatherRain = Color(0xFF4A7BA7)
val WeatherWind = Color(0xFF4A9B9B)
val WeatherVisibility = Color(0xFF8A8A85)
val WeatherExtreme = Color(0xFFC45C4A)

// Legacy aliases kept for MarkerColors and existing references
val WeatherSunny = WeatherClear
val WeatherCloudy = Color(0xFFB8A88A)
val WeatherStorm = WeatherExtreme
val WeatherSnow = Color(0xFF90CAF9)
val WeatherFog = WeatherVisibility
val WeatherWindLegacy = WeatherWind

// ─── Ride condition indicators (summary dashboard) ────────────────────────────

val RideConditionGood = WeatherClear
val RideConditionCaution = RideCastGold
val RideConditionPoor = WeatherExtreme
