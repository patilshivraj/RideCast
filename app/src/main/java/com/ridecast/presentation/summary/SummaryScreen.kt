package com.ridecast.presentation.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ridecast.core.util.Result
import com.ridecast.domain.model.RideSummaryStats
import com.ridecast.domain.model.RideWeather
import com.ridecast.presentation.components.RideCastTopBar
import com.ridecast.presentation.route.RouteUiModel
import com.ridecast.presentation.route.RouteViewModel
import com.ridecast.presentation.trip.TripPlannerViewModel
import com.ridecast.presentation.weather.WeatherViewModel
import kotlin.math.roundToInt

@Composable
fun SummaryScreen(
    modifier: Modifier = Modifier,
    routeViewModel: RouteViewModel,
    weatherViewModel: WeatherViewModel,
    summaryViewModel: SummaryViewModel = hiltViewModel(),
    tripPlannerViewModel: TripPlannerViewModel = hiltViewModel(),
) {
    val weatherState by weatherViewModel.weatherState.collectAsStateWithLifecycle()
    val routeState by routeViewModel.routeState.collectAsStateWithLifecycle()
    val summaryState by summaryViewModel.summaryState.collectAsStateWithLifecycle()
    val tripState by tripPlannerViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(weatherState, routeState) {
        if (weatherState is Result.Success && routeState is Result.Success) {
            val rideWeather = (weatherState as Result.Success<RideWeather>).data
            val route = (routeState as Result.Success<RouteUiModel>).data
            summaryViewModel.computeSummary(
                rideWeather = rideWeather,
                distanceKm = route.distanceKm,
                durationFormatted = route.durationFormatted,
                originName = tripState.selectedOrigin?.name ?: "Origin",
                destinationName = tripState.selectedDestination?.name ?: "Destination",
            )
        }
    }

    Scaffold(
        topBar = { RideCastTopBar(title = "Ride Summary") },
        modifier = modifier,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                weatherState !is Result.Success -> EmptySummaryContent()
                summaryState is Result.Loading  -> SummaryLoadingContent()
                summaryState is Result.Error    -> SummaryErrorContent(
                    message = (summaryState as Result.Error).message,
                )
                summaryState is Result.Success  -> {
                    val stats = (summaryState as Result.Success<RideSummaryStats>).data
                    val originName = tripState.selectedOrigin?.name ?: "Origin"
                    val destName = tripState.selectedDestination?.name ?: "Destination"
                    SummaryContent(stats = stats, originName = originName, destinationName = destName)
                }
            }
        }
    }
}

// ── State composables ──────────────────────────────────────────────────────────

@Composable
private fun EmptySummaryContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Speed,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Plan a ride to see the summary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter a route and fetch weather to unlock insights",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
    }
}

@Composable
private fun SummaryLoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Computing your ride summary…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SummaryErrorContent(message: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Summary unavailable",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message ?: "An unexpected error occurred.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
    }
}

// ── Success content ────────────────────────────────────────────────────────────

@Composable
private fun SummaryContent(
    stats: RideSummaryStats,
    originName: String,
    destinationName: String,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            TripOverviewCard(
                originName = originName,
                destinationName = destinationName,
                distanceKm = stats.distanceKm,
                durationFormatted = stats.durationFormatted,
            )
        }
        item {
            TemperatureCard(
                high = stats.highestTempCelsius,
                low = stats.lowestTempCelsius,
                avg = stats.averageTempCelsius,
            )
        }
        item {
            PrecipitationCard(
                rainKm = stats.rainExposureKm,
                rainHours = stats.rainExposureHours,
            )
        }
        item {
            WindCard(strongWindPoints = stats.strongWindPoints)
        }
        item {
            InsightsCard(insights = stats.insights)
        }
    }
}

// ── Cards ──────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun TripOverviewCard(
    originName: String,
    destinationName: String,
    distanceKm: Double,
    durationFormatted: String,
) {
    SummaryCard(title = "Trip Overview", icon = Icons.Outlined.DirectionsBike) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = originName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "→  $destinationName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatChip(label = "Distance", value = "${(distanceKm * 10).roundToInt() / 10.0} km")
            StatChip(label = "Duration", value = durationFormatted)
        }
    }
}

@Composable
private fun TemperatureCard(high: Double, low: Double, avg: Double) {
    SummaryCard(title = "Temperature", icon = Icons.Outlined.Thermostat) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatChip(label = "High", value = "${high.roundToInt()}°C")
            StatChip(label = "Avg", value = "${avg.roundToInt()}°C")
            StatChip(label = "Low", value = "${low.roundToInt()}°C")
        }
    }
}

@Composable
private fun PrecipitationCard(rainKm: Long, rainHours: Double) {
    SummaryCard(title = "Rain Exposure", icon = Icons.Outlined.WaterDrop) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatChip(label = "Distance in rain", value = "$rainKm km")
            val hoursInt = rainHours.toInt()
            val minutesInt = ((rainHours - hoursInt) * 60).roundToInt()
            val hoursLabel = if (hoursInt > 0) "${hoursInt}h ${minutesInt}m" else "${minutesInt}m"
            StatChip(label = "Time in rain", value = hoursLabel)
        }
    }
}

@Composable
private fun WindCard(strongWindPoints: Int) {
    SummaryCard(title = "Wind", icon = Icons.Outlined.Air) {
        if (strongWindPoints == 0) {
            Text(
                text = "No strong wind sections on this route.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = "$strongWindPoints waypoint(s) with strong winds (> 40 km/h). Ride with caution.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InsightsCard(insights: List<String>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ride Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            insights.forEachIndexed { index, insight ->
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (index < insights.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
