package com.ridecast.domain.model

/**
 * The computed route between origin and destination.
 *
 * [encodedPolyline] is the Google Maps encoded polyline string which must be decoded
 * before rendering or sampling. [samplePoints] contains the decoded, sampled waypoints
 * with their ETAs — one entry per N kilometres as configured.
 */
data class Route(
    val distanceMeters: Long,
    val durationSeconds: Long,
    /** Google Maps encoded polyline (precision 5). Decoded in Milestone 3. */
    val encodedPolyline: String,
    /** Sampled waypoints along the route, each carrying an ETA. Populated in Milestone 4. */
    val samplePoints: List<RoutePoint>,
)
