package com.ridecast.presentation.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.onFocusChanged

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.ridecast.core.util.Result
import com.ridecast.domain.model.FavoritePlace
import com.ridecast.domain.model.MapDisplayType
import com.ridecast.domain.model.TravelMode
import com.ridecast.presentation.map.toComposeMapType
import com.ridecast.presentation.components.RideCastPrimaryButton
import com.ridecast.presentation.components.RideCastSurfaceCard
import com.ridecast.presentation.components.RideMetric
import com.ridecast.presentation.components.SectionHeader
import com.ridecast.presentation.permissions.LocationPermissionHandler
import com.ridecast.presentation.route.RouteUiModel
import com.ridecast.presentation.route.RouteViewModel
import com.ridecast.presentation.theme.RideCastSpacing
import com.ridecast.presentation.theme.RideCastType
import com.ridecast.presentation.weather.WeatherViewModel
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val IndiaCentre = LatLng(20.5937, 78.9629)
private const val PLAN_MAP_RADIUS_METERS = 50_000.0
private const val PLAN_MAP_BOUNDS_PADDING_PX = 80
private val IntermediateStopRowHeight = 64.dp

private fun recalculateRouteIfValid(
    viewModel: TripPlannerViewModel,
    routeViewModel: RouteViewModel,
    weatherViewModel: WeatherViewModel,
    routeState: Result<RouteUiModel>,
    requireExistingRoute: Boolean = false,
) {
    if (requireExistingRoute && routeState !is Result.Success) return
    val tripInput = viewModel.buildTripInput() ?: return
    weatherViewModel.clearWeather()
    routeViewModel.calculateRoute(tripInput)
}

