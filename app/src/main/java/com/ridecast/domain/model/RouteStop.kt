package com.ridecast.domain.model

/**
 * A resolved stop along a route — used for intermediate waypoints between origin and destination.
 */
data class RouteStop(
    val name: String,
    val lat: Double,
    val lng: Double,
    val placeId: String? = null,
)
