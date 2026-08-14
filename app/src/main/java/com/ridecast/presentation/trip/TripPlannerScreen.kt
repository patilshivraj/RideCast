package com.ridecast.presentation.trip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.ridecast.core.util.Result
import com.ridecast.presentation.components.RideCastTopBar
import com.ridecast.presentation.permissions.LocationPermissionHandler
import com.ridecast.presentation.route.RouteViewModel
import com.ridecast.presentation.weather.WeatherViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TripPlannerScreen(
    modifier: Modifier = Modifier,
    viewModel: TripPlannerViewModel = hiltViewModel(),
    routeViewModel: RouteViewModel,
    weatherViewModel: WeatherViewModel,
) {
    LocationPermissionHandler(
        onGranted = { /* Permission available — "Use Current Location" button is active */ },
        onDenied = { /* Location denied — manual entry still works fine */ },
    ) {
        TripPlannerContent(
            modifier = modifier,
            viewModel = viewModel,
            routeViewModel = routeViewModel,
            weatherViewModel = weatherViewModel,
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
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val routeState by routeViewModel.routeState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Surface location errors via Snackbar
    LaunchedEffect(uiState.locationError) {
        uiState.locationError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearLocationError()
        }
    }

    Scaffold(
        topBar = { RideCastTopBar("Plan Your Ride") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // ── Origin ──────────────────────────────────────────────────────
            Text(
                text = "Origin",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            PlacesSearchField(
                query = uiState.originQuery,
                predictions = uiState.originPredictions,
                onQueryChanged = viewModel::onOriginQueryChanged,
                onPredictionSelected = viewModel::onOriginSelected,
                placeholder = "Search origin…",
            )

            Spacer(Modifier.height(8.dp))

            // ── Use Current Location ─────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = viewModel::onUseCurrentLocation,
                    enabled = !uiState.isLoadingLocation,
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Use Current Location")
                }
                if (uiState.isLoadingLocation) {
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Destination ──────────────────────────────────────────────────
            Text(
                text = "Destination",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            PlacesSearchField(
                query = uiState.destinationQuery,
                predictions = uiState.destinationPredictions,
                onQueryChanged = viewModel::onDestinationQueryChanged,
                onPredictionSelected = viewModel::onDestinationSelected,
                placeholder = "Search destination…",
            )

            Spacer(Modifier.height(20.dp))

            // ── Departure Date ───────────────────────────────────────────────
            Text(
                text = "Departure Date",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    uiState.departureDate.format(
                        DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"),
                    ),
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Departure Time ───────────────────────────────────────────────
            Text(
                text = "Departure Time",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    uiState.departureTime.format(
                        DateTimeFormatter.ofPattern("hh:mm a"),
                    ),
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Calculate Ride ───────────────────────────────────────────────
            val isCalculating = routeState is Result.Loading
            Button(
                onClick = {
                    val tripInput = viewModel.buildTripInput()
                    if (tripInput != null) {
                        weatherViewModel.clearWeather()
                        routeViewModel.calculateRoute(tripInput)
                    }
                },
                enabled = uiState.canCalculate && !isCalculating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isCalculating) "Calculating…" else "Calculate Ride")
            }

            if (isCalculating) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (routeState is Result.Success) {
                Spacer(Modifier.height(8.dp))
                val model = (routeState as Result.Success).data
                Text(
                    text = "✅ Route ready — ${model.distanceKm.toInt()} km · ${model.durationFormatted}. Tap Map or Timeline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Only show error if it's a real error (not the initial empty state)
            val errorMsg = (routeState as? Result.Error)?.message
            if (routeState is Result.Error && errorMsg != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "❌ $errorMsg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    // ── Date Picker Dialog ───────────────────────────────────────────────────
    if (showDatePicker) {
        val initialMillis = uiState.departureDate
            .atStartOfDay(ZoneId.systemDefault())
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

    // ── Time Picker Dialog ───────────────────────────────────────────────────
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

/**
 * A search input field with a dropdown showing autocomplete predictions.
 */
@Composable
private fun PlacesSearchField(
    query: String,
    predictions: List<AutocompletePrediction>,
    onQueryChanged: (String) -> Unit,
    onPredictionSelected: (AutocompletePrediction) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                )
            },
            singleLine = true,
        )

        if (predictions.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column {
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