@Composable
fun TripPlannerScreen(
    modifier: Modifier = Modifier,
    viewModel: TripPlannerViewModel = hiltViewModel(),
    routeViewModel: RouteViewModel,
    weatherViewModel: WeatherViewModel,
    mapType: MapDisplayType = MapDisplayType.NORMAL,
) {
    LocationPermissionHandler(
        onGranted = { viewModel.loadMapCenterOnAppear() },
        onDenied = { },
    ) {
        TripPlannerContent(
            modifier = modifier,
            viewModel = viewModel,
            routeViewModel = routeViewModel,
            weatherViewModel = weatherViewModel,
            mapType = mapType,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripPlannerContent(
    modifier: Modifier = Modifier,
    viewModel: TripPlannerViewModel,
    routeViewModel: RouteViewModel,
    weatherViewModel: WeatherViewModel,
    mapType: MapDisplayType,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val routeState by routeViewModel.routeState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var favoritePickerTarget by remember { mutableStateOf<FavoritePlace?>(null) }

    LaunchedEffect(uiState.locationError) {
        uiState.locationError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearLocationError()
        }
    }

    LaunchedEffect(uiState.favoritesMessage) {
        uiState.favoritesMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearFavoritesMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PlanMapBackground(
            routeState = routeState,
            mapType = mapType,
            mapCenter = uiState.mapCenterLatLng,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = RideCastSpacing.md, vertical = RideCastSpacing.md),
            ) {
                Text(
                    text = "Plan Your Ride",
                    style = RideCastType.screenTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(RideCastSpacing.md))

                SectionHeader(
                    title = "Route",
                    subtitle = "Where are you riding today?",
                )

                RideCastSurfaceCard {
                    RouteFlowSection(
                        originQuery = uiState.originQuery,
                        destinationQuery = uiState.destinationQuery,
                        originPredictions = uiState.originPredictions,
                        destinationPredictions = uiState.destinationPredictions,
                        selectedOrigin = uiState.selectedOrigin,
                        selectedDestination = uiState.selectedDestination,
                        intermediateStops = uiState.intermediateStops,
                        isOriginFavorite = viewModel.isFavorite(uiState.selectedOrigin?.placeId),
                        isDestinationFavorite = viewModel.isFavorite(uiState.selectedDestination?.placeId),
                        onOriginQueryChanged = viewModel::onOriginQueryChanged,
                        onDestinationQueryChanged = viewModel::onDestinationQueryChanged,
                        onOriginSelected = viewModel::onOriginSelected,
                        onDestinationSelected = viewModel::onDestinationSelected,
                        onIntermediateQueryChanged = viewModel::onIntermediateQueryChanged,
                        onIntermediateSelected = viewModel::onIntermediateSelected,
                        onAddIntermediateStop = viewModel::addIntermediateStop,
                        onRemoveIntermediateStop = viewModel::removeIntermediateStop,
                        onMoveIntermediateStop = viewModel::moveIntermediateStop,
                        onIntermediateStopReorderComplete = {
                            recalculateRouteIfValid(
                                viewModel = viewModel,
                                routeViewModel = routeViewModel,
                                weatherViewModel = weatherViewModel,
                                routeState = routeState,
                                requireExistingRoute = true,
                            )
                        },
                        onSwapOriginAndDestination = {
                            viewModel.swapOriginAndDestination()
                            recalculateRouteIfValid(
                                viewModel = viewModel,
                                routeViewModel = routeViewModel,
                                weatherViewModel = weatherViewModel,
                                routeState = routeState,
                            )
                        },
                        onToggleOriginFavorite = {
                            uiState.selectedOrigin?.let(viewModel::toggleFavorite)
                        },
                        onToggleDestinationFavorite = {
                            uiState.selectedDestination?.let(viewModel::toggleFavorite)
                        },
                        isLoadingLocation = uiState.isLoadingLocation,
                        onUseCurrentLocation = viewModel::onUseCurrentLocation,
                    )
                }

                if (uiState.favoritePlaces.isNotEmpty()) {
                    Spacer(Modifier.height(RideCastSpacing.md))
                    FavoritePlacesRow(
                        favorites = uiState.favoritePlaces,
                        onFavoriteClick = { favoritePickerTarget = it },
                    )
                }

                Spacer(Modifier.height(RideCastSpacing.lg))

                SectionHeader(title = "Departure")

                RideCastSurfaceCard {
                    DepartureControlsRow(
                        dateFormatted = uiState.departureDate.format(
                            DateTimeFormatter.ofPattern("EEE, MMM d"),
                        ),
                        timeFormatted = uiState.departureTime.format(
                            DateTimeFormatter.ofPattern("h:mm a"),
                        ),
                        onDateClick = { showDatePicker = true },
                        onTimeClick = { showTimePicker = true },
                    )
                }

                Spacer(Modifier.height(RideCastSpacing.lg))

                SectionHeader(
                    title = "Vehicle",
                    subtitle = "Route and ETA depend on travel mode",
                )

                RideCastSurfaceCard {
                    TravelModeSelector(
                        selectedMode = uiState.travelMode,
                        onModeSelected = viewModel::onTravelModeSelected,
                    )
                }

                Spacer(Modifier.height(RideCastSpacing.lg))

                val isCalculating = routeState is Result.Loading
                RideCastPrimaryButton(
                    text = if (isCalculating) "Calculating route…" else "Calculate Ride",
                    onClick = {
                        recalculateRouteIfValid(
                            viewModel = viewModel,
                            routeViewModel = routeViewModel,
                            weatherViewModel = weatherViewModel,
                            routeState = routeState,
                        )
                    },
                    enabled = uiState.canCalculate && !isCalculating,
                )

                if (isCalculating) {
                    Spacer(Modifier.height(RideCastSpacing.sm))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }

                when (routeState) {
                    is Result.Success -> {
                        Spacer(Modifier.height(RideCastSpacing.md))
                        RouteReadyCard(model = (routeState as Result.Success).data)
                    }
                    is Result.Error -> {
                        val errorMsg = (routeState as Result.Error).message
                        if (errorMsg != null) {
                            Spacer(Modifier.height(RideCastSpacing.md))
                            Text(
                                text = errorMsg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    else -> Unit
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(RideCastSpacing.md),
            )
        }
    }

    favoritePickerTarget?.let { favorite ->
        AlertDialog(
            onDismissRequest = { favoritePickerTarget = null },
            title = { Text(favorite.name) },
            text = { Text("Use this saved place as…") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setFavoriteAsDestination(favorite)
                        favoritePickerTarget = null
                    },
                ) { Text("Destination") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setFavoriteAsOrigin(favorite)
                        favoritePickerTarget = null
                    },
                ) { Text("Origin") }
            },
        )
    }

    if (showDatePicker) {
        val initialMillis = uiState.departureDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.onDateSelected(date)
                            recalculateRouteIfValid(
                                viewModel = viewModel,
                                routeViewModel = routeViewModel,
                                weatherViewModel = weatherViewModel,
                                routeState = routeState,
                                requireExistingRoute = true,
                            )
                        }
                        showDatePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.departureTime.hour,
            initialMinute = uiState.departureTime.minute,
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Departure Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onTimeSelected(
                            LocalTime.of(timePickerState.hour, timePickerState.minute),
                        )
                        recalculateRouteIfValid(
                            viewModel = viewModel,
                            routeViewModel = routeViewModel,
                            weatherViewModel = weatherViewModel,
                            routeState = routeState,
                            requireExistingRoute = true,
                        )
                        showTimePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        )
    }
}

