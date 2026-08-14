package com.ridecast.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val RideCastLightColorScheme = lightColorScheme(
    primary = RideCastOlive,
    onPrimary = Color.White,
    primaryContainer = RideCastOliveLight.copy(alpha = 0.25f),
    onPrimaryContainer = RideCastOliveDark,
    secondary = RideCastGold,
    onSecondary = RideCastCharcoal,
    secondaryContainer = RideCastGoldLight.copy(alpha = 0.35f),
    onSecondaryContainer = RideCastGoldDark,
    background = RideCastCream,
    onBackground = RideCastCharcoal,
    surface = RideCastSurfaceElevated,
    onSurface = RideCastCharcoal,
    onSurfaceVariant = RideCastCharcoalMuted,
    surfaceVariant = RideCastCream,
    outline = RideCastCharcoalMuted.copy(alpha = 0.35f),
    outlineVariant = RideCastCharcoalMuted.copy(alpha = 0.2f),
    error = WeatherExtreme,
    errorContainer = WeatherExtreme.copy(alpha = 0.12f),
    onErrorContainer = WeatherExtreme,
)

private val RideCastDarkColorScheme = darkColorScheme(
    primary = RideCastOliveLight,
    onPrimary = RideCastCharcoal,
    primaryContainer = RideCastOliveDark,
    onPrimaryContainer = RideCastOliveLight,
    secondary = RideCastGoldLight,
    onSecondary = RideCastCharcoal,
    secondaryContainer = RideCastGoldDark,
    onSecondaryContainer = RideCastGoldLight,
    background = Color(0xFF1C1B18),
    onBackground = Color(0xFFE8E4DC),
    surface = Color(0xFF262420),
    onSurface = Color(0xFFE8E4DC),
    onSurfaceVariant = Color(0xFFA8A49C),
    surfaceVariant = Color(0xFF2E2C28),
    outline = Color(0xFF5C5A56),
    outlineVariant = Color(0xFF3D3B38),
    error = Color(0xFFE07A6A),
    errorContainer = WeatherExtreme.copy(alpha = 0.2f),
    onErrorContainer = Color(0xFFF0B0A8),
)

/**
 * Root theme for all RideCast composables.
 *
 * Brand palette is applied by default. Material You dynamic colour can be enabled
 * via [dynamicColor] (Android 12+ only).
 */
@Composable
fun RideCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> RideCastDarkColorScheme
        else -> RideCastLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RideCastTypography,
        shapes = RideCastShapes,
        content = content,
    )
}
