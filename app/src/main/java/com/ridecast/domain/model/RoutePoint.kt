package com.ridecast.domain.model

import java.time.ZonedDateTime

/**
 * A sampled point along the decoded route polyline.
 *
 * Route sampling generates one [RoutePoint] every N kilometres (configurable in Settings).
 * The [eta] field represents when the rider is expected to pass through this point based
 * on the total route duration — initially proportional, replaceable with per-leg ETA later.
 */
data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    /** Cumulative distance from the trip origin in metres. */
    val distanceFromStartMeters: Long,
    /** Estimated time of arrival at this point, accounting for departure time. */
    val eta: ZonedDateTime,
)
