package com.ridecast.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Defines every top-level navigation destination in the app.
 *
 * Each screen carries its route string, display label, and two icon variants
 * (outlined for unselected, filled for selected) for the bottom navigation bar.
 *
 * String routes are used here for simplicity. Typed Navigation (with @Serializable)
 * will be adopted when screens require complex navigation arguments.
 */
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    data object TripPlanner : Screen(
        route        = "trip_planner",
        label        = "Plan",
        icon         = Icons.Outlined.Navigation,
        selectedIcon = Icons.Filled.Navigation,
    )

    data object Timeline : Screen(
        route        = "timeline",
        label        = "Timeline",
        icon         = Icons.Outlined.AccessTime,
        selectedIcon = Icons.Filled.AccessTime,
    )

    data object Map : Screen(
        route        = "map",
        label        = "Map",
        icon         = Icons.Outlined.Map,
        selectedIcon = Icons.Filled.Map,
    )

    data object Summary : Screen(
        route        = "summary",
        label        = "Summary",
        icon         = Icons.Outlined.Speed,
        selectedIcon = Icons.Filled.Speed,
    )

    data object Settings : Screen(
        route        = "settings",
        label        = "Settings",
        icon         = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
    )

    companion object {
        /** All tabs shown in the bottom navigation bar, in display order. */
        val bottomNavTabs: List<Screen> = listOf(
            TripPlanner,
            Timeline,
            Map,
            Summary,
            Settings,
        )
    }
}
