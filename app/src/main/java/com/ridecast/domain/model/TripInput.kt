package com.ridecast.domain.model

import java.time.ZonedDateTime

/**
 * Represents the user's intent for a ride — origin, destination, and departure time.
 *
 * Both origin and destination carry both a human-readable name (for display) and
 * precise coordinates (for the Routes API and weather lookups).
 */
data class TripInput(
    val originName: String,
    val originLat: Double,
    val originLng: Double,
    val destinationName: String,
    val destinationLat: Double,
    val destinationLng: Double,
    /** The exact moment the rider plans to leave. Timezone-aware for accurate ETA computation. */
    val departureTime: ZonedDateTime,
)
