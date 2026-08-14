package com.ridecast.presentation.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.ridecast.core.util.Result
import com.ridecast.domain.model.MapDisplayType
import com.ridecast.domain.model.RideWeather
import com.ridecast.domain.model.WeatherPoint
import com.ridecast.presentation.settings.SettingsViewModel
import com.ridecast.presentation.components.RideMetric
import com.ridecast.presentation.route.RouteUiModel
import com.ridecast.presentation.route.RouteViewModel
import com.ridecast.presentation.theme.RideCastSpacing
import com.ridecast.presentation.theme.RideCastType
import com.ridecast.presentation.theme.RideCastOlive
import com.ridecast.presentation.weather.WeatherViewModel
import com.ridecast.presentation.weather.toEmoji
import com.ridecast.presentation.weather.toWeatherCondition
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val IndiaCentre = LatLng(20.5937, 78.9629)

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    routeViewModel: RouteViewModel,
    weatherViewModel: WeatherViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val routeState by routeViewModel.routeState.collectAsStateWithLifecycle()
    val weatherState by weatherViewModel.weatherState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedWeatherPoint by remember { mutableStateOf<WeatherPoint?>(null) }

    LaunchedEffect(routeState) {
        if (routeState is Result.Error) {
            val error = (routeState as Result.Error)
            snackbarHostState.showSnackbar(
                error.message ?: "Failed to calculate route.",
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        RideMap(
            routeState = routeState,
            weatherState = weatherState,
            mapType = settings.mapType,
            onMapTypeSelected = settingsViewModel::setMapType,
            onMarkerClick = { wp -> selectedWeatherPoint = wp },
            modifier = Modifier.fillMaxSize(),
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = RideCastSpacing.md),
        ) { data ->
            Snackbar(snackbarData = data)
        }
    }

    selectedWeatherPoint?.let { wp ->
        WeatherMarkerBottomSheet(
            weatherPoint = wp,
            distanceKm = (wp.routePoint.distanceFromStartMeters / 1000).toInt(),
            onDismiss = { selectedWeatherPoint = null },
        )
    }
}

@Composable
private fun RideMap(
    routeState: Result<RouteUiModel>,
    weatherState: Result<RideWeather>,
    mapType: MapDisplayType,
    onMapTypeSelected: (MapDisplayType) -> Unit,
    onMarkerClick: (WeatherPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(IndiaCentre, 5f)
    }

    val mapProperties = MapProperties(mapType = mapType.toComposeMapType())

    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                myLocationButtonEnabled = false,
            ),
        )
    }

    val routeColor = RideCastOlive

    val weatherPoints = if (weatherState is Result.Success) {
        (weatherState as Result.Success<RideWeather>).data.weatherPoints
    } else {
        emptyList()
    }

    LaunchedEffect(routeState) {
        if (routeState is Result.Success) {
            val model = (routeState as Result.Success<RouteUiModel>).data
            if (model.polylinePoints.isNotEmpty()) {
                val bounds = LatLngBounds.builder().also { builder ->
                    model.polylinePoints.forEach { builder.include(it) }
                }.build()
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(bounds, 80),
                    durationMs = 800,
                )
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
        ) {
            if (routeState is Result.Success) {
                val model = (routeState as Result.Success<RouteUiModel>).data
                Polyline(
                    points = model.polylinePoints,
                    color = routeColor,
                    width = 6f,
                    geodesic = true,
                )
            }

            weatherPoints.forEach { weatherPoint ->
                val condition = weatherPoint.weather.conditionCode.toWeatherCondition()
                val markerHue = condition.toMarkerHue()
                val distanceKm = (weatherPoint.routePoint.distanceFromStartMeters / 1000).toInt()
                val timeStr = weatherPoint.routePoint.eta.format(
                    DateTimeFormatter.ofPattern("HH:mm"),
                )

                Marker(
                    state = MarkerState(
                        position = LatLng(
                            weatherPoint.routePoint.latitude,
                            weatherPoint.routePoint.longitude,
                        ),
                    ),
                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                    title = "$timeStr · ${weatherPoint.weather.temperatureCelsius.roundToInt()}°C",
                    snippet = "${condition.toEmoji()} ${weatherPoint.weather.conditionText}",
                    onClick = { _ ->
                        onMarkerClick(weatherPoint)
                        true
                    },
                )
            }
        }

        MapTypeOverlay(
            mapType = mapType,
            onMapTypeSelected = onMapTypeSelected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = RideCastSpacing.md, end = RideCastSpacing.md),
        )

        when (routeState) {
            is Result.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            is Result.Success -> {
                val model = (routeState as Result.Success<RouteUiModel>).data
                MapRouteSummaryCard(
                    distanceKm = model.distanceKm,
                    durationFormatted = model.durationFormatted,
                    waypointCount = model.samplePoints.size,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            start = RideCastSpacing.md,
                            bottom = RideCastSpacing.md,
                            end = 56.dp,
                        ),
                )
            }
            is Result.Error -> Unit
        }
    }
}

@Composable
private fun MapRouteSummaryCard(
    distanceKm: Double,
    durationFormatted: String,
    waypointCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RideCastSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Route,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(RideCastSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${distanceKm.roundToInt()} km",
                    style = RideCastType.metricPrimary,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "$waypointCount weather points",
                    style = RideCastType.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            VerticalDivider(
                modifier = Modifier.height(32.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Spacer(Modifier.width(RideCastSpacing.md))
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(RideCastSpacing.sm))
            RideMetric(
                label = "Duration",
                value = durationFormatted,
            )
        }
    }
}
