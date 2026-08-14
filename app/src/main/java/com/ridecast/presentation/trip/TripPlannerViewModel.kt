package com.ridecast.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.ridecast.core.util.Result
import com.ridecast.data.location.LocationService
import com.ridecast.domain.model.TripInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Lightweight wrapper around a resolved Google Place — carries only the fields the UI needs.
 * Kept in the presentation layer to avoid introducing Google Maps types into the domain.
 */
data class PlaceDetails(
    val placeId: String,
    val name: String,
    val address: String,
    val latLng: LatLng,
)

data class TripPlannerUiState(
    val originQuery: String = "",
    val originPredictions: List<AutocompletePrediction> = emptyList(),
    val selectedOrigin: PlaceDetails? = null,
    val destinationQuery: String = "",
    val destinationPredictions: List<AutocompletePrediction> = emptyList(),
    val selectedDestination: PlaceDetails? = null,
    val departureDate: LocalDate = LocalDate.now(),
    val departureTime: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val isLoadingLocation: Boolean = false,
    val locationError: String? = null,
    val canCalculate: Boolean = false,
)

@HiltViewModel
class TripPlannerViewModel @Inject constructor(
    private val placesClient: PlacesClient,
    private val locationService: LocationService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripPlannerUiState())
    val uiState: StateFlow<TripPlannerUiState> = _uiState.asStateFlow()

    fun onOriginQueryChanged(query: String) {
        _uiState.update { it.copy(originQuery = query, selectedOrigin = null) }
        fetchPredictions(query, isOrigin = true)
    }

    fun onOriginSelected(prediction: AutocompletePrediction) {
        _uiState.update {
            it.copy(
                originQuery = prediction.getPrimaryText(null).toString(),
                originPredictions = emptyList(),
            )
        }
        fetchPlaceDetails(prediction, isOrigin = true)
    }

    fun onDestinationQueryChanged(query: String) {
        _uiState.update { it.copy(destinationQuery = query, selectedDestination = null) }
        fetchPredictions(query, isOrigin = false)
    }

    fun onDestinationSelected(prediction: AutocompletePrediction) {
        _uiState.update {
            it.copy(
                destinationQuery = prediction.getPrimaryText(null).toString(),
                destinationPredictions = emptyList(),
            )
        }
        fetchPlaceDetails(prediction, isOrigin = false)
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(departureDate = date) }
    }

    fun onTimeSelected(time: LocalTime) {
        _uiState.update { it.copy(departureTime = time) }
    }

    fun onUseCurrentLocation() {
        _uiState.update { it.copy(isLoadingLocation = true, locationError = null) }
        viewModelScope.launch {
            try {
                when (val result = locationService.getCurrentLocation()) {
                    is Result.Success -> {
                        val latLng = result.data
                        val details = PlaceDetails(
                            placeId = "current_location",
                            name = "Current Location",
                            address = "${latLng.latitude}, ${latLng.longitude}",
                            latLng = latLng,
                        )
                        _uiState.update {
                            it.copy(
                                originQuery = "Current Location",
                                selectedOrigin = details,
                                isLoadingLocation = false,
                            )
                        }
                        updateCanCalculate()
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingLocation = false,
                                locationError = result.message ?: "Unable to get location",
                            )
                        }
                    }
                    is Result.Loading -> Unit
                }
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error fetching current location")
                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        locationError = e.localizedMessage ?: "Location error",
                    )
                }
            }
        }
    }

    fun clearLocationError() {
        _uiState.update { it.copy(locationError = null) }
    }

    fun buildTripInput(): TripInput? {
        val state = _uiState.value
        val origin = state.selectedOrigin ?: return null
        val destination = state.selectedDestination ?: return null
        return TripInput(
            originName = origin.name,
            originLat = origin.latLng.latitude,
            originLng = origin.latLng.longitude,
            destinationName = destination.name,
            destinationLat = destination.latLng.latitude,
            destinationLng = destination.latLng.longitude,
            departureTime = ZonedDateTime.of(
                state.departureDate,
                state.departureTime,
                ZoneId.systemDefault(),
            ),
        )
    }

    private fun fetchPredictions(query: String, isOrigin: Boolean) {
        if (query.length < 2) {
            _uiState.update {
                if (isOrigin) it.copy(originPredictions = emptyList())
                else it.copy(destinationPredictions = emptyList())
            }
            return
        }
        viewModelScope.launch {
            try {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .build()
                val response = withContext(Dispatchers.IO) {
                    placesClient.findAutocompletePredictions(request).await()
                }
                _uiState.update {
                    if (isOrigin) it.copy(originPredictions = response.autocompletePredictions)
                    else it.copy(destinationPredictions = response.autocompletePredictions)
                }
            } catch (e: Exception) {
                Timber.e(e, "Autocomplete predictions failed for query: $query")
                _uiState.update {
                    if (isOrigin) it.copy(originPredictions = emptyList())
                    else it.copy(destinationPredictions = emptyList())
                }
            }
        }
    }

    private fun fetchPlaceDetails(prediction: AutocompletePrediction, isOrigin: Boolean) {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.LOCATION,
            Place.Field.FORMATTED_ADDRESS,
        )
        viewModelScope.launch {
            try {
                val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
                val response = withContext(Dispatchers.IO) {
                    placesClient.fetchPlace(request).await()
                }
                val place = response.place
                val latLng = place.location ?: run {
                    Timber.w("Place ${place.id} returned no LatLng")
                    return@launch
                }
                val details = PlaceDetails(
                    placeId = place.id ?: prediction.placeId,
                    name = place.displayName ?: prediction.getPrimaryText(null).toString(),
                    address = place.formattedAddress ?: prediction.getFullText(null).toString(),
                    latLng = latLng,
                )
                _uiState.update {
                    if (isOrigin) it.copy(selectedOrigin = details)
                    else it.copy(selectedDestination = details)
                }
                updateCanCalculate()
            } catch (e: Exception) {
                Timber.e(e, "FetchPlace failed for ${prediction.placeId}")
            }
        }
    }

    private fun updateCanCalculate() {
        _uiState.update {
            it.copy(canCalculate = it.selectedOrigin != null && it.selectedDestination != null)
        }
    }
}
