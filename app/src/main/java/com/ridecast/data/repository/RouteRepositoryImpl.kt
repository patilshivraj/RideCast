package com.ridecast.data.repository

import com.ridecast.BuildConfig
import com.ridecast.core.util.Result
import com.ridecast.data.network.RoutesApiService
import com.ridecast.data.network.dto.ComputeRoutesRequest
import com.ridecast.data.network.dto.RouteLocation
import com.ridecast.data.network.dto.RouteWaypoint
import com.ridecast.data.network.dto.RoutesLatLng
import com.ridecast.domain.model.Route
import com.ridecast.domain.model.TripInput
import com.ridecast.domain.repository.RouteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepositoryImpl @Inject constructor(
    private val routesApiService: RoutesApiService,
) : RouteRepository {

    override suspend fun calculateRoute(input: TripInput): Result<Route> {
        return try {
            val intermediates = input.intermediateStops.map { stop ->
                RouteWaypoint(RouteLocation(RoutesLatLng(stop.lat, stop.lng)))
            }
            val request = ComputeRoutesRequest(
                origin = RouteWaypoint(RouteLocation(RoutesLatLng(input.originLat, input.originLng))),
                destination = RouteWaypoint(RouteLocation(RoutesLatLng(input.destinationLat, input.destinationLng))),
                intermediates = intermediates,
                travelMode = input.travelMode.apiValue,
                departureTime = input.departureTime.toInstant().toString(),
            )
            val response = withContext(Dispatchers.IO) {
                routesApiService.computeRoutes(
                    apiKey = BuildConfig.MAPS_API_KEY,
                    request = request,
                )
            }
            val route = response.routes?.firstOrNull()
                ?: return Result.Error(
                    exception = Exception("No routes returned"),
                    message = "No route found between those locations.",
                )
            val encodedPolyline = route.polyline?.encodedPolyline
                ?: return Result.Error(
                    exception = Exception("Missing polyline"),
                    message = "Route has no polyline data.",
                )
            val durationSeconds = route.duration
                ?.removeSuffix("s")?.toLongOrNull()
                ?: 0L
            Result.Success(
                Route(
                    distanceMeters = route.distanceMeters ?: 0L,
                    durationSeconds = durationSeconds,
                    encodedPolyline = encodedPolyline,
                    samplePoints = emptyList(), // populated by SampleRouteUseCase in RouteViewModel
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Route calculation failed")
            Result.Error(e, e.localizedMessage ?: "Failed to calculate route.")
        }
    }
}