private fun latLngBoundsForRadius(center: LatLng, radiusMeters: Double): LatLngBounds {
    val north = SphericalUtil.computeOffset(center, radiusMeters, 0.0)
    val east = SphericalUtil.computeOffset(center, radiusMeters, 90.0)
    val south = SphericalUtil.computeOffset(center, radiusMeters, 180.0)
    val west = SphericalUtil.computeOffset(center, radiusMeters, 270.0)
    return LatLngBounds.builder()
        .include(north)
        .include(east)
        .include(south)
        .include(west)
        .build()
}

@Composable
private fun PlanMapBackground(
    routeState: Result<RouteUiModel>,
    mapType: MapDisplayType,
    mapCenter: LatLng?,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(IndiaCentre, 5f)
    }

    LaunchedEffect(routeState, mapCenter) {
        when {
            routeState is Result.Success -> {
                val model = (routeState as Result.Success<RouteUiModel>).data
                if (model.polylinePoints.isNotEmpty()) {
                    val bounds = LatLngBounds.builder().also { builder ->
                        model.polylinePoints.forEach { builder.include(it) }
                    }.build()
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngBounds(bounds, 120),
                        durationMs = 600,
                    )
                }
            }
            mapCenter != null -> {
                val bounds = latLngBoundsForRadius(mapCenter, PLAN_MAP_RADIUS_METERS)
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(bounds, PLAN_MAP_BOUNDS_PADDING_PX),
                    durationMs = 600,
                )
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = mapType.toComposeMapType()),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
        ),
    )
}

