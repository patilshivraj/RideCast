package com.ridecast.presentation.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.ridecast.core.util.PolylineDecoder
import com.ridecast.core.util.Result
import com.ridecast.domain.model.Route
import com.ridecast.domain.model.RoutePoint
import com.ridecast.domain.model.SamplingConfig
import com.ridecast.domain.model.TripInput
import com.ridecast.domain.repository.RouteRepository
import com.ridecast.domain.usecase.SampleRouteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class RouteUiModel(
    val route: Route,
    val polylinePoints: List<LatLng>,
    val distanceKm: Double,
    val durationFormatted: String,
    val samplePoints: List<RoutePoint>,
)

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val sampleRouteUseCase: SampleRouteUseCase,
) : ViewModel() {

    // Initial state is "not yet calculated" — use Error(null) as a sentinel to distinguish
    // from an in-progress calculation (Loading) and a real error (Error with message).
    private val _routeState = MutableStateFlow<Result<RouteUiModel>>(Result.Error(null, null))
    val routeState: StateFlow<Result<RouteUiModel>> = _routeState.asStateFlow()

    private var _currentInput: TripInput? = null
    private var _samplingConfig: SamplingConfig = SamplingConfig()

    fun calculateRoute(input: TripInput) {
        _currentInput = input
        _routeState.value = Result.Loading
        viewModelScope.launch {
            _routeState.value = when (val result = routeRepository.calculateRoute(input)) {
                is Result.Success -> {
                    val decoded = PolylineDecoder.decode(result.data.encodedPolyline)
                    val samplePoints = sampleRouteUseCase(
                        polylinePoints = decoded,
                        totalDistanceMeters = result.data.distanceMeters,
                        totalDurationSeconds = result.data.durationSeconds,
                        departureTime = input.departureTime,
                        config = _samplingConfig,
                    )
                    Timber.d("Route sampled: ${samplePoints.size} points at ${_samplingConfig.intervalKm}km intervals")
                    samplePoints.forEachIndexed { i, pt ->
                        Timber.d("  Point $i: ${pt.latitude},${pt.longitude} @ ${pt.eta} (+${pt.distanceFromStartMeters / 1000}km)")
                    }
                    Result.Success(
                        RouteUiModel(
                            route = result.data.copy(samplePoints = samplePoints),
                            polylinePoints = decoded,
                            distanceKm = result.data.distanceMeters / 1000.0,
                            durationFormatted = formatDuration(result.data.durationSeconds),
                            samplePoints = samplePoints,
                        )
                    )
                }
                is Result.Error -> result
                is Result.Loading -> result
            }
        }
    }

    fun updateSamplingConfig(config: SamplingConfig) {
        _samplingConfig = config
        val input = _currentInput ?: return
        val currentSuccess = _routeState.value as? Result.Success ?: return
        val decoded = currentSuccess.data.polylinePoints
        val route = currentSuccess.data.route
        val samplePoints = sampleRouteUseCase(
            polylinePoints = decoded,
            totalDistanceMeters = route.distanceMeters,
            totalDurationSeconds = route.durationSeconds,
            departureTime = input.departureTime,
            config = _samplingConfig,
        )
        Timber.d("Route re-sampled: ${samplePoints.size} points at ${_samplingConfig.intervalKm}km intervals")
        _routeState.value = Result.Success(
            currentSuccess.data.copy(
                route = route.copy(samplePoints = samplePoints),
                samplePoints = samplePoints,
            )
        )
    }

    fun clearRoute() {
        _routeState.value = Result.Error(null, null)
    }

    private fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}
