package com.ridecast.domain.model

/**
 * Controls how often the route is sampled for weather lookups.
 * Kept in the domain layer so the use case doesn't reference Android or UI types.
 */
data class SamplingConfig(
    val intervalKm: Int = DEFAULT_INTERVAL_KM,
) {
    companion object {
        const val DEFAULT_INTERVAL_KM = 50
        val SUPPORTED_INTERVALS = listOf(10, 25, 50, 100)
    }
}
