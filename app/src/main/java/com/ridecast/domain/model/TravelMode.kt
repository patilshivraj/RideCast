package com.ridecast.domain.model

/**
 * Travel mode for Google Routes API route calculation.
 */
enum class TravelMode(val apiValue: String) {
    /** Motorcycle / two-wheeler routing (RideCast default). */
    TWO_WHEELER("TWO_WHEELER"),
    /** Standard car routing. */
    DRIVE("DRIVE"),
}
