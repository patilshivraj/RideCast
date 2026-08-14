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

// ─── Static fallback colour schemes (pre-Android 12) ──────────────────────────

private val RideCastLightColorScheme = lightColorScheme(
    primary          = RideCastBlue,
    onPrimary        = Color.White,
    primaryContainer = RideCastBlueLight,
    onPrimaryContainer = RideCastBlueDark,
    secondary        = RideCastOrange,
    onSecondary      = Color.White,
    secondaryContainer = RideCastOrangeLight,
    onSecondaryContainer = RideCastOrangeDark,
)

private val RideCastDarkColorScheme = darkColorScheme(
    primary          = RideCastBlueLight,
    onPrimary        = RideCastBlueDark,
    primaryContainer = RideCastBlue,
    onPrimaryContainer = Color.White,
    secondary        = RideCastOrangeLight,
    onSecondary      = RideCastOrangeDark,
    secondaryContainer = RideCastOrange,
    onSecondaryContainer = Color.White,
)

/**
 * Root theme for all RideCast composables.
 *
 * On Android 12+ (API 31/S), Material You dynamic colour is applied using the device
 * wallpaper as the seed. On older devices, the static brand palette defined above is used.
 *
 * @param darkTheme     Whether to apply the dark colour scheme. Defaults to system setting.
 * @param dynamicColor  Whether to enable Material You wallpaper-based colour extraction
 *                      (Android 12+ only). Can be toggled from the Settings screen.
 */
@Composable
fun RideCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> RideCastDarkColorScheme
        else      -> RideCastLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = RideCastTypography,
        content     = content,
    )
}
