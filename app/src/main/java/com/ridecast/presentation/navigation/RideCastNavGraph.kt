package com.ridecast.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ridecast.core.util.Result
import com.ridecast.domain.model.SamplingConfig
import com.ridecast.presentation.map.MapScreen
import com.ridecast.presentation.route.RouteViewModel
import com.ridecast.presentation.settings.SettingsScreen
import com.ridecast.presentation.settings.SettingsViewModel
import com.ridecast.presentation.summary.SummaryScreen
import com.ridecast.presentation.summary.SummaryViewModel
import com.ridecast.presentation.timeline.TimelineScreen
import com.ridecast.presentation.trip.TripPlannerScreen
import com.ridecast.presentation.weather.WeatherViewModel

/**
 * Central navigation graph for RideCast.
 *
 * [RouteViewModel] and [WeatherViewModel] are instantiated once at the NavHost level so they are
 * shared between screens: [TripPlannerScreen] triggers route calculation and weather fetch, while
 * [MapScreen] and [TimelineScreen] consume the results.
 *
 * [SettingsViewModel] is also hoisted here so that sampling interval changes from settings are
 * applied to the route immediately via [RouteViewModel.updateSamplingConfig].
 *
 * @param navController The [NavHostController] owned by the host Activity.
 * @param modifier      Applied to the [NavHost] container — typically provides
 *                      bottom-bar padding from the outer [Scaffold].
 */
@Composable
fun RideCastNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val routeViewModel: RouteViewModel = hiltViewModel()
    val weatherViewModel: WeatherViewModel = hiltViewModel()
    val summaryViewModel: SummaryViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val settingsState by settingsViewModel.settings.collectAsStateWithLifecycle()
    val routeState by routeViewModel.routeState.collectAsStateWithLifecycle()

    LaunchedEffect(settingsState.samplingIntervalKm) {
        routeViewModel.updateSamplingConfig(SamplingConfig(settingsState.samplingIntervalKm))
    }

    // Trigger weather fetch as soon as route calculation succeeds, regardless of which screen
    // is currently visible. Placed here so it survives navigation away from TripPlannerScreen.
    LaunchedEffect(routeState) {
        if (routeState is Result.Success) {
            val success = routeState as Result.Success
            weatherViewModel.fetchWeather(success.data.route, success.data.samplePoints)
        }
    }

    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 }
    }
    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 4 }
    }
    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 }
    }
    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 4 }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.TripPlanner.route,
        modifier = modifier,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
    ) {
        composable(Screen.TripPlanner.route) {
            TripPlannerScreen(
                routeViewModel = routeViewModel,
                weatherViewModel = weatherViewModel,
            )
        }

        composable(Screen.Timeline.route) {
            TimelineScreen(routeViewModel = routeViewModel, weatherViewModel = weatherViewModel)
        }

        composable(Screen.Map.route) {
            MapScreen(routeViewModel = routeViewModel, weatherViewModel = weatherViewModel)
        }

        composable(Screen.Summary.route) {
            SummaryScreen(
                routeViewModel = routeViewModel,
                weatherViewModel = weatherViewModel,
                summaryViewModel = summaryViewModel,
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
