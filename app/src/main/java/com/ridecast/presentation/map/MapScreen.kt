package com.ridecast.presentation.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.ridecast.R
import com.ridecast.core.util.Result
import com.ridecast.domain.model.RideWeather
import com.ridecast.domain.model.WeatherPoint
import com.ridecast.presentation.components.RideCastTopBar
import com.ridecast.presentation.route.RouteUiModel
import com.ridecast.presentation.route.RouteViewModel
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
) {
    val routeState by routeViewModel.routeState.collectAsStateWithLifecycle()
    val weatherState by weatherViewModel.weatherState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedWeatherPoint by remember { mutableStateOf<WeatherPoint?>(null) }

    LaunchedEffect(routeState) {
        if (routeState is Result.Error) {
            val error = (routeState as Result.Error)
            snackbarHostState.showSnackbar(
                error.message ?: "Failed to calculate route."
            )
        }
    }

    Scaffold(
        topBar = { RideCastTopBar(title = stringResource(R.string.title_map)) },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        RideMap(
            routeState = routeState,
            weatherState = weatherState,
            onMarkerClick = { wp -> selectedWeatherPoint = wp },
            modifier = Modifier.padding(innerPadding),
        )
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
    onMarkerClick: (WeatherPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(IndiaCentre, 5f)
    }

    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
            ),
        )
    }

    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                myLocationButtonEnabled = false,
            ),
        )
    }

    // Capture theme color before entering the non-composable GoogleMap content lambda
    val routeColor = MaterialTheme.colorScheme.primary

    // Capture weather points before the non-composable lambda
    val weatherPoints = if (weatherState is Result.Success) {
        (weatherState as Result.Success<RideWeather>).data.weatherPoints
    } else emptyList()

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
                    width = 8f,
                    geodesic = true,
                )
            }

            weatherPoints.forEach { weatherPoint ->
                val condition = weatherPoint.weather.conditionCode.toWeatherCondition()
                val markerHue = condition.toMarkerHue()
                val distanceKm = (weatherPoint.routePoint.distanceFromStartMeters / 1000).toInt()
                val timeStr = weatherPoint.routePoint.eta.format(
                    DateTimeFormatter.ofPattern("HH:mm")
                )

                Marker(
                    state = MarkerState(
                        position = LatLng(
                            weatherPoint.routePoint.latitude,
                            weatherPoint.routePoint.longitude,
                        )
                    ),
                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                    title = "$timeStr · ${weatherPoint.weather.temperatureCelsius.roundToInt()}°C",
                    snippet = "${condition.toEmoji()} ${weatherPoint.weather.conditionText}",
                    onClick = { _ ->
                        onMarkerClick(weatherPoint)
                        true  // consume the click to suppress default info window
                    },
                )
            }
        }

        when (routeState) {
            is Result.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is Result.Success -> {
                val model = (routeState as Result.Success<RouteUiModel>).data
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                ) {
                    Text(
                        text = "${model.distanceKm.roundToInt()} km · ${model.durationFormatted}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            }
            is Result.Error -> Unit
        }
    }
}
