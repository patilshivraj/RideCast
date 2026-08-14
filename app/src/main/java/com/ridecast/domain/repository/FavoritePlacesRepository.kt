package com.ridecast.domain.repository

import com.ridecast.domain.model.FavoritePlace
import kotlinx.coroutines.flow.Flow

interface FavoritePlacesRepository {
    val favorites: Flow<List<FavoritePlace>>

    /** @return true when added; false when already saved or at the 15-place limit */
    suspend fun addFavorite(place: FavoritePlace): Boolean

    suspend fun removeFavorite(placeId: String)

    suspend fun isFavorite(placeId: String): Boolean
}