@Composable
private fun FavoritePlacesRow(
    favorites: List<FavoritePlace>,
    onFavoriteClick: (FavoritePlace) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Saved places",
            style = RideCastType.label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(RideCastSpacing.xs))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(RideCastSpacing.sm),
        ) {
            favorites.forEach { favorite ->
                SuggestionChip(
                    onClick = { onFavoriteClick(favorite) },
                    label = { Text(favorite.name, maxLines = 1) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun RouteFlowSection(
    originQuery: String,
    destinationQuery: String,
    originPredictions: List<AutocompletePrediction>,
    destinationPredictions: List<AutocompletePrediction>,
    selectedOrigin: PlaceDetails?,
    selectedDestination: PlaceDetails?,
    intermediateStops: List<IntermediateStopState>,
    isOriginFavorite: Boolean,
    isDestinationFavorite: Boolean,
    onOriginQueryChanged: (String) -> Unit,
    onDestinationQueryChanged: (String) -> Unit,
    onOriginSelected: (AutocompletePrediction) -> Unit,
    onDestinationSelected: (AutocompletePrediction) -> Unit,
    onIntermediateQueryChanged: (String, String) -> Unit,
    onIntermediateSelected: (String, AutocompletePrediction) -> Unit,
    onAddIntermediateStop: () -> Unit,
    onRemoveIntermediateStop: (String) -> Unit,
    onMoveIntermediateStop: (Int, Int) -> Unit,
    onIntermediateStopReorderComplete: () -> Unit,
    onSwapOriginAndDestination: () -> Unit,
    onToggleOriginFavorite: () -> Unit,
    onToggleDestinationFavorite: () -> Unit,
    isLoadingLocation: Boolean,
    onUseCurrentLocation: () -> Unit,
) {
    val connectorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val canAddStop = intermediateStops.size < TripPlannerViewModel.MAX_INTERMEDIATE_STOPS
    val density = LocalDensity.current
    val rowHeightPx = with(density) { IntermediateStopRowHeight.toPx() }
    var draggedStopId by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.width(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RouteFlowDot(isOrigin = true)
            RouteFlowConnector(color = connectorColor)
            RouteFlowConnector(color = connectorColor)

            intermediateStops.forEach { _ ->
                RouteFlowWaypointDot()
                RouteFlowConnector(color = connectorColor)
            }

            if (canAddStop) {
                RouteFlowConnector(color = connectorColor)
            }

            Icon(
                imageVector = Icons.Outlined.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RouteFlowDot(isOrigin = false)
        }

        Spacer(Modifier.width(RideCastSpacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            PlacesSearchField(
                query = originQuery,
                predictions = originPredictions,
                onQueryChanged = onOriginQueryChanged,
                onPredictionSelected = onOriginSelected,
                placeholder = "Choose starting point",
                showBookmark = selectedOrigin != null &&
                    selectedOrigin.placeId != "current_location",
                isBookmarked = isOriginFavorite,
                onToggleBookmark = onToggleOriginFavorite,
                showCurrentLocationOption = true,
                onCurrentLocationClick = onUseCurrentLocation,
                isLoadingCurrentLocation = isLoadingLocation,
            )

            Spacer(Modifier.height(RideCastSpacing.sm))

            intermediateStops.forEachIndexed { index, stop ->
                val isDragging = draggedStopId == stop.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = if (isDragging) dragOffsetY else 0f
                            alpha = if (draggedStopId != null && !isDragging) 0.85f else 1f
                        },
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Filled.DragHandle,
                        contentDescription = "Reorder stop ${index + 1}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(top = RideCastSpacing.sm)
                            .pointerInput(stop.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedStopId = stop.id
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        if (draggedStopId == stop.id) {
                                            dragOffsetY += dragAmount.y
                                        }
                                    },
                                    onDragEnd = {
                                        val draggedId = draggedStopId
                                        val fromIndex = intermediateStops.indexOfFirst { it.id == draggedId }
                                        if (fromIndex >= 0) {
                                            val delta = (dragOffsetY / rowHeightPx).roundToInt()
                                            val toIndex = (fromIndex + delta)
                                                .coerceIn(0, intermediateStops.lastIndex)
                                            if (fromIndex != toIndex) {
                                                onMoveIntermediateStop(fromIndex, toIndex)
                                                onIntermediateStopReorderComplete()
                                            }
                                        }
                                        draggedStopId = null
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        draggedStopId = null
                                        dragOffsetY = 0f
                                    },
                                )
                            },
                    )
                    PlacesSearchField(
                        query = stop.query,
                        predictions = stop.predictions,
                        onQueryChanged = { onIntermediateQueryChanged(stop.id, it) },
                        onPredictionSelected = { onIntermediateSelected(stop.id, it) },
                        placeholder = "Stop ${index + 1}",
                        showBookmark = false,
                        isBookmarked = false,
                        onToggleBookmark = {},
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onRemoveIntermediateStop(stop.id) }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove stop",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(RideCastSpacing.sm))
            }

            if (canAddStop) {
                TextButton(onClick = onAddIntermediateStop) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(RideCastSpacing.xs))
                    Text("Add stop", style = RideCastType.label)
                }
                Spacer(Modifier.height(RideCastSpacing.sm))
            }

            PlacesSearchField(
                query = destinationQuery,
                predictions = destinationPredictions,
                onQueryChanged = onDestinationQueryChanged,
                onPredictionSelected = onDestinationSelected,
                placeholder = "Choose destination",
                showBookmark = selectedDestination != null,
                isBookmarked = isDestinationFavorite,
                onToggleBookmark = onToggleDestinationFavorite,
                showCurrentLocationOption = true,
                onCurrentLocationClick = onUseCurrentLocation,
                isLoadingCurrentLocation = isLoadingLocation,
            )
        }

        IconButton(
            onClick = onSwapOriginAndDestination,
            modifier = Modifier.padding(top = RideCastSpacing.xs),
        ) {
            Icon(
                imageVector = Icons.Filled.SwapVert,
                contentDescription = "Swap origin and destination",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RouteFlowDot(isOrigin: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(
                if (isOrigin) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
            ),
    )
}

@Composable
private fun RouteFlowWaypointDot() {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outline),
    )
}

