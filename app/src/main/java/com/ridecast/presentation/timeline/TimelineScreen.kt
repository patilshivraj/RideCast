package com.ridecast.presentation.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ridecast.core.util.Result
import com.ridecast.domain.model.RideWeather
import com.ridecast.presentation.components.EmptyState
import com.ridecast.presentation.components.RideCastTopBar
import com.ridecast.presentation.components.ShimmerTimelineList
import com.ridecast.presentation.route.RouteViewModel
import com.ridecast.presentation.theme.RideCastSpacing
import com.ridecast.presentation.trip.TripPlannerViewModel
import com.ridecast.presentation.weather.WeatherViewModel

@Composable
fun TimelineScreen(
    modifier: Modifier = Modifier,
    routeViewModel: RouteViewModel,
    weatherViewModel: WeatherViewModel,
    tripPlannerViewModel: TripPlannerViewModel = hiltViewModel(),
) {
    val weatherState by weatherViewModel.weatherState.collectAsStateWithLifecycle()
    val routeState by routeViewModel.routeState.collectAsStateWithLifecycle()
    val tripState by tripPlannerViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { RideCastTopBar(title = "Weather Timeline") },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                routeState !is Result.Success -> {
                    EmptyState(
                        icon = Icons.Outlined.AccessTime,
                        title = "No ride planned",
                        message = "Plan a route first to see weather along your journey.",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                weatherState is Result.Loading -> {
                    ShimmerTimelineList(modifier = Modifier.fillMaxSize())
                }
                weatherState is Result.Error -> {
                    EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "Weather unavailable",
                        message = (weatherState as Result.Error).message ?: "Unable to fetch weather data.",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                weatherState is Result.Success -> {
                    val rideWeather = (weatherState as Result.Success<RideWeather>).data
                    val originName = tripState.selectedOrigin?.name ?: "Origin"
                    val destinationName = tripState.selectedDestination?.name ?: "Destination"
                    val timelineItems = remember(rideWeather, originName, destinationName) {
                        rideWeather.toTimelineItems(originName, destinationName)
                    }
                    TimelineList(items = timelineItems)
                }
            }
        }
    }
}

@Composable
private fun TimelineList(items: List<TimelineItem>) {
    val pullState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = false,
        onRefresh = { },
        state = pullState,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = RideCastSpacing.md,
                vertical = RideCastSpacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(RideCastSpacing.md),
        ) {
            items(items, key = { it.index }) { item ->
                AnimatedTimelineCard(item = item)
            }
        }
    }
}
