package com.ridecast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
 *
 * Responsibilities:
 * - Enable edge-to-edge rendering so Compose controls system bar insets.
 * - Apply [RideCastTheme] (Material You) to the entire composition.
 * - Host the bottom navigation bar and the [RideCastNavGraph].
 *
 * Navigation state is owned here so it survives configuration changes via the
 * NavController backed by the Activity's ViewModelStore.
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
                DarkMode.LIGHT  -> false
                DarkMode.DARK   -> true
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

    Scaffold(
        modifier  = Modifier.fillMaxSize(),
        bottomBar = {
            RideCastBottomBar(
                navController = navController,
                tabs          = tabs,
            )
        },
    ) { innerPadding ->
        RideCastNavGraph(
            navController = navController,
            modifier      = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun RideCastBottomBar(
    navController: NavHostController,
    tabs: List<Screen>,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        tabs.forEach { screen ->
            val isSelected = currentDestination
                ?.hierarchy
                ?.any { it.route == screen.route } == true

            NavigationBarItem(
                selected = isSelected,
                onClick  = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination to avoid building a large back stack.
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination on re-select.
                        launchSingleTop = true
                        // Restore state when reselecting a previously visited tab.
                        restoreState    = true
                    }
                },
                icon  = {
                    Icon(
                        imageVector        = if (isSelected) screen.selectedIcon else screen.icon,
                        contentDescription = screen.label,
                    )
                },
                label = { Text(text = screen.label) },
            )
        }
    }
}
