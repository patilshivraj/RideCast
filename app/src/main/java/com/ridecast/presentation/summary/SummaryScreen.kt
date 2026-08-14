package com.ridecast.presentation.summary

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ridecast.core.util.Result
import com.ridecast.domain.model.RideSummaryStats
import com.ridecast.domain.model.RideWeather
import com.ridecast.presentation.components.EmptyState
import com.ridecast.presentation.components.LoadingState
import com.ridecast.presentation.components.RideCastSurfaceCard
import com.ridecast.presentation.components.RideCastTopBar
import com.ridecast.presentation.components.RideMetric
import com.ridecast.presentation.components.SectionHeader
import com.ridecast.presentation.route.RouteUiModel
import com.ridecast.presentation.route.RouteViewModel
import com.ridecast.presentation.theme.RideCastSpacing
import com.ridecast.presentation.theme.RideCastType
import com.ridecast.presentation.theme.RideConditionCaution
import com.ridecast.presentation.theme.RideConditionGood
import com.ridecast.presentation.theme.RideConditionPoor
import com.ridecast.presentation.trip.TripPlannerViewModel
import com.ridecast.presentation.weather.WeatherViewModel
import kotlin.math.roundToInt

private enum class RideCondition(val label: String, val color: Color) {
    GOOD("Good to ride", RideConditionGood),
    CAUTION("Ride with caution", RideConditionCaution),
    POOR("Consider delaying", RideConditionPoor),
}

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
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                weatherState !is Result.Success -> {
                    EmptyState(
                        icon = Icons.Outlined.Speed,
                        title = "No ride summary yet",
                        message = "Plan a route and fetch weather to see ride intelligence.",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                summaryState is Result.Loading -> {
                    LoadingState(
                        message = "Computing your ride summary…",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                summaryState is Result.Error -> {
                    EmptyState(
                        icon = Icons.Outlined.Speed,
                        title = "Summary unavailable",
                        message = (summaryState as Result.Error).message ?: "An unexpected error occurred.",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                summaryState is Result.Success -> {
                    val stats = (summaryState as Result.Success<RideSummaryStats>).data
                    val originName = tripState.selectedOrigin?.name ?: "Origin"
                    val destName = tripState.selectedDestination?.name ?: "Destination"
                    SummaryContent(
                        stats = stats,
                        originName = originName,
                        destinationName = destName,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryContent(
    stats: RideSummaryStats,
    originName: String,
    destinationName: String,
) {
    val rideCondition = deriveRideCondition(stats.insights)

    LazyColumn(
        contentPadding = PaddingValues(RideCastSpacing.md),
        verticalArrangement = Arrangement.spacedBy(RideCastSpacing.md),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            RideConditionBanner(condition = rideCondition)
        }
        item {
            TripOverviewCard(
                originName = originName,
                destinationName = destinationName,
                distanceKm = stats.distanceKm,
                durationFormatted = stats.durationFormatted,
            )
        }
        item {
            MetricsRow(
                high = stats.highestTempCelsius,
                low = stats.lowestTempCelsius,
                avg = stats.averageTempCelsius,
                rainKm = stats.rainExposureKm,
                strongWindPoints = stats.strongWindPoints,
            )
        }
        item {
            InsightsSection(insights = stats.insights)
        }
    }
}

@Composable
private fun RideConditionBanner(condition: RideCondition) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = condition.color.copy(alpha = 0.15f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RideCastSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(condition.color, MaterialTheme.shapes.small),
            )
            Spacer(Modifier.width(RideCastSpacing.sm))
            Text(
                text = condition.label,
                style = RideCastType.metricPrimary,
                color = condition.color,
                fontWeight = FontWeight.SemiBold,
            )
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
    RideCastSurfaceCard {
        SectionHeader(title = "Trip overview")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.DirectionsBike,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(RideCastSpacing.sm))
            Column {
                Text(text = originName, style = RideCastType.cardTitle)
                Text(
                    text = "→ $destinationName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(RideCastSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            RideMetric(
                label = "Distance",
                value = "${(distanceKm * 10).roundToInt() / 10.0} km",
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            RideMetric(
                label = "Duration",
                value = durationFormatted,
                horizontalAlignment = Alignment.CenterHorizontally,
            )
        }
    }
}

@Composable
private fun MetricsRow(
    high: Double,
    low: Double,
    avg: Double,
    rainKm: Long,
    strongWindPoints: Int,
) {
    RideCastSurfaceCard {
        SectionHeader(title = "Conditions along route")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            RideMetric(
                label = "High",
                value = "${high.roundToInt()}°",
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            RideMetric(
                label = "Avg",
                value = "${avg.roundToInt()}°",
                emphasized = true,
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            RideMetric(
                label = "Low",
                value = "${low.roundToInt()}°",
                horizontalAlignment = Alignment.CenterHorizontally,
            )
        }
        Spacer(Modifier.height(RideCastSpacing.md))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(RideCastSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.height(RideCastSpacing.xs))
                RideMetric(
                    label = "Rain distance",
                    value = "$rainKm km",
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.Air,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.height(RideCastSpacing.xs))
                RideMetric(
                    label = "Strong wind points",
                    value = "$strongWindPoints",
                    emphasized = strongWindPoints > 0,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
            }
        }
    }
}

@Composable
private fun InsightsSection(insights: List<String>) {
    RideCastSurfaceCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(RideCastSpacing.sm))
            Text(
                text = "Ride insights",
                style = RideCastType.cardTitle,
            )
        }
        Spacer(Modifier.height(RideCastSpacing.md))
        insights.forEachIndexed { index, insight ->
            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            if (index < insights.lastIndex) {
                Spacer(Modifier.height(RideCastSpacing.sm))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Spacer(Modifier.height(RideCastSpacing.sm))
            }
        }
    }
}

private fun deriveRideCondition(insights: List<String>): RideCondition {
    val combined = insights.joinToString(" ").lowercase()
    return when {
        combined.contains("thunderstorm") ||
            combined.contains("storms") ||
            combined.contains("heavy rain") -> RideCondition.POOR
        combined.contains("rain") ||
            combined.contains("wind") ||
            combined.contains("visibility") ||
            combined.contains("peak temperature") -> RideCondition.CAUTION
        combined.contains("great") || combined.contains("✅") -> RideCondition.GOOD
        else -> RideCondition.CAUTION
    }
}