@Composable
private fun RouteFlowConnector(color: Color) {
    Box(
        modifier = Modifier
            .width(2.dp)
            .height(24.dp)
            .background(color),
    )
}

@Composable
private fun TravelModeSelector(
    selectedMode: TravelMode,
    onModeSelected: (TravelMode) -> Unit,
) {
    val options = listOf(TravelMode.TWO_WHEELER to "Motorcycle", TravelMode.DRIVE to "Car")
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                selected = mode == selectedMode,
                onClick = { onModeSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
                label = { Text(label, maxLines = 1) },
            )
        }
    }
}

@Composable
private fun DepartureControlsRow(
    dateFormatted: String,
    timeFormatted: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(RideCastSpacing.sm),
    ) {
        DepartureChip(
            icon = Icons.Filled.CalendarToday,
            label = "Date",
            value = dateFormatted,
            onClick = onDateClick,
            modifier = Modifier.weight(1f),
        )
        DepartureChip(
            icon = Icons.Filled.Schedule,
            label = "Time",
            value = timeFormatted,
            onClick = onTimeClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DepartureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RideCastSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(RideCastSpacing.sm))
            Column {
                Text(
                    text = label,
                    style = RideCastType.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = RideCastType.metricSecondary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun RouteReadyCard(model: RouteUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RideCastSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Route ready",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(RideCastSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Route ready",
                    style = RideCastType.cardTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Open Timeline or Map to explore weather along your ride.",
                    style = RideCastType.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RideMetric(
                label = "Distance",
                value = "${model.distanceKm.roundToInt()} km",
                horizontalAlignment = Alignment.End,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RideCastSpacing.md, vertical = RideCastSpacing.sm),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            RideMetric(
                label = "Duration",
                value = model.durationFormatted,
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            RideMetric(
                label = "Waypoints",
                value = "${model.samplePoints.size}",
                horizontalAlignment = Alignment.CenterHorizontally,
            )
        }
    }
}

@Composable
private fun PlacesSearchField(
    query: String,
    predictions: List<AutocompletePrediction>,
    onQueryChanged: (String) -> Unit,
    onPredictionSelected: (AutocompletePrediction) -> Unit,
    placeholder: String,
    showBookmark: Boolean,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier,
    showCurrentLocationOption: Boolean = false,
    onCurrentLocationClick: () -> Unit = {},
    isLoadingCurrentLocation: Boolean = false,
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 2.dp,
            shadowElevation = 1.dp,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    if (showBookmark) {
                        IconButton(onClick = onToggleBookmark) {
                            Icon(
                                imageVector = if (isBookmarked) {
                                    Icons.Filled.Bookmark
                                } else {
                                    Icons.Outlined.BookmarkBorder
                                },
                                contentDescription = if (isBookmarked) {
                                    "Remove from saved places"
                                } else {
                                    "Save place"
                                },
                                tint = if (isBookmarked) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
            )
        }

        val showDropdown = predictions.isNotEmpty() || (isFocused && query.isEmpty() && showCurrentLocationOption)
        
        if (showDropdown) {
            Spacer(Modifier.height(RideCastSpacing.xs))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column {
                    if (isFocused && query.isEmpty() && showCurrentLocationOption) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = "Your location",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            leadingContent = {
                                if (isLoadingCurrentLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.MyLocation,
                                        contentDescription = "Use current location",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.clickable(
                                enabled = !isLoadingCurrentLocation,
                                onClick = onCurrentLocationClick
                            )
                        )
                    }
                    
                    predictions.forEach { prediction ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = prediction.getPrimaryText(null).toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = prediction.getSecondaryText(null).toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            modifier = Modifier.clickable { onPredictionSelected(prediction) },
                        )
                    }
                }
            }
        }
    }
}
