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
import com.ridecast.domain.model.FavoritePlace
import com.ridecast.domain.model.RouteStop
import com.ridecast.domain.model.TravelMode
import com.ridecast.domain.model.TripInput
import com.ridecast.domain.repository.FavoritePlacesRepository
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
import java.util.UUID
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

data class IntermediateStopState(
    val id: String = UUID.randomUUID().toString(),
    val query: String = "",
    val predictions: List<AutocompletePrediction> = emptyList(),
    val selectedPlace: PlaceDetails? = null,
)

data class TripPlannerUiState(
    val originQuery: String = "",
    val originPredictions: List<AutocompletePrediction> = emptyList(),
    val selectedOrigin: PlaceDetails? = null,
    val destinationQuery: String = "",
    val destinationPredictions: List<AutocompletePrediction> = emptyList(),
    val selectedDestination: PlaceDetails? = null,
    val intermediateStops: List<IntermediateStopState> = emptyList(),
    val departureDate: LocalDate = LocalDate.now(),
    val departureTime: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val isLoadingLocation: Boolean = false,
    val locationError: String? = null,
    /** Drives Plan screen map background; null keeps the default India overview. */
    val mapCenterLatLng: LatLng? = null,
    val canCalculate: Boolean = false,
    val favoritePlaces: List<FavoritePlace> = emptyList(),
    val favoritesMessage: String? = null,
    val travelMode: TravelMode = TravelMode.TWO_WHEELER,
)

