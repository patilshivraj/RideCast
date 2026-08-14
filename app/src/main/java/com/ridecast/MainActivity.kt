package com.ridecast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ridecast.domain.model.DarkMode
import com.ridecast.presentation.navigation.RideCastNavGraph
import com.ridecast.presentation.navigation.Screen
import com.ridecast.presentation.settings.SettingsViewModel
import com.ridecast.presentation.theme.RideCastTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-Activity host for the entire RideCast app.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val darkTheme = when (settings.darkMode) {
                DarkMode.LIGHT -> false
                DarkMode.DARK -> true
                DarkMode.SYSTEM -> isSystemInDarkTheme()
            }
            RideCastTheme(darkTheme = darkTheme) {
                RideCastApp()
            }
        }
    }
}

@Composable
private fun RideCastApp() {
    val navController: NavHostController = rememberNavController()
    val tabs = remember { Screen.bottomNavTabs }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isImmersiveTab = navBackStackEntry?.destination?.route in immersiveTabRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            RideCastBottomBar(
                navController = navController,
                tabs = tabs,
            )
        },
    ) { innerPadding ->
        RideCastNavGraph(
            navController = navController,
            modifier = if (isImmersiveTab) {
                Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            } else {
                Modifier.padding(innerPadding)
            },
        )
    }
}

// Route strings only — do not reference Screen.* here; MainActivityKt <clinit> would
// initialize before Screen data objects and leave nulls in Screen.bottomNavTabs.
private val immersiveTabRoutes = setOf(
    "trip_planner",
    "map",
)

@Composable
private fun RideCastBottomBar(
    navController: NavHostController,
    tabs: List<Screen>,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets,
    ) {
        tabs.forEach { screen ->
            val isSelected = currentDestination
                ?.hierarchy
                ?.any { it.route == screen.route } == true

            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1f,
                animationSpec = tween(200),
                label = "nav_icon_scale",
            )
            val labelColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(200),
                label = "nav_label_color",
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.icon,
                        contentDescription = screen.label,
                        modifier = Modifier.scale(iconScale),
                    )
                },
                label = {
                    Text(
                        text = screen.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                ),
            )
        }
    }
}
