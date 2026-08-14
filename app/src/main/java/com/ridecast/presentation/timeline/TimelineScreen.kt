package com.ridecast.presentation.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ridecast.core.util.Result
import com.ridecast.domain.model.RideWeather
import com.ridecast.presentation.components.RideCastTopBar
import com.ridecast.presentation.components.ShimmerTimelineList
import com.ridecast.presentation.route.RouteViewModel
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
        modifier = modifier,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                routeState !is Result.Success -> {
                    EmptyTimelineContent()
                }
                weatherState is Result.Loading -> {
                    TimelineLoadingContent()
                }
                weatherState is Result.Error -> {
                    TimelineErrorContent((weatherState as Result.Error).message)
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
        onRefresh = { /* weatherViewModel.refresh() — stub */ },
        state = pullState,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.index }) { item ->
                AnimatedTimelineCard(item = item)
            }
        }
    }
}

@Composable
private fun EmptyTimelineContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Ride Planned",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Plan a ride first to see the weather timeline.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TimelineLoadingContent() {
    ShimmerTimelineList(modifier = Modifier.fillMaxSize())
}

@Composable
private fun TimelineErrorContent(message: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Weather Unavailable",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message ?: "Unable to fetch weather data.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
