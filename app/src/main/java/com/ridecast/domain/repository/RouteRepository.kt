package com.ridecast.domain.repository

import com.ridecast.core.util.Result
import com.ridecast.domain.model.Route
import com.ridecast.domain.model.TripInput

/**
 * Contract for all route-calculation data sources.
 *
 * The default implementation uses the Google Routes API. Future implementations could
 * support GPX import or cached offline routes without any changes to callers.
 */
interface RouteRepository {

    /**
     * Calculates a route for the given [input] and returns it as a [Route] containing
     * the encoded polyline, distance, duration, and sampled waypoints.
     */
    suspend fun calculateRoute(input: TripInput): Result<Route>
}