@HiltViewModel
class TripPlannerViewModel @Inject constructor(
    private val placesClient: PlacesClient,
    private val locationService: LocationService,
    private val favoritePlacesRepository: FavoritePlacesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripPlannerUiState())
    val uiState: StateFlow<TripPlannerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritePlacesRepository.favorites.collect { favorites ->
                _uiState.update { it.copy(favoritePlaces = favorites) }
            }
        }
    }

    fun onOriginQueryChanged(query: String) {
        _uiState.update { it.copy(originQuery = query, selectedOrigin = null) }
        fetchPredictions(query, StopTarget.Origin)
    }

    fun onOriginSelected(prediction: AutocompletePrediction) {
        _uiState.update {
            it.copy(
                originQuery = prediction.getPrimaryText(null).toString(),
                originPredictions = emptyList(),
            )
        }
        fetchPlaceDetails(prediction, StopTarget.Origin)
    }

    fun onDestinationQueryChanged(query: String) {
        _uiState.update { it.copy(destinationQuery = query, selectedDestination = null) }
        fetchPredictions(query, StopTarget.Destination)
    }

    fun onDestinationSelected(prediction: AutocompletePrediction) {
        _uiState.update {
            it.copy(
                destinationQuery = prediction.getPrimaryText(null).toString(),
                destinationPredictions = emptyList(),
            )
        }
        fetchPlaceDetails(prediction, StopTarget.Destination)
    }

    fun onIntermediateQueryChanged(stopId: String, query: String) {
        _uiState.update { state ->
            state.copy(
                intermediateStops = state.intermediateStops.map { stop ->
                    if (stop.id == stopId) stop.copy(query = query, selectedPlace = null) else stop
                },
            )
        }
        fetchPredictions(query, StopTarget.Intermediate(stopId))
    }

    fun onIntermediateSelected(stopId: String, prediction: AutocompletePrediction) {
        _uiState.update { state ->
            state.copy(
                intermediateStops = state.intermediateStops.map { stop ->
                    if (stop.id == stopId) {
                        stop.copy(
                            query = prediction.getPrimaryText(null).toString(),
                            predictions = emptyList(),
                        )
                    } else {
                        stop
                    }
                },
            )
        }
        fetchPlaceDetails(prediction, StopTarget.Intermediate(stopId))
    }

    fun addIntermediateStop() {
        _uiState.update { state ->
            if (state.intermediateStops.size >= MAX_INTERMEDIATE_STOPS) {
                state
            } else {
                state.copy(intermediateStops = state.intermediateStops + IntermediateStopState())
            }
        }
    }

    fun removeIntermediateStop(stopId: String) {
        _uiState.update { state ->
            state.copy(intermediateStops = state.intermediateStops.filter { it.id != stopId })
        }
        updateCanCalculate()
    }

    /** Swaps origin and destination; route recalculation is triggered from the Plan screen when input is valid. */
    fun swapOriginAndDestination() {
        _uiState.update { state ->
            state.copy(
                originQuery = state.destinationQuery,
                destinationQuery = state.originQuery,
                selectedOrigin = state.selectedDestination,
                selectedDestination = state.selectedOrigin,
                originPredictions = emptyList(),
                destinationPredictions = emptyList(),
            )
        }
        updateCanCalculate()
    }

    /** Moves an intermediate stop within the list (used for drag-to-reorder). */
    fun moveIntermediateStop(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        _uiState.update { state ->
            val stops = state.intermediateStops.toMutableList()
            if (fromIndex !in stops.indices || toIndex !in stops.indices) return@update state
            val item = stops.removeAt(fromIndex)
            stops.add(toIndex, item)
            state.copy(intermediateStops = stops)
        }
        updateCanCalculate()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(departureDate = date) }
    }

    fun onTimeSelected(time: LocalTime) {
        _uiState.update { it.copy(departureTime = time) }
    }

    fun onTravelModeSelected(mode: TravelMode) {
        _uiState.update { it.copy(travelMode = mode) }
    }

    /** Fetches device location for the Plan screen map background (silent — no loading UI). */
    fun loadMapCenterOnAppear() {
        viewModelScope.launch {
            when (val result = locationService.getCurrentLocation()) {
                is Result.Success -> {
                    _uiState.update { it.copy(mapCenterLatLng = result.data) }
                }
                else -> Unit
            }
        }
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
                                mapCenterLatLng = latLng,
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

    fun clearFavoritesMessage() {
        _uiState.update { it.copy(favoritesMessage = null) }
    }

    fun isFavorite(placeId: String?): Boolean {
        if (placeId == null) return false
        return _uiState.value.favoritePlaces.any { it.placeId == placeId }
    }

    fun toggleFavorite(details: PlaceDetails) {
        if (details.placeId == "current_location") return
        viewModelScope.launch {
            if (favoritePlacesRepository.isFavorite(details.placeId)) {
                favoritePlacesRepository.removeFavorite(details.placeId)
            } else {
                val added = favoritePlacesRepository.addFavorite(
                    FavoritePlace(
                        placeId = details.placeId,
                        name = details.name,
                        lat = details.latLng.latitude,
                        lng = details.latLng.longitude,
                    ),
                )
                if (!added) {
                    _uiState.update {
                        it.copy(favoritesMessage = "You can save up to 15 places")
                    }
                }
            }
        }
    }

    fun setFavoriteAsOrigin(favorite: FavoritePlace) {
        applyFavorite(favorite, isOrigin = true)
    }

    fun setFavoriteAsDestination(favorite: FavoritePlace) {
        applyFavorite(favorite, isOrigin = false)
    }

    private fun applyFavorite(favorite: FavoritePlace, isOrigin: Boolean) {
        val details = PlaceDetails(
            placeId = favorite.placeId,
            name = favorite.name,
            address = favorite.name,
            latLng = LatLng(favorite.lat, favorite.lng),
        )
        _uiState.update {
            if (isOrigin) {
                it.copy(
                    originQuery = favorite.name,
                    selectedOrigin = details,
                    originPredictions = emptyList(),
                )
            } else {
                it.copy(
                    destinationQuery = favorite.name,
                    selectedDestination = details,
                    destinationPredictions = emptyList(),
                )
            }
        }
        updateCanCalculate()
    }

    fun buildTripInput(): TripInput? {
        val state = _uiState.value
        val origin = state.selectedOrigin ?: return null
        val destination = state.selectedDestination ?: return null
        if (state.intermediateStops.any { it.selectedPlace == null }) return null

        val intermediateStops = state.intermediateStops.map { stop ->
            val place = stop.selectedPlace ?: return null
            RouteStop(
                name = place.name,
                lat = place.latLng.latitude,
                lng = place.latLng.longitude,
                placeId = place.placeId.takeIf { it != "current_location" },
            )
        }

        return TripInput(
            originName = origin.name,
            originLat = origin.latLng.latitude,
            originLng = origin.latLng.longitude,
            destinationName = destination.name,
            destinationLat = destination.latLng.latitude,
            destinationLng = destination.latLng.longitude,
            intermediateStops = intermediateStops,
            departureTime = ZonedDateTime.of(
                state.departureDate,
                state.departureTime,
                ZoneId.systemDefault(),
            ),
            travelMode = state.travelMode,
        )
    }

    private sealed class StopTarget {
        data object Origin : StopTarget()
        data object Destination : StopTarget()
        data class Intermediate(val id: String) : StopTarget()
    }

    private fun fetchPredictions(query: String, target: StopTarget) {
        if (query.length < 2) {
            _uiState.update { state -> state.withPredictions(target, emptyList()) }
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
                _uiState.update { state ->
                    state.withPredictions(target, response.autocompletePredictions)
                }
            } catch (e: Exception) {
                Timber.e(e, "Autocomplete predictions failed for query: $query")
                _uiState.update { state -> state.withPredictions(target, emptyList()) }
            }
        }
    }

    private fun TripPlannerUiState.withPredictions(
        target: StopTarget,
        predictions: List<AutocompletePrediction>,
    ): TripPlannerUiState = when (target) {
        StopTarget.Origin -> copy(originPredictions = predictions)
        StopTarget.Destination -> copy(destinationPredictions = predictions)
        is StopTarget.Intermediate -> copy(
            intermediateStops = intermediateStops.map { stop ->
                if (stop.id == target.id) stop.copy(predictions = predictions) else stop
            },
        )
    }

    private fun fetchPlaceDetails(prediction: AutocompletePrediction, target: StopTarget) {
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
                _uiState.update { state -> state.withSelectedPlace(target, details) }
                updateCanCalculate()
            } catch (e: Exception) {
                Timber.e(e, "FetchPlace failed for ${prediction.placeId}")
            }
        }
    }

    private fun TripPlannerUiState.withSelectedPlace(
        target: StopTarget,
        details: PlaceDetails,
    ): TripPlannerUiState = when (target) {
        StopTarget.Origin -> copy(selectedOrigin = details)
        StopTarget.Destination -> copy(selectedDestination = details)
        is StopTarget.Intermediate -> copy(
            intermediateStops = intermediateStops.map { stop ->
                if (stop.id == target.id) stop.copy(selectedPlace = details) else stop
            },
        )
    }

    private fun updateCanCalculate() {
        _uiState.update { state ->
            val allIntermediatesResolved = state.intermediateStops.all { it.selectedPlace != null }
            state.copy(
                canCalculate = state.selectedOrigin != null &&
                    state.selectedDestination != null &&
                    allIntermediatesResolved,
            )
        }
    }

    companion object {
        const val MAX_INTERMEDIATE_STOPS = 5
    }
}
