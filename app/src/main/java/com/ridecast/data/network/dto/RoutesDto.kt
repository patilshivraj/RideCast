package com.ridecast.data.network.dto

// ── Request DTOs ──────────────────────────────────────────────────────────────

data class ComputeRoutesRequest(
    val origin: RouteWaypoint,
    val destination: RouteWaypoint,
    val intermediates: List<RouteWaypoint> = emptyList(),
    val travelMode: String = "DRIVE",
    val routingPreference: String = "TRAFFIC_AWARE",
)

data class RouteWaypoint(
    val location: RouteLocation,
)

data class RouteLocation(
    val latLng: RoutesLatLng,
)

data class RoutesLatLng(
    val latitude: Double,
    val longitude: Double,
)

// ── Response DTOs ─────────────────────────────────────────────────────────────

data class ComputeRoutesResponse(
    val routes: List<RouteDto>?,
)

data class RouteDto(
    val distanceMeters: Long?,
    /** Duration string from Routes API, e.g. "28800s" — strip the "s" suffix to get seconds. */
    val duration: String?,
    val polyline: PolylineDto?,
)

data class PolylineDto(
    val encodedPolyline: String?,
)
