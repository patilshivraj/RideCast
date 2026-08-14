package com.ridecast.domain.model

/**
 * A user-saved place for quick selection on the Plan screen.
 * Persisted via [com.ridecast.domain.repository.FavoritePlacesRepository] (max 15 entries).
 */
data class FavoritePlace(
    val placeId: String,
    val name: String,
    val lat: Double,
    val lng: Double,
)
